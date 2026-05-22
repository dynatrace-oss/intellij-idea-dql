package pl.thedeem.intellij.dql.completion.engines;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.PlainPrefixMatcher;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ex.ApplicationUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.completion.CompletionUtils;
import pl.thedeem.intellij.common.sdk.model.DQLAutocompleteResult;
import pl.thedeem.intellij.common.sdk.model.DQLSuggestion;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.completion.AutocompleteUtils;
import pl.thedeem.intellij.dql.psi.DQLString;
import pl.thedeem.intellij.dql.services.dynatrace.DynatraceRestService;
import pl.thedeem.intellij.dql.services.query.DQLQueryConfigurationService;
import pl.thedeem.intellij.dql.services.query.DQLQueryParserService;
import pl.thedeem.intellij.dql.services.query.model.QueryConfiguration;
import pl.thedeem.intellij.dql.services.variables.DQLVariablesService;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenant;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenantsService;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class DQLDynatraceAutocomplete {
    private static final int MAX_SUGGESTIONS = 150;
    private static final Logger logger = Logger.getInstance(DQLDynatraceAutocomplete.class);

    public void autocomplete(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        DQLQueryConfigurationService configurationService = DQLQueryConfigurationService.getInstance();
        QueryConfiguration configuration = configurationService.getQueryConfiguration(parameters.getOriginalFile());
        DynatraceTenant tenant = DynatraceTenantsService.getInstance().findTenant(configuration.tenant());
        if (tenant == null) {
            return;
        }
        Project project = parameters.getOriginalFile().getProject();
        List<DQLVariablesService.VariableDefinition> variables = DQLVariablesService
                .getInstance(project)
                .getDefinedVariables(parameters.getOriginalFile());
        DQLQueryParserService.ParseResult substitutedQuery = DQLQueryParserService.getInstance()
                .getSubstitutedQuery(parameters.getOriginalFile(), variables);
        long offset = substitutedQuery.getSubstitutedOffset(parameters.getOffset());
        try {
            DQLAutocompleteResult autocomplete = getAutocompleteResult(project, tenant, substitutedQuery, offset);
            if (autocomplete == null) {
                return;
            }
            for (DQLSuggestion suggestion : autocomplete.getSuggestions().subList(0, Math.min(autocomplete.getSuggestions().size(), MAX_SUGGESTIONS))) {
                ProgressManager.checkCanceled();
                if (StringUtil.isNotEmpty(suggestion.getSuggestion())) {
                    addSuggestionElement(parameters, suggestion, result);
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
            @NotNull Project project,
            @NotNull DynatraceTenant tenant,
            @NotNull DQLQueryParserService.ParseResult substitutedQuery,
            long offset
    ) throws ExecutionException {
        return ApplicationUtil.runWithCheckCanceled(
                ApplicationManager.getApplication().executeOnPooledThread(() -> {
                    try {
                        ProgressManager.checkCanceled();
                        DynatraceRestService rest = DynatraceRestService.getInstance(project);
                        return rest.withStandardErrorHandling(
                                rest.autocompleteQuery(tenant, substitutedQuery.parsed(), offset),
                                tenant
                        ).get();
                    } catch (ExecutionException e) {
                        return null;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return null;
                    }
                }),
                ProgressManager.getInstance().getProgressIndicator()
        );
    }

    // The list is available here:
    // https://developer.dynatrace.com/develop/sdks/client-query/#autocompletesuggestionpart
    // Most options are provided by plugin itself; only data-related values need to be processed
    private static void addSuggestionElement(@NotNull CompletionParameters parameters, @NotNull DQLSuggestion suggestion, @NotNull CompletionResultSet result) {
        Set<String> types = suggestion.getParts().stream()
                .map(DQLSuggestion.DQLSuggestionPart::getType)
                .collect(Collectors.toSet());
        if (types.contains("BUCKET")) {
            result.addElement(CompletionUtils.createLookupElement(
                    suggestion.getSuggestion(),
                    DQLIcon.BUCKET,
                    AutocompleteUtils.BUCKET,
                    null,
                    null
            ));
            return;
        }
        if (types.contains("TABULAR_FILE_NAME") || types.contains("SAVED_TABLE_NAME")) {
            LookupElement element = CompletionUtils.createLookupElement(
                    suggestion.getSuggestion(),
                    DQLIcon.TABULAR_FILE,
                    AutocompleteUtils.FILE,
                    null,
                    null
            );
            // Dynatrace returns one path segment instead of the whole file path (which IDE normally expects).
            String typedPrefix = suggestion.getSuggestion().substring(0, Math.toIntExact(suggestion.alreadyTypedCharacters));
            result.withPrefixMatcher(new PlainPrefixMatcher(typedPrefix, true)).addElement(element);
            return;
        }
        if (types.contains("SMARTSCAPE_EDGE_PATTERN") || types.contains("SMARTSCAPE_NODE_PATTERN")
                || types.contains("SMARTSCAPE_NODE_TYPE") || types.contains("SMARTSCAPE_EDGE_TYPE")) {
            String suggestionValue = suggestion.getSuggestion();
            PsiFile originalFile = parameters.getOriginalFile();
            int originalOffset = parameters.getOffset();
            PsiElement originalElement = originalFile.findElementAt(originalOffset);
            if (originalElement == null && originalOffset > 0) {
                originalElement = originalFile.findElementAt(originalOffset - 1);
            }
            DQLString originalLiteral = PsiTreeUtil.getParentOfType(originalElement, DQLString.class);
            RangeMarker rangeMarker = originalLiteral != null ? parameters.getEditor().getDocument().createRangeMarker(originalLiteral.getTextRange()) : null;
            String lookupText = suggestionValue.length() >= 2 && suggestionValue.startsWith("\"") && suggestionValue.endsWith("\"")
                    ? suggestionValue.substring(1, suggestionValue.length() - 1)
                    : suggestionValue;
            result.addElement(CompletionUtils.createLookupElement(
                    lookupText,
                    DQLIcon.SMARTSCAPE,
                    AutocompleteUtils.SMARTSCAPE,
                    null,
                    (insertionContext, lookupElement) -> {
                        int start = insertionContext.getStartOffset();
                        int end = insertionContext.getTailOffset();
                        if (rangeMarker != null && rangeMarker.isValid()) {
                            start = Math.min(start, rangeMarker.getStartOffset());
                            end = Math.max(end, rangeMarker.getEndOffset());
                            rangeMarker.dispose();
                        }
                        insertionContext.getDocument().replaceString(start, end, suggestionValue);
                        insertionContext.getEditor().getCaretModel().moveToOffset(start + suggestionValue.length());
                    }
            ));
            return;
        }
        if (types.contains("METRIC_KEY")) {
            result.addElement(CompletionUtils.createLookupElement(
                    suggestion.getSuggestion(),
                    DQLIcon.METRIC,
                    AutocompleteUtils.METRIC,
                    null,
                    null
            ));
            return;
        }
        if (types.contains("SIMPLE_IDENTIFIER")) {
            result.addElement(CompletionUtils.createLookupElement(
                    suggestion.getSuggestion(),
                    DQLIcon.DQL_FIELD,
                    AutocompleteUtils.DATA_REFERENCE,
                    null,
                    null
            ));
            return;
        }
        if (types.contains("DATA_OBJECT")) {
            result.addElement(CompletionUtils.createLookupElement(
                    suggestion.getSuggestion(),
                    DQLIcon.DATA_OBJECT,
                    AutocompleteUtils.DATA_OBJECT,
                    null,
                    null
            ));
        }
    }

}
