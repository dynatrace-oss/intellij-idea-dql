package pl.thedeem.intellij.dql.services.dynatrace

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import kotlinx.coroutines.*
import pl.thedeem.intellij.common.sdk.DynatraceRestClient
import pl.thedeem.intellij.common.sdk.errors.DQLInvalidResponseException
import pl.thedeem.intellij.common.sdk.errors.DQLNotAuthorizedException
import pl.thedeem.intellij.common.sdk.model.*
import pl.thedeem.intellij.dql.DQLBundle
import pl.thedeem.intellij.dql.services.notifications.DQLNotificationsService
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenant
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenantsService
import java.net.ConnectException
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

internal class DynatraceRestServiceImpl(
    private val project: Project,
    private val cs: CoroutineScope
) : DynatraceRestService {

    companion object {
        private val logger = Logger.getInstance(DynatraceRestServiceImpl::class.java)
        private const val POLL_INTERVAL_MS = 200L
    }

    override fun verifyQuery(tenant: DynatraceTenant, query: String): CompletableFuture<DQLVerifyResponse> =
        executeAsync { DynatraceRestClient(tenant.url).verifyDQL(DQLVerifyPayload(query), resolveToken(tenant)) }

    override fun autocompleteQuery(
        tenant: DynatraceTenant,
        query: String,
        offset: Long
    ): CompletableFuture<DQLAutocompleteResult> =
        executeAsync {
            DynatraceRestClient(tenant.url).autocomplete(
                DQLAutocompletePayload(query, offset),
                resolveToken(tenant)
            )
        }


    override fun executeQuery(
        tenant: DynatraceTenant,
        payload: DQLExecutePayload,
        progressConsumer: Consumer<DQLPollResponse>?
    ): CompletableFuture<DQLPollResponse> {
        val future = CompletableFuture<DQLPollResponse>()
        val job = cs.launch {
            try {
                val result = withBackgroundProgress(
                    project,
                    DQLBundle.message("services.restService.executeQuery.progress.title"),
                    cancellable = true
                ) {
                    val client = DynatraceRestClient(tenant.url)
                    val apiToken = resolveToken(tenant)
                    val executeResponse = withContext(Dispatchers.IO) { client.executeDQL(payload, apiToken) }
                    val requestToken = executeResponse.requestToken
                        ?: throw IllegalStateException("DQL execute response did not return a request token")

                    logger.info("DQL query started: token=$requestToken, state=${executeResponse.state}")

                    try {
                        pollUntilFinished(client, requestToken, apiToken, progressConsumer)
                    } catch (e: CancellationException) {
                        logger.info("DQL query $requestToken cancelled, requesting cancellation on Dynatrace")
                        withContext(NonCancellable + Dispatchers.IO) {
                            runCatching { client.cancelDQL(requestToken, apiToken) }
                                .onFailure {
                                    logger.warn(
                                        "Failed to send cancel request for DQL query $requestToken",
                                        it
                                    )
                                }
                        }
                        throw e
                    }
                }
                future.complete(result)
            } catch (e: CancellationException) {
                future.cancel(true)
                throw e
            } catch (e: Exception) {
                future.completeExceptionally(e)
            }
        }
        future.whenComplete { _, _ ->
            if (future.isCancelled) {
                job.cancel()
            }
        }
        return future
    }

    private suspend fun pollUntilFinished(
        client: DynatraceRestClient,
        requestToken: String,
        apiToken: String?,
        progressConsumer: Consumer<DQLPollResponse>?
    ): DQLPollResponse {
        var pollResponse: DQLPollResponse
        do {
            yield()
            delay(POLL_INTERVAL_MS)
            pollResponse = withContext(Dispatchers.IO) { client.pollDQLState(requestToken, apiToken) }

            val fraction = (pollResponse.progress ?: 0L) / 100.0
            ProgressManager.getInstance().progressIndicator?.fraction = fraction

            val snapshot = pollResponse
            progressConsumer?.let { consumer ->
                withContext(Dispatchers.Main) { consumer.accept(snapshot) }
            }
        } while (!pollResponse.isFinished)
        return pollResponse
    }

    private suspend fun resolveToken(tenant: DynatraceTenant): String? = withContext(Dispatchers.IO) {
        DynatraceTenantsService.getInstance().resolveApiToken(project, tenant)
    }

    private fun <T> executeAsync(block: suspend () -> T): CompletableFuture<T> {
        val future = CompletableFuture<T>()
        cs.launch(Dispatchers.IO) {
            try {
                future.complete(block())
            } catch (e: CancellationException) {
                future.cancel(true)
                throw e
            } catch (e: Exception) {
                future.completeExceptionally(e)
            }
        }
        return future
    }

    override fun <T : Any> withStandardErrorHandling(
        future: CompletableFuture<T>,
        tenant: DynatraceTenant
    ): CompletableFuture<T> {
        return future.whenComplete { _, error ->
            if (error == null || error is CancellationException) return@whenComplete
            val manageTenants = listOf(ActionManager.getInstance().getAction("DQL.ManageTenants"))
            ApplicationManager.getApplication().invokeLater {
                val notification = DQLNotificationsService.getInstance(project)
                when (error) {
                    is ConnectException -> notification.showErrorNotification(
                        DQLBundle.message("notifications.error.invalidConnection.title", tenant.name),
                        DQLBundle.message(
                            "notifications.error.invalidConnection.message",
                            tenant.name,
                            tenant.url,
                            error.message
                        ),
                        project, manageTenants
                    )

                    is DQLNotAuthorizedException -> notification.showErrorNotification(
                        DQLBundle.message("notifications.error.invalidAuth.title", tenant.name),
                        DQLBundle.message("notifications.error.invalidAuth.message", tenant.name, error.apiMessage),
                        project, manageTenants
                    )

                    is DQLInvalidResponseException -> notification.showErrorNotification(
                        DQLBundle.message("notifications.error.invalidResponse.title", tenant.name),
                        DQLBundle.message("notifications.error.invalidResponse.message", error.apiMessage),
                        project, manageTenants
                    )

                    else -> notification.showErrorNotification(
                        DQLBundle.message("notifications.error.unknownError.title", tenant.name),
                        DQLBundle.message("notifications.error.unknownError.message", error.message),
                        project, manageTenants
                    )
                }
            }
        }
    }
}
