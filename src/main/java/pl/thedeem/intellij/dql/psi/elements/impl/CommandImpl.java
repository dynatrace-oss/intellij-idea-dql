package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.definition.*;
import pl.thedeem.intellij.dql.psi.*;
import pl.thedeem.intellij.dql.psi.elements.Command;

import java.util.*;
import java.util.stream.Collectors;

public abstract class CommandImpl extends ASTWrapperPsiElement implements Command {
    private CachedValue<List<DQLParameterObject>> parameters;
    private CachedValue<DQLCommandDefinition> definition;
    private CachedValue<Map<DQLExpression, DQLParameterObject>> parameterMapping;

    public CommandImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String getName() {
        DQLQueryStatementKeyword keyword = this.findChildByClass(DQLQueryStatementKeyword.class);
        return keyword != null ? keyword.getName() : getText();
    }

    @Override
    public ItemPresentation getPresentation() {
        return new DQLItemPresentation(DQLBundle.message("presentation.command", getName()), this, DQLIcon.DQL_QUERY_COMMAND);
    }

    @Override
    public String getFieldName() {
        return new DQLFieldNamesGenerator().addPart(getName()).getFieldName();
    }

    @Override
    public @NotNull List<DQLParameterObject> getParameters() {
        if (parameters == null) {
            parameters = CachedValuesManager.getManager(getProject()).createCachedValue(
                    () -> new CachedValueProvider.Result<>(recalculateParameters(), this),
                    false
            );
        }
        return parameters.getValue();
    }

    @Override
    public @NotNull Set<String> getDefinedParameterNames() {
        return getParameters().stream()
                .filter(p -> p.getDefinition() != null)
                .map(p -> p.getDefinition().name)
                .collect(Collectors.toSet());
    }

    @Override
    public @NotNull List<DQLParameterDefinition> getMissingRequiredParameters() {
        DQLCommandDefinition definition = getDefinition();
        if (definition == null) {
            return List.of();
        }

        Set<DQLParameterDefinition> definedDefinitions = getDefinedParameters();
        return definition.getRequiredParameters().stream().filter(p -> !definedDefinitions.contains(p)).toList();
    }

    @Override
    public @NotNull List<DQLParameterDefinition> getMissingExclusiveRequiredParameters() {
        DQLCommandDefinition definition = getDefinition();
        if (definition == null) {
            return List.of();
        }

        Set<DQLParameterDefinition> definedDefinitions = getDefinedParameters();
        List<DQLParameterDefinition> exclusivelyRequired = definition.getExclusiveRequiredParameters();
        DQLParameterDefinition oneRequiredFound = exclusivelyRequired.stream().filter(definedDefinitions::contains).findFirst().orElse(null);
        if (oneRequiredFound != null) {
            return List.of();
        }
        return exclusivelyRequired;
    }

    @Override
    public @NotNull Set<DQLParameterDefinition> getDefinedParameters() {
        return getParameters().stream()
                .map(DQLParameterObject::getDefinition)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Override
    public @Nullable DQLCommandDefinition getDefinition() {
        if (definition == null) {
            DQLDefinitionService service = DQLDefinitionService.getInstance(getProject());
            definition = CachedValuesManager.getManager(getProject()).createCachedValue(
                    () -> new CachedValueProvider.Result<>(service.getCommand(this.getName()), this),
                    false
            );
        }
        return definition.getValue();
    }

    @Override
    public @Nullable PsiElement getPipe() {
        ASTNode firstChild = getNode().getFirstChildNode();
        return DQLTypes.PIPE == firstChild.getElementType() ? firstChild.getPsi() : null;
    }

    @Override
    public boolean isFirstStatement() {
        DQLQuery query = (DQLQuery) getParent();
        return query.getQueryStatementList().getFirst() == this;
    }

    @Override
    public @Nullable DQLParameterObject getParameter(@NotNull DQLExpression parameter) {
        if (parameterMapping == null) {
            parameterMapping = CachedValuesManager.getManager(getProject()).createCachedValue(
                    () -> new CachedValueProvider.Result<>(recalculateParameterMapping(), this, parameters),
                    false
            );
        }
        return parameterMapping.getValue().get(parameter);
    }

    public List<DQLExpression> getParameterArguments() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, DQLExpression.class);
    }

    private List<DQLParameterObject> recalculateParameters() {
        DQLCommandDefinition definition = getDefinition();
        List<DQLExpression> defined = getParameterArguments();
        DQLParametersMapper mapper = new DQLParametersMapper(definition != null ? definition.parameters : List.of());
        return mapper.map(defined);
    }

    private Map<DQLExpression, DQLParameterObject> recalculateParameterMapping() {
        Map<DQLExpression, DQLParameterObject> parameterMapping = new HashMap<>();
        for (DQLParameterObject parameter : getParameters()) {
            parameterMapping.put(parameter.getExpression(), parameter);
        }
        return Collections.unmodifiableMap(parameterMapping);
    }
}
