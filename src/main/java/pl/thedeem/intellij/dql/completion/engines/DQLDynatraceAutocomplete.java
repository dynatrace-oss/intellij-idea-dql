package pl.thedeem.intellij.dql.completion.engines;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.ex.ApplicationUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.completion.CompletionUtils;
import pl.thedeem.intellij.common.sdk.DynatraceRestClient;
import pl.thedeem.intellij.common.sdk.errors.DQLInvalidResponseException;
import pl.thedeem.intellij.common.sdk.errors.DQLNotAuthorizedException;
import pl.thedeem.intellij.common.sdk.model.DQLAutocompletePayload;
import pl.thedeem.intellij.common.sdk.model.DQLAutocompleteResult;
import pl.thedeem.intellij.common.sdk.model.DQLSuggestion;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.completion.AutocompleteUtils;
import pl.thedeem.intellij.dql.definition.model.QueryConfiguration;
import pl.thedeem.intellij.dql.services.notifications.DQLNotificationsService;
import pl.thedeem.intellij.dql.services.query.DQLQueryConfigurationService;
import pl.thedeem.intellij.dql.services.query.DQLQueryParserService;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenant;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenantsService;

import java.io.IOException;
import java.net.ConnectException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class DQLDynatraceAutocomplete {
    private static final Logger logger = Logger.getInstance(DQLDynatraceAutocomplete.class);

    public void autocomplete(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        DynatraceTenantsService tenantsService = DynatraceTenantsService.getInstance();
        DQLQueryConfigurationService configurationService = DQLQueryConfigurationService.getInstance();
        QueryConfiguration configuration = configurationService.getQueryConfiguration(parameters.getOriginalFile());
        DynatraceTenant tenant = tenantsService.findTenant(configuration.tenant());
        if (tenant == null) {
            return;
        }
        DynatraceRestClient client = new DynatraceRestClient(tenant.getUrl());
        try {
            DQLAutocompleteResult autocomplete = getAutocompleteResult(parameters, tenant, client, configuration);
            if (autocomplete == null) {
                return;
            }
            for (DQLSuggestion suggestion : autocomplete.getSuggestions()) {
                if (StringUtil.isNotEmpty(suggestion.getSuggestion())) {
                    Set<String> types = suggestion.getParts().stream().map(DQLSuggestion.DQLSuggestionPart::getType).collect(Collectors.toSet());
                    if (types.contains("SIMPLE_IDENTIFIER")) {
                        result.addElement(CompletionUtils.createLookupElement(
                                suggestion.getSuggestion(),
                                DQLIcon.DQL_FIELD,
                                AutocompleteUtils.DATA_REFERENCE,
                                null,
                                null
                        ));
                    } else if (types.contains("DATA_OBJECT")) {
                        result.addElement(CompletionUtils.createLookupElement(
                                suggestion.getSuggestion(),
                                DQLIcon.DQL_FIELD,
                                AutocompleteUtils.STATIC,
                                null,
                                null
                        ));
                    }
                }
            }
        } catch (ProcessCanceledException e) {
            logger.debug("Autocomplete operation was cancelled.");
            throw e;
        } catch (ExecutionException e) {
            logger.warn("Could not load autocomplete results from Dynatrace: " + e.getMessage());
        }
    }

    private static DQLAutocompleteResult getAutocompleteResult(
            @NotNull CompletionParameters parameters,
            @NotNull DynatraceTenant tenant,
            @NotNull DynatraceRestClient client,
            @NotNull QueryConfiguration configuration
    ) throws ExecutionException {
        Project project = parameters.getOriginalFile().getProject();
        return ApplicationUtil.runWithCheckCanceled(
                ApplicationManager.getApplication().executeOnPooledThread(() -> {
                    try {
                        ProgressManager.checkCanceled();
                        DQLQueryParserService parser = DQLQueryParserService.getInstance();
                        String apiToken = PasswordSafe.getInstance().getPassword(DQLUtil.createCredentialAttributes(tenant.getCredentialId()));
                        DQLQueryParserService.ParseResult substitutedQuery = ReadAction.compute(() -> parser.getSubstitutedQuery(parameters.getOriginalFile(), configuration.definedVariables()));
                        return client.autocomplete(
                                new DQLAutocompletePayload(substitutedQuery.parsed(), (long) substitutedQuery.getOriginalOffset(parameters.getPosition().getTextOffset())),
                                apiToken
                        );
                    } catch (ConnectException e) {
                        ApplicationManager.getApplication().invokeLater(() -> DQLNotificationsService.getInstance(project).showErrorNotification(
                                DQLBundle.message("notifications.error.invalidConnection.title", tenant.getName()),
                                DQLBundle.message("notifications.error.invalidConnection.message", tenant.getName(), tenant.getUrl(), e.getMessage()),
                                project,
                                List.of(ActionManager.getInstance().getAction("DQL.ManageTenants"))
                        ));
                        return null;
                    } catch (DQLNotAuthorizedException e) {
                        ApplicationManager.getApplication().invokeLater(() -> DQLNotificationsService.getInstance(project).showErrorNotification(
                                DQLBundle.message("notifications.error.invalidAuth.title", tenant.getName()),
                                DQLBundle.message("notifications.error.invalidAuth.message", tenant.getName(), e.getApiMessage()),
                                project,
                                List.of(ActionManager.getInstance().getAction("DQL.ManageTenants"))
                        ));
                        return null;
                    } catch (DQLInvalidResponseException e) {
                        ApplicationManager.getApplication().invokeLater(() -> DQLNotificationsService.getInstance(project).showErrorNotification(
                                DQLBundle.message("notifications.error.invalidResponse.title", tenant.getName()),
                                DQLBundle.message("notifications.error.invalidResponse.message", e.getApiMessage()),
                                project,
                                List.of(ActionManager.getInstance().getAction("DQL.ManageTenants"))
                        ));
                        return null;
                    } catch (IOException e) {
                        ApplicationManager.getApplication().invokeLater(() -> DQLNotificationsService.getInstance(project).showErrorNotification(
                                DQLBundle.message("notifications.error.unknownError.title", tenant.getName()),
                                DQLBundle.message("notifications.error.unknownError.message", e.getMessage()),
                                project,
                                List.of(ActionManager.getInstance().getAction("DQL.ManageTenants"))
                        ));
                        return null;
                    }
                }),
                ProgressManager.getInstance().getProgressIndicator()
        );
    }
}
