package pl.thedeem.intellij.dpl.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.DPLIcon;
import pl.thedeem.intellij.dpl.DPLUtil;
import pl.thedeem.intellij.dpl.definition.DPLDefinitionService;
import pl.thedeem.intellij.dpl.definition.model.Command;
import pl.thedeem.intellij.dpl.definition.model.Configuration;
import pl.thedeem.intellij.dpl.definition.model.Expression;
import pl.thedeem.intellij.dpl.psi.*;
import pl.thedeem.intellij.dpl.psi.elements.ExpressionElement;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public abstract class ExpressionElementImpl extends ASTWrapperPsiElement implements ExpressionElement {
    private CachedValue<Map<String, Configuration>> configurationDefinition;

    public ExpressionElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull Set<String> getDefinedParameters() {
        DPLConfiguration configuration = PsiTreeUtil.getChildOfType(this, DPLConfiguration.class);
        DPLParameter[] parameters = Objects.requireNonNullElse(PsiTreeUtil.getChildrenOfType(configuration, DPLParameter.class), new DPLParameter[0]);
        Set<String> result = new HashSet<>();

        Map<String, Configuration> configurationDefinition = getConfigurationDefinition();

        for (DPLParameter parameter : parameters) {
            if (parameter instanceof DPLNamedParameter namedParameter) {
                String parameterName = Objects.requireNonNull(namedParameter.getParameterName().getName()).toLowerCase();
                result.add(parameterName);
                Configuration definition = configurationDefinition.get(parameterName);
                if (definition != null && definition.aliases() != null) {
                    result.addAll(definition.aliases());
                }
            }
        }

        return result;
    }

    @Override
    public @NotNull Set<Configuration> getAvailableConfiguration() {
        Set<String> definedParameters = getDefinedParameters();
        Map<String, Configuration> allParameters = getConfigurationDefinition();
        Set<Configuration> result = new HashSet<>();
        for (Configuration parameter : allParameters.values()) {
            Set<String> forbiddenNames = new HashSet<>(parameter.names());
            forbiddenNames.addAll(Objects.requireNonNullElse(parameter.excludes(), Set.of()));
            if (definedParameters.stream().noneMatch(forbiddenNames::contains)) {
                result.add(parameter);
            }
        }
        return result;
    }

    @Override
    public @Nullable Configuration getParameterDefinition(@NotNull String parameterName) {
        Map<String, Configuration> configurationDefinition = getConfigurationDefinition();
        String searchedName = parameterName.toLowerCase();
        for (Configuration value : configurationDefinition.values()) {
            if (value.names().contains(searchedName)) {
                return value;
            }
        }
        return null;
    }

    @Override
    public @NotNull Map<String, Configuration> getConfigurationDefinition() {
        if (configurationDefinition == null) {
            configurationDefinition = CachedValuesManager.getManager(getProject()).createCachedValue(
                    () -> new CachedValueProvider.Result<>(recalculateConfigurationDefinition(), this),
                    false
            );
        }
        return configurationDefinition.getValue();
    }

    private @NotNull Map<String, Configuration> recalculateConfigurationDefinition() {
        DPLExpression expression = findChildByClass(DPLExpression.class);
        if (expression == null) {
            return Map.of();
        }
        if (expression instanceof DPLCommandExpression command) {
            Command definition = command.getDefinition();
            if (definition == null) {
                return Map.of();
            }
            return Objects.requireNonNullElse(definition.configuration(), Map.of());
        }
        DPLDefinitionService service = DPLDefinitionService.getInstance(getProject());
        Map<String, Expression> expressions = service.expressions();
        String expressionName = DPLUtil.getExpressionName(expression);
        Expression definition = expressions.get(expressionName);
        if (definition == null) {
            return Map.of();
        }
        return Objects.requireNonNullElse(definition.configuration(), Map.of());
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(DPLBundle.message("presentation.expression"), this, DPLIcon.EXPRESSION);
    }

    @Override
    public @Nullable DPLFieldName getExportedName() {
        @NotNull DPLFieldName[] fields = this.findChildrenByClass(DPLFieldName.class);
        if (fields.length > 0) {
            return fields[fields.length - 1];
        }
        return null;
    }

    @Override
    public @Nullable DPLFieldName getMemberName() {
        @NotNull DPLFieldName[] fields = this.findChildrenByClass(DPLFieldName.class);
        if (fields.length > 1) {
            return fields[0];
        } else if (fields.length == 1 && isMembersListExpression()) {
            return fields[0];
        }
        return null;
    }

    @Override
    public boolean isMembersListExpression() {
        if (getParent() instanceof DPLCommandMatchersContent matchers) {
            DPLCommandExpression parentExpression = PsiTreeUtil.getParentOfType(matchers, DPLCommandExpression.class);
            if (parentExpression == null) {
                return false;
            }
            Command definition = parentExpression.getDefinition();
            return definition != null && definition.matchers() != null && "members_list".equals(definition.matchers().type());
        }
        return false;
    }
}
