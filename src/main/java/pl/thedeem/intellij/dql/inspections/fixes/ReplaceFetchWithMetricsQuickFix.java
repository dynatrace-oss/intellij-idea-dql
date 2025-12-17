package pl.thedeem.intellij.dql.inspections.fixes;

import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.quickFixes.AbstractReplaceElementQuickFix;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.DQLDefinitionService;
import pl.thedeem.intellij.dql.definition.model.Command;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.psi.DQLQueryStatement;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ReplaceFetchWithMetricsQuickFix extends AbstractReplaceElementQuickFix<DQLQueryStatement> {
    @Override
    protected @Nullable DQLQueryStatement getElementToReplace(@NotNull PsiElement element) {
        return element instanceof DQLQueryStatement statement ? statement : PsiTreeUtil.getParentOfType(element, DQLQueryStatement.class);
    }

    @Override
    protected @NotNull String getDefaultReplacement(@NotNull DQLQueryStatement element) {
        DQLDefinitionService service = DQLDefinitionService.getInstance(element.getProject());
        Command metrics = service.getCommandByName("metrics");
        Command definition = element.getDefinition();
        if (metrics == null || definition == null) {
            return element.getText();
        }
        Set<String> metricsParameters = metrics.parameters().stream().map(Parameter::name).collect(Collectors.toSet());
        return metrics.name() + " " + joinCommandParameters(element.getParameters(), metricsParameters);
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return DQLBundle.message("inspection.fix.genericFamilyName");
    }

    @NotNull
    @Override
    public String getName() {
        return DQLBundle.message("inspection.variable.metricSeriesMigration.issueDetected.fix");
    }

    @Override
    protected boolean useTemplateVariable() {
        return false;
    }

    private @NotNull String joinCommandParameters(@NotNull List<MappedParameter> parameters, @NotNull Set<String> metricsParameters) {
        return String.join(",", parameters.stream()
                .filter(p -> p.definition() != null && metricsParameters.contains(p.name()))
                .map(p -> {
                    String result = "";
                    if (p.definition().requiresName() && !p.isExplicitlyNamed()) {
                        result += p.name() + ": ";
                    }
                    result += String.join(", ", p.getExpressions().stream().map(PsiElement::getText).toList());
                    return result;

                }).toList());
    }
}
