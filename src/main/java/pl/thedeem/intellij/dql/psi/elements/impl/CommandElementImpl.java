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
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.definition.DQLDefinitionService;
import pl.thedeem.intellij.dql.definition.DQLFieldNamesGenerator;
import pl.thedeem.intellij.dql.definition.DQLParametersCalculatorService;
import pl.thedeem.intellij.dql.definition.model.Command;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.psi.DQLCommandKeyword;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLQuery;
import pl.thedeem.intellij.dql.psi.DQLTypes;
import pl.thedeem.intellij.dql.psi.elements.CommandElement;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public abstract class CommandElementImpl extends ASTWrapperPsiElement implements CommandElement {
    private CachedValue<List<MappedParameter>> parameters;
    private CachedValue<Command> definition;

    public CommandElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String getName() {
        DQLCommandKeyword keyword = this.findChildByClass(DQLCommandKeyword.class);
        return keyword != null ? keyword.getName() : getText();
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(DQLBundle.message("presentation.command", getName()), this, DQLIcon.DQL_QUERY_COMMAND);
    }

    @Override
    public String getFieldName() {
        return new DQLFieldNamesGenerator().addPart(getName()).getFieldName();
    }

    @Override
    public @NotNull List<MappedParameter> getParameters() {
        if (parameters == null) {
            parameters = CachedValuesManager.getManager(getProject()).createCachedValue(
                    () -> new CachedValueProvider.Result<>(recalculateParameters(), this),
                    false
            );
        }
        return parameters.getValue();
    }

    @Override
    public @NotNull Collection<Parameter> getMissingRequiredParameters() {
        Command definition = getDefinition();
        if (definition == null) {
            return List.of();
        }
        return DQLUtil.getMissingParameters(getParameters(), definition.requiredParameters());
    }

    @Override
    public @NotNull Collection<Parameter> getMissingParameters() {
        Command definition = getDefinition();
        if (definition == null) {
            return List.of();
        }
        return DQLUtil.getMissingParameters(getParameters(), definition.parameters());
    }

    @Override
    public @Nullable Command getDefinition() {
        if (definition == null) {
            DQLDefinitionService service = DQLDefinitionService.getInstance(getProject());
            definition = CachedValuesManager.getManager(getProject()).createCachedValue(
                    () -> new CachedValueProvider.Result<>(service.getCommandByName(Objects.requireNonNull(this.getName())), this),
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
        return query.getCommandList().getFirst() == this;
    }

    @Override
    public @Nullable MappedParameter getParameter(@NotNull DQLExpression parameter) {
        return getParameters().stream().filter(m -> m.includes(parameter)).findFirst().orElse(null);
    }

    @Override
    public @Nullable MappedParameter findParameter(@NotNull String name) {
        return getParameters().stream().filter(p -> name.equals(p.name())).findFirst().orElse(null);
    }

    public List<DQLExpression> getParameterArguments() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, DQLExpression.class);
    }

    private List<MappedParameter> recalculateParameters() {
        Command definition = getDefinition();
        List<DQLExpression> defined = getParameterArguments();
        List<Parameter> definitions = definition != null ? definition.parameters() : List.of();
        DQLParametersCalculatorService calculatorService = DQLParametersCalculatorService.getInstance(getProject());
        return calculatorService.mapParameters(defined, definitions);
    }
}
