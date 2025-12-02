package pl.thedeem.intellij.dpl.psi.elements;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dpl.definition.model.Configuration;
import pl.thedeem.intellij.dpl.definition.model.ExpressionDescription;
import pl.thedeem.intellij.dpl.psi.*;

import java.util.List;
import java.util.Set;

public interface ExpressionElement extends PsiElement {
    @NotNull Set<String> getDefinedParameters(@Nullable DPLGroupExpression group);

    @Nullable ExpressionDescription getDefinition();

    @NotNull Set<Configuration> getAvailableConfiguration(@NotNull DPLGroupExpression group);

    @Nullable Configuration getParameterDefinition(@NotNull String parameterName);

    @Nullable DPLFieldName getExportedName();

    @Nullable DPLFieldName getMemberName();

    @Nullable DPLQuantifierExpression getQuantifier();

    @Nullable DPLConfigurationExpression getConfiguration();

    @Nullable DPLDefinitionExpression getDefinedExpression();

    @Nullable DPLMatchersExpression getMatchers();

    @Nullable DPLLookaroundExpression getLookaround();

    @Nullable DPLNullableExpression getNullable();

    @NotNull ExpressionParts getExpressionParts();

    boolean isMembersListExpression();

    record ExpressionParts(
            List<DPLQuantifierExpression> quantifiers,
            List<DPLConfigurationExpression> configurations,
            List<DPLMatchersExpression> matchers,
            List<DPLLookaroundExpression> lookarounds,
            List<DPLNullableExpression> nullables,
            List<DPLExportNameExpression> names,
            DPLDefinitionExpression expression
    ) {
    }
}
