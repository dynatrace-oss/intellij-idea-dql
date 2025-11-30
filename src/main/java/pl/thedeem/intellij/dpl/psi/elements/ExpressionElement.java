package pl.thedeem.intellij.dpl.psi.elements;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dpl.definition.model.Configuration;
import pl.thedeem.intellij.dpl.psi.DPLFieldName;

import java.util.Map;
import java.util.Set;

public interface ExpressionElement extends PsiElement {
    @NotNull Set<String> getDefinedParameters();

    @NotNull Map<String, Configuration> getConfigurationDefinition();

    @NotNull Set<Configuration> getAvailableConfiguration();

    @Nullable Configuration getParameterDefinition(@NotNull String parameterName);

    @Nullable DPLFieldName getExportedName();

    @Nullable DPLFieldName getMemberName();

    boolean isMembersListExpression();
}
