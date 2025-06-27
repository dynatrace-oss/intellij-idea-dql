package pl.thedeem.intellij.dql.inspections.external;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.executing.DQLExecutionUtil;
import pl.thedeem.intellij.dql.executing.DQLParsedQuery;
import pl.thedeem.intellij.dql.executing.executeDql.runConfiguration.ExecuteDQLRunConfiguration;
import pl.thedeem.intellij.dql.sdk.DynatraceRestClient;
import pl.thedeem.intellij.dql.sdk.errors.DQLErrorResponseException;
import pl.thedeem.intellij.dql.sdk.errors.DQLNotAuthorizedException;
import pl.thedeem.intellij.dql.sdk.model.DQLSyntaxErrorPositionDetails;
import pl.thedeem.intellij.dql.sdk.model.DQLVerifyPayload;
import pl.thedeem.intellij.dql.sdk.model.DQLVerifyResponse;
import pl.thedeem.intellij.dql.settings.DQLSettings;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenant;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenantsService;

import java.io.IOException;

public class DQLVerificationAnnotator extends ExternalAnnotator<DQLVerificationAnnotator.Input, DQLVerificationAnnotator.Result> {
    public record Input(PsiFile file) {
    }

    public record Result(DQLVerifyResponse response, @Nullable DQLParsedQuery parsedQuery) {
    }

    @Override
    public Input collectInformation(@NotNull PsiFile file, @NotNull Editor editor, boolean hasErrors) {
        return new Input(file);
    }

    @Override
    public Result doAnnotate(Input input) {
        // We cannot do any annotations on partial files, as they will not be valid either way
        if (!canPerformExternalValidation(input)) {
            return new Result(new DQLVerifyResponse(), null);
        }
        String tenantName = findTenantName(input);
        DynatraceTenant tenant = DynatraceTenantsService.getInstance().findTenant(tenantName);
        if (tenant != null) {
            String apiToken = PasswordSafe.getInstance().getPassword(DQLUtil.createCredentialAttributes(tenant.getCredentialId()));
            DynatraceRestClient client = new DynatraceRestClient(tenant.getUrl());
            try {
                DQLParsedQuery parsedQuery = new DQLParsedQuery(input.file);
                DQLVerifyResponse response = client.verifyDQL(new DQLVerifyPayload(parsedQuery.getParsedQuery()), apiToken);
                return new Result(response, parsedQuery);
            } catch (IOException | InterruptedException | DQLNotAuthorizedException | DQLErrorResponseException e) {
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

    private TextRange getTextRange(DQLVerifyResponse.DQLVerifyNotification notification, @Nullable DQLParsedQuery parsedQuery) {
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

    private String findTenantName(Input input) {
        RunnerAndConfigurationSettings existingSettings = DQLExecutionUtil.findExistingSettings(input.file().getProject(), input.file);
        if (existingSettings != null && existingSettings.getConfiguration() instanceof ExecuteDQLRunConfiguration executeDQL) {
            String tenant = executeDQL.getTenantName();
            if (StringUtil.isNotEmpty(tenant)) {
                return tenant;
            }
        }
        return DQLSettings.getInstance().getDefaultLiveValidationsTenant();
    }
}
