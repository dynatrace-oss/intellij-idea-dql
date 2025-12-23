package pl.thedeem.intellij.dql.inspections.external;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.ide.passwordSafe.PasswordSafe;
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
import pl.thedeem.intellij.common.sdk.DynatraceRestClient;
import pl.thedeem.intellij.common.sdk.errors.DQLApiException;
import pl.thedeem.intellij.common.sdk.model.DQLSyntaxErrorPositionDetails;
import pl.thedeem.intellij.common.sdk.model.DQLVerifyPayload;
import pl.thedeem.intellij.common.sdk.model.DQLVerifyResponse;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.definition.model.QueryConfiguration;
import pl.thedeem.intellij.dql.services.query.DQLQueryConfigurationService;
import pl.thedeem.intellij.dql.services.query.DQLQueryParserService;
import pl.thedeem.intellij.dql.settings.DQLSettings;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenant;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenantsService;

import java.io.IOException;

public class DQLVerificationAnnotator extends ExternalAnnotator<DQLVerificationAnnotator.Input, DQLVerificationAnnotator.Result> {
    public record Input(PsiFile file) {
    }

    public record Result(DQLVerifyResponse response, @Nullable DQLQueryParserService.ParseResult parsedQuery) {
    }

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
        DQLQueryConfigurationService configurationService = DQLQueryConfigurationService.getInstance(project);
        QueryConfiguration configuration = configurationService.getQueryConfiguration(input.file);
        DQLQueryParserService parser = DQLQueryParserService.getInstance(project);
        DynatraceTenantsService tenantsService = DynatraceTenantsService.getInstance();
        DynatraceTenant tenant = tenantsService.findTenant(configuration.tenant());
        if (tenant != null) {
            String apiToken = PasswordSafe.getInstance().getPassword(DQLUtil.createCredentialAttributes(tenant.getCredentialId()));
            DynatraceRestClient client = new DynatraceRestClient(tenant.getUrl());
            try {
                DQLQueryParserService.ParseResult parseResult = WriteCommandAction.runWriteCommandAction(
                        project,
                        (Computable<DQLQueryParserService.ParseResult>) () -> parser.getSubstitutedQuery(input.file, configuration.definedVariables())
                );
                DQLVerifyResponse response = client.verifyDQL(new DQLVerifyPayload(parseResult.parsed()), apiToken);
                return new Result(response, parseResult);
            } catch (IOException | InterruptedException | DQLApiException e) {
                return new Result(new DQLVerifyResponse(), null);
            }
        }
        return new Result(new DQLVerifyResponse(), null);
    }

    @Override
    public void apply(@NotNull PsiFile file, Result result, @NotNull AnnotationHolder holder) {
        for (DQLVerifyResponse.DQLVerifyNotification notification : result.response().getNotifications()) {
            holder.newAnnotation(HighlightSeverity.GENERIC_SERVER_ERROR_OR_WARNING, notification.getMessage())
                    .range(getTextRange(notification, result.parsedQuery()))
                    .highlightType(getSeverity(notification))
                    .create();
        }
    }

    private TextRange getTextRange(DQLVerifyResponse.DQLVerifyNotification notification, @Nullable DQLQueryParserService.ParseResult parsedQuery) {
        DQLSyntaxErrorPositionDetails syntaxPosition = notification.getSyntaxPosition();
        int start = syntaxPosition.getStartIndex();
        int end = syntaxPosition.getEndIndex();
        if (parsedQuery != null) {
            start = parsedQuery.getOriginalOffset(start);
            end = parsedQuery.getOriginalOffset(end);
        }
        return new TextRange(start, end);
    }

    private boolean canPerformExternalValidation(Input input) {
        // We cannot do any annotations on partial files, as they will not be valid either way
        return !DQLUtil.isPartialFile(input.file)
                && DQLSettings.getInstance().isPerformingLiveValidationEnabled();
    }

    private ProblemHighlightType getSeverity(DQLVerifyResponse.DQLVerifyNotification notification) {
        return switch (notification.getSeverity()) {
            case "INFO" -> ProblemHighlightType.INFORMATION;
            case "WARN", "WARNING" -> ProblemHighlightType.WARNING;
            default -> ProblemHighlightType.GENERIC_ERROR;
        };
    }
}
