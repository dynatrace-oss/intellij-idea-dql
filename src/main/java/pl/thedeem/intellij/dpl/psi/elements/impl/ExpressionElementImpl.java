package pl.thedeem.intellij.dpl.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.DPLIcon;
import pl.thedeem.intellij.dpl.definition.DPLDefinitionService;
import pl.thedeem.intellij.dpl.definition.model.Configuration;
import pl.thedeem.intellij.dpl.definition.model.ExpressionDescription;
import pl.thedeem.intellij.dpl.psi.*;
import pl.thedeem.intellij.dpl.psi.elements.ExpressionElement;

import java.util.*;

public abstract class ExpressionElementImpl extends ASTWrapperPsiElement implements ExpressionElement {
    private CachedValue<ExpressionDescription> definition;
    private CachedValue<ExpressionParts> expressionParts;

    public ExpressionElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull Set<String> getDefinedParameters(@Nullable DPLGroupExpression group) {
        List<DPLParameterExpression> parameters;
        if (group != null) {
            parameters = group.getParameters();
        } else {
            DPLConfigurationExpression configuration = getConfiguration();
            parameters = List.of(Objects.requireNonNullElse(PsiTreeUtil.getChildrenOfType(configuration, DPLParameterExpression.class), new DPLParameterExpression[0]));
        }

        ExpressionDescription description = getDefinition();
        Map<String, Configuration> configurationDefinition = description != null ? Objects.requireNonNullElse(description.configuration(), Map.of()) : Map.of();

        Set<String> result = new HashSet<>();
        for (DPLParameterExpression parameter : parameters) {
            DPLParameterName pName = parameter.getParameterName();
            if (pName != null) {
                String parameterName = Objects.requireNonNullElse(pName.getName(), "").toLowerCase();
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
    public @NotNull Set<Configuration> getAvailableConfiguration(@NotNull DPLGroupExpression group) {
        Set<String> definedParameters = getDefinedParameters(group);
        ExpressionDescription description = getDefinition();
        Map<String, Configuration> allParameters = description != null ? Objects.requireNonNullElse(description.configuration(), Map.of()) : Map.of();

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
        ExpressionDescription description = getDefinition();
        Map<String, Configuration> configurationDefinition = description != null ? Objects.requireNonNullElse(description.configuration(), Map.of()) : Map.of();

        String searchedName = parameterName.toLowerCase();
        for (Configuration value : configurationDefinition.values()) {
            if (value.names().contains(searchedName)) {
                return value;
            }
        }
        return null;
    }

    @Override
    public @Nullable ExpressionDescription getDefinition() {
        if (definition == null) {
            definition = CachedValuesManager.getManager(getProject()).createCachedValue(
                    () -> new CachedValueProvider.Result<>(recalculateDefinition(), this),
                    false
            );
        }
        return definition.getValue();
    }

    @Override
    public ItemPresentation getPresentation() {
        DPLDefinitionExpression definedExpression = getDefinedExpression();
        if (definedExpression instanceof NavigationItem navigationItem) {
            ItemPresentation presentation = navigationItem.getPresentation();
            if (presentation != null) {
                return presentation;
            }
        }
        return new StandardItemPresentation(DPLBundle.message("presentation.expression"), this, DPLIcon.EXPRESSION);
    }

    @Override
    public @Nullable DPLFieldName getExportedName() {
        ExpressionParts parts = getExpressionParts();
        List<DPLExportNameExpression> fields = parts.names();
        if (!fields.isEmpty()) {
            return fields.getFirst().getFieldName();
        }
        return null;
    }

    @Override
    public @Nullable DPLFieldName getMemberName() {
        ExpressionParts parts = getExpressionParts();
        List<DPLExportNameExpression> fields = parts.names();
        if (fields.size() > 1) {
            return fields.getLast().getFieldName();
        } else if (fields.size() == 1 && isMembersListExpression()) {
            return fields.getLast().getFieldName();
        }
        return null;
    }

    @Override
    public boolean isMembersListExpression() {
        if (getParent() instanceof DPLCommandMatchersContent matchers) {
            DPLExpressionDefinition parentExpression = PsiTreeUtil.getParentOfType(matchers, DPLExpressionDefinition.class);
            if (parentExpression == null) {
                return false;
            }
            ExpressionDescription definition = parentExpression.getDefinition();
            return definition != null && definition.matchers() != null && "members_list".equals(definition.matchers().type());
        }
        return false;
    }


    @Override
    public @Nullable DPLQuantifierExpression getQuantifier() {
        ExpressionParts parts = getExpressionParts();
        return parts.quantifiers().isEmpty() ? null : parts.quantifiers().getFirst();
    }

    @Override
    public @Nullable DPLConfigurationExpression getConfiguration() {
        ExpressionParts parts = getExpressionParts();
        return parts.configurations().isEmpty() ? null : parts.configurations().getFirst();
    }

    @Override
    public @Nullable DPLDefinitionExpression getDefinedExpression() {
        ExpressionParts parts = getExpressionParts();
        return parts.expression();
    }

    @Override
    public @Nullable DPLMatchersExpression getMatchers() {
        ExpressionParts parts = getExpressionParts();
        return parts.matchers().isEmpty() ? null : parts.matchers().getFirst();
    }

    @Override
    public @Nullable DPLLookaroundExpression getLookaround() {
        ExpressionParts parts = getExpressionParts();
        return parts.lookarounds().isEmpty() ? null : parts.lookarounds().getFirst();
    }

    @Override
    public @Nullable DPLNullableExpression getNullable() {
        ExpressionParts parts = getExpressionParts();
        return parts.nullables().isEmpty() ? null : parts.nullables().getFirst();
    }

    @Override
    public @NotNull ExpressionParts getExpressionParts() {
        if (expressionParts == null) {
            expressionParts = CachedValuesManager.getManager(getProject()).createCachedValue(
                    () -> new CachedValueProvider.Result<>(recalculateExpressionParts(), this),
                    false
            );
        }
        return expressionParts.getValue();
    }

    private @Nullable ExpressionDescription recalculateDefinition() {
        ExpressionParts parts = getExpressionParts();
        DPLDefinitionExpression expression = parts.expression();
        if (expression == null) {
            return null;
        }
        DPLDefinitionService service = DPLDefinitionService.getInstance(getProject());
        if (expression instanceof DPLCommandExpression command) {
            String commandName = command.getName();
            return service.commands().get(commandName != null ? commandName.toUpperCase() : "");
        }
        return expression.getDefinition();
    }

    private @NotNull ExpressionParts recalculateExpressionParts() {
        PsiElement expression = this.findChildByClass(DPLExpression.class);

        List<DPLQuantifierExpression> quantifiers = new ArrayList<>();
        List<DPLConfigurationExpression> configurations = new ArrayList<>();
        List<DPLMatchersExpression> matchers = new ArrayList<>();
        List<DPLLookaroundExpression> lookarounds = new ArrayList<>();
        List<DPLNullableExpression> nullable = new ArrayList<>();
        List<DPLExportNameExpression> names = new ArrayList<>();
        DPLDefinitionExpression expr = null;

        while (expression != null) {
            expression = switch (expression) {
                case DPLQuantifierExpression element -> {
                    quantifiers.add(element);
                    yield element.getExpression();
                }
                case DPLConfigurationExpression element -> {
                    configurations.add(element);
                    yield element.getExpression();
                }
                case DPLMatchersExpression element -> {
                    matchers.add(element);
                    yield element.getExpression();
                }
                case DPLLookaroundExpression element -> {
                    lookarounds.add(element);
                    yield element.getExpression();
                }
                case DPLNullableExpression element -> {
                    nullable.add(element);
                    yield element.getExpression();
                }
                case DPLExportNameExpression element -> {
                    names.add(element);
                    yield element.getExpression();
                }
                case DPLMetaExpression element -> element.getExpression();
                case DPLDefinitionExpression element -> {
                    expr = element;
                    yield null;
                }
                default -> null;
            };
        }
        return new ExpressionParts(quantifiers, configurations, matchers, lookarounds, nullable, names, expr);
    }
}
