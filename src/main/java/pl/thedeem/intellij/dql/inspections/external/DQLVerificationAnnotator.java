package pl.thedeem.intellij.dql.inspections.external;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.sdk.model.DQLSyntaxErrorPositionDetails;
import pl.thedeem.intellij.common.sdk.model.DQLVerifyResponse;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.services.dynatrace.DynatraceRestService;
import pl.thedeem.intellij.dql.services.query.DQLQueryConfigurationService;
import pl.thedeem.intellij.dql.services.query.DQLQueryParserService;
import pl.thedeem.intellij.dql.services.query.model.QueryConfiguration;
import pl.thedeem.intellij.dql.settings.DQLSettings;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenant;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenantsService;
import pl.thedeem.intellij.dqlexpr.DQLExprFileType;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class DQLVerificationAnnotator extends ExternalAnnotator<DQLVerificationAnnotator.Input, DQLVerificationAnnotator.Result> {
    @Override
    public Input collectInformation(@NotNull PsiFile file, @NotNull Editor editor, boolean hasErrors) {
        return new Input(file);
    }

    @Override
    public Result doAnnotate(Input input) {
        if (!canPerformExternalValidation(input)) {
            return new Result(new DQLVerifyResponse(), null);
        }
        Project project = input.file.getProject();
        DQLQueryConfigurationService configurationService = DQLQueryConfigurationService.getInstance();
        QueryConfiguration configuration = configurationService.getQueryConfiguration(input.file);
        DQLQueryParserService parser = DQLQueryParserService.getInstance();
        DynatraceTenantsService tenantsService = DynatraceTenantsService.getInstance();
        DynatraceRestService rest = DynatraceRestService.getInstance(project);
        DynatraceTenant tenant = tenantsService.findTenant(configuration.tenant());
        if (tenant != null) {
            DQLQueryParserService.ParseResult parseResult = WriteCommandAction.runWriteCommandAction(
                    project,
                    (Computable<DQLQueryParserService.ParseResult>) () -> parser.getSubstitutedQuery(input.file, configuration.definedVariables())
            );
            try {
                DQLVerifyResponse response = rest.withStandardErrorHandling(rest.verifyQuery(tenant, parseResult.parsed()), tenant).get();
                return new Result(response, parseResult);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return new Result(new DQLVerifyResponse(), null);
            } catch (ExecutionException e) {
                return new Result(new DQLVerifyResponse(), null);
            }
        }
        return new Result(new DQLVerifyResponse(), null);
    }

    @Override
    public void apply(@NotNull PsiFile file, @NotNull Result result, @NotNull AnnotationHolder holder) {
        for (DQLVerifyResponse.DQLVerifyNotification notification : result.response().getNotifications()) {
            holder.newAnnotation(HighlightSeverity.GENERIC_SERVER_ERROR_OR_WARNING, notification.getMessage())
                    .range(getTextRange(notification, result.parsedQuery()))
                    .highlightType(getSeverity(notification))
                    .create();
        }
    }

    private TextRange getTextRange(@NotNull DQLVerifyResponse.DQLVerifyNotification notification, @Nullable DQLQueryParserService.ParseResult parsedQuery) {
        DQLSyntaxErrorPositionDetails syntaxPosition = notification.getSyntaxPosition();
        int start = syntaxPosition.getStartIndex();
        int end = syntaxPosition.getEndIndex();
        if (parsedQuery != null) {
            start = parsedQuery.getOriginalOffset(start);
            end = parsedQuery.getOriginalOffset(end);
        }
        return new TextRange(start, end);
    }

    private boolean canPerformExternalValidation(@NotNull Input input) {
        Boolean userData = Objects.requireNonNullElseGet(
                input.file().getUserData(DQLSettings.EXTERNAL_VALIDATION_ENABLED),
                () -> DQLSettings.getInstance().isPerformingLiveValidationEnabled()
        );
        if (!userData) {
            return false;
        }
        // We cannot do any annotations on partial files, as they will not be valid either way
        return !DQLUtil.isPartialFile(input.file) && !DQLExprFileType.INSTANCE.equals(input.file.getFileType());
    }

    private ProblemHighlightType getSeverity(@NotNull DQLVerifyResponse.DQLVerifyNotification notification) {
        return switch (notification.getSeverity()) {
            case "INFO" -> ProblemHighlightType.INFORMATION;
            case "WARN", "WARNING" -> ProblemHighlightType.WARNING;
            default -> ProblemHighlightType.GENERIC_ERROR;
        };
    }

    public record Input(PsiFile file) {
    }

    public record Result(DQLVerifyResponse response, @Nullable DQLQueryParserService.ParseResult parsedQuery) {
    }
}
