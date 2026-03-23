package pl.thedeem.intellij.dql.services.dynatrace

import com.intellij.credentialStore.Credentials
import com.intellij.ide.BrowserUtil
import com.intellij.ide.passwordSafe.PasswordSafe
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pl.thedeem.intellij.common.DefinitionUtils
import pl.thedeem.intellij.common.sdk.SimpleOAuthClient
import pl.thedeem.intellij.common.sdk.errors.SSONotConfiguredException
import pl.thedeem.intellij.common.sdk.errors.SSOReAuthRequiredException
import pl.thedeem.intellij.dql.DQLUtil
import pl.thedeem.intellij.dql.services.dynatrace.model.OAuthConfig
import pl.thedeem.intellij.dql.services.dynatrace.model.OAuthEnvironment
import java.net.URI
import java.net.URISyntaxException
import java.time.Instant
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

internal class DynatraceOAuthServiceImpl @JvmOverloads constructor(
    private val cs: CoroutineScope,
    private val client: SimpleOAuthClient = SimpleOAuthClient()
) : DynatraceOAuthService {
    companion object {
        private const val CRED_PREFIX = "oauth-refresh-"
        private const val DEFINITION_FILE = "oauth/config.json"
        private const val EXPIRY_BUFFER_SECONDS = 60L
    }

    private val tokenCache = ConcurrentHashMap<String, Map<String, Any>>()
    private val mutex = Mutex()
    private val signInFutures = ConcurrentHashMap<String, CompletableFuture<Void>>()
    private val config: OAuthConfig by lazy { loadConfiguration() }

    override fun signIn(environmentUrl: String, credentialId: String): CompletableFuture<Void> {
        return signInFutures.computeIfAbsent(credentialId) {
            val future = CompletableFuture<Void>()
            cs.launch(Dispatchers.IO) {
                try {
                    val oauth = getOAuthEnvironment(environmentUrl)
                    val tokens = client.startAuthFlow(
                        oauth.authUrl(), oauth.tokenUrl(), oauth.clientId(),
                        environmentUrl, config.defaultScopes(), BrowserUtil::browse
                    ).await()

                    persistSecret(credentialId, tokens, null)
                    future.complete(null)
                } catch (e: CancellationException) {
                    future.cancel(true)
                    throw e
                } catch (e: Exception) {
                    future.completeExceptionally(e)
                } finally {
                    signInFutures.remove(credentialId)
                }
            }
            future
        }
    }

    override fun resolveToken(credentialId: String, environmentUrl: String): CompletableFuture<String?> {
        val future = CompletableFuture<String?>()
        cs.launch(Dispatchers.IO) {
            try {
                val result = mutex.withLock {
                    getTokenFromCache(credentialId) ?: run {
                        val storedRefreshToken = loadRefreshToken(credentialId)
                            ?: throw SSONotConfiguredException("Not logged into Dynatrace SSO")

                        val oauth = getOAuthEnvironment(environmentUrl)
                        val refreshed = try {
                            client.refreshToken(oauth.tokenUrl(), oauth.clientId(), storedRefreshToken)
                        } catch (e: SSOReAuthRequiredException) {
                            clearCredentials(credentialId)
                            throw e
                        }

                        persistSecret(credentialId, refreshed, storedRefreshToken)
                        refreshed["access_token"] as? String
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
        return future
    }

    override fun signOut(credentialId: String): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        cs.launch(Dispatchers.IO) {
            try {
                clearCredentials(credentialId)
                future.complete(null)
            } catch (e: CancellationException) {
                future.cancel(true)
                throw e
            } catch (e: Exception) {
                future.completeExceptionally(e)
            }
        }
        return future
    }

    private fun clearCredentials(credentialId: String) {
        tokenCache.remove(credentialId)
        val attributes = DQLUtil.createCredentialAttributes(CRED_PREFIX + credentialId)
        PasswordSafe.instance[attributes] = null
    }

    private fun getTokenFromCache(id: String): String? {
        val cached = tokenCache[id] ?: return null
        val validTo = Instant.ofEpochMilli(cached["valid_to"] as Long)
        return if (Instant.now().plusSeconds(EXPIRY_BUFFER_SECONDS).isBefore(validTo)) {
            cached["access_token"] as? String
        } else null
    }

    private fun persistSecret(id: String, refreshed: Map<String, Any>, currentToken: String?) {
        tokenCache[id] = refreshed

        val tokenToStore = (refreshed["refresh_token"] as? String) ?: currentToken
        val attributes = DQLUtil.createCredentialAttributes(CRED_PREFIX + id)
        val credentials = tokenToStore?.let { Credentials("oauth", it) }

        PasswordSafe.instance[attributes] = credentials
    }

    private fun loadRefreshToken(id: String): String? =
        PasswordSafe.instance.getPassword(DQLUtil.createCredentialAttributes(CRED_PREFIX + id))

    private fun getOAuthEnvironment(environmentUrl: String): OAuthEnvironment {
        val hostname = getHostnameFromUrl(environmentUrl)
        return config.environments().find { env ->
            (env.domains() ?: emptyList()).any { domain -> hostname.endsWith(domain, ignoreCase = true) }
        } ?: config.environments().lastOrNull() ?: OAuthEnvironment.empty()
    }

    private fun getHostnameFromUrl(url: String): String = try {
        URI(url).host ?: url
    } catch (_: URISyntaxException) {
        url
    }

    private fun loadConfiguration(): OAuthConfig =
        DefinitionUtils.loadDefinitionFromFile(DEFINITION_FILE, OAuthConfig::class.java) ?: OAuthConfig.empty()
}