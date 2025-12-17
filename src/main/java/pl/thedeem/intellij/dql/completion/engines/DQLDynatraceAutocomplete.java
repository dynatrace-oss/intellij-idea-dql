package pl.thedeem.intellij.dql.completion.engines;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.ex.ApplicationUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.completion.CompletionUtils;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.completion.AutocompleteUtils;
import pl.thedeem.intellij.dql.sdk.DynatraceRestClient;
import pl.thedeem.intellij.dql.sdk.errors.DQLApiException;
import pl.thedeem.intellij.dql.sdk.model.DQLAutocompletePayload;
import pl.thedeem.intellij.dql.sdk.model.DQLAutocompleteResult;
import pl.thedeem.intellij.dql.sdk.model.DQLSuggestion;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenant;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenantsService;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

public class DQLDynatraceAutocomplete {
    private static final Logger logger = Logger.getInstance(DQLDynatraceAutocomplete.class);

    public void autocomplete(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        DynatraceTenant tenant = recalculateTenantConfiguration(parameters);
        if (tenant != null) {
            DynatraceRestClient client = new DynatraceRestClient(tenant.getUrl());
            try {
                DQLAutocompleteResult autocomplete = ApplicationUtil.runWithCheckCanceled(
                        ApplicationManager.getApplication().executeOnPooledThread(() -> {
                            try {
                                ProgressManager.checkCanceled();
                                // TODO: Find a way to parse the query without causing the process cancelled issue  DQLParsedQuery query = new DQLParsedQuery(parameters.getOriginalFile());
                                String apiToken = PasswordSafe.getInstance().getPassword(DQLUtil.createCredentialAttributes(tenant.getCredentialId()));
                                return client.autocomplete(
                                        new DQLAutocompletePayload(ReadAction.compute(() -> parameters.getOriginalFile().getText()), (long) parameters.getPosition().getTextOffset()),
                                        apiToken
                                );
                            } catch (IOException | InterruptedException | DQLApiException e) {
                                logger.warn("Something went wrong when getting autocomplete results from Dynatrace: " + e.getMessage());
                                return null;
                            }
                        }),
                        ProgressManager.getInstance().getProgressIndicator()
                );

                if (autocomplete != null) {
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
                }
            } catch (ProcessCanceledException e) {
                logger.debug("Autocomplete operation was cancelled.");
                throw e;
            } catch (Exception e) {
                logger.warn("Could not load autocomplete results from Dynatrace: " + e.getMessage());
            }
        }
    }

    protected DynatraceTenant recalculateTenantConfiguration(CompletionParameters parameters) {
        String tenantName = ReadAction.compute(() -> DynatraceTenantsService.getInstance().findTenantName(
                parameters.getOriginalFile().getProject(), parameters.getOriginalFile())
        );
        return DynatraceTenantsService.getInstance().findTenant(tenantName);
    }
}
