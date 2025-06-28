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
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.completion.AutocompleteUtils;
import pl.thedeem.intellij.dql.sdk.DynatraceRestClient;
import pl.thedeem.intellij.dql.sdk.errors.DQLErrorResponseException;
import pl.thedeem.intellij.dql.sdk.errors.DQLNotAuthorizedException;
import pl.thedeem.intellij.dql.sdk.model.DQLAutocompletePayload;
import pl.thedeem.intellij.dql.sdk.model.DQLAutocompleteResult;
import pl.thedeem.intellij.dql.sdk.model.DQLSuggestion;
import pl.thedeem.intellij.dql.settings.DQLSettings;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenant;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenantsService;

import java.io.IOException;

public class DQLLiveAutocomplete implements DQLCompletionEngine {
   private static final Logger logger = Logger.getInstance(DQLLiveAutocomplete.class);

   @Override
   public CompletionResult autocomplete(@NotNull CompletionParameters parameters, @NotNull PsiElement position, @NotNull CompletionResultSet result) {
      PsiFile file = parameters.getOriginalFile();
      if (DQLUtil.isPartialFile(file) || !DQLSettings.getInstance().isUseDynatraceAutocompleteEnabled()) {
         return CompletionResult.PASS;
      }

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
                          new DQLAutocompletePayload(file.getText(), (long) parameters.getPosition().getTextOffset()),
                          apiToken
                      );
                   } catch (IOException | InterruptedException | DQLNotAuthorizedException | DQLErrorResponseException e) {
                     return null;
                   }
                }),
                ProgressManager.getInstance().getProgressIndicator()
            );

            if (autocomplete != null) {
               for (DQLSuggestion suggestion : autocomplete.getSuggestions()) {
                  if (StringUtil.isNotEmpty(suggestion.getSuggestion()) && suggestion.getParts().stream().anyMatch(
                      i -> "SIMPLE_IDENTIFIER".equals(i.getType())
                  )) {
                     result.addElement(AutocompleteUtils.createLookupElement(
                         suggestion.getSuggestion(),
                         DQLIcon.DQL_FIELD,
                         AutocompleteUtils.DATA_REFERENCE,
                         null,
                         null
                     ));
                  }
               }
            }
         }
         catch (ProcessCanceledException e) {
            logger.debug("Autocomplete operation was cancelled.");
            throw e;
         }
         catch (Exception e) {
            logger.warn("Could not load autocomplete results from Dynatrace: " + e.getMessage());
         }
      }

      return CompletionResult.PASS;
   }

   protected DynatraceTenant recalculateTenantConfiguration(CompletionParameters parameters) {
      String tenantName = ReadAction.compute(() -> DynatraceTenantsService.getInstance().findTenantName(
          parameters.getOriginalFile().getProject(), parameters.getOriginalFile())
      );
      return  DynatraceTenantsService.getInstance().findTenant(tenantName);
   }
}
