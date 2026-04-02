package pl.thedeem.intellij.dql.services.parameters.model;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.psi.DQLBracketExpression;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLParameterExpression;
import pl.thedeem.intellij.dql.psi.elements.BaseElement;
import pl.thedeem.intellij.dql.psi.elements.BaseTypedElement;
import pl.thedeem.intellij.dql.services.definition.model.Parameter;
import pl.thedeem.intellij.dql.services.query.DQLFieldsCalculatorService;

import java.util.*;

public class MappedParameter implements BaseTypedElement {
    private final Parameter definition;
    private final PsiElement holder;
    private final Set<PsiElement> expressions;
    private final Set<PsiElement> values;
    private final List<List<PsiElement>> parameterGroups;
    private final String name;
    private final boolean pseudo;

    public MappedParameter(@Nullable Parameter definition, @NotNull PsiElement holder) {
        this(definition, holder, false);
    }

    public MappedParameter(@Nullable Parameter definition, @NotNull PsiElement holder, boolean pseudo) {
        this.definition = definition;
        this.holder = holder;
        this.expressions = new LinkedHashSet<>();
        this.values = new LinkedHashSet<>();
        this.parameterGroups = new ArrayList<>();
        this.name = calculateName();
        this.pseudo = pseudo;
        addChildExpression(holder);
    }

    public @Nullable Parameter definition() {
        return definition;
    }

    public @NotNull PsiElement holder() {
        return holder;
    }

    @Override
    public @NotNull Collection<String> getDataType() {
        Set<String> result = new HashSet<>();
        for (PsiElement expression : expressions) {
            if (expression instanceof BaseElement element) {
                result.addAll(element.getDataType());
            }
        }
        return result;
    }

    @Override
    public boolean accessesData() {
        for (PsiElement value : expressions) {
            if (value instanceof BaseTypedElement entity && entity.accessesData()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getFieldName() {
        return DQLFieldsCalculatorService.getInstance().calculateFieldName(holder);
    }

    @Override
    public String toString() {
        return "%s with %s expressions (%s values)".formatted(name(), expressions.size(), values.size());
    }

    public @NotNull List<PsiElement> expressions() {
        return expressions.stream().toList();
    }

    public @NotNull List<PsiElement> values() {
        return values.stream().toList();
    }

    public @NotNull List<List<PsiElement>> parameterGroups() {
        return parameterGroups;
    }

    public void addChildExpression(@NotNull PsiElement currentExpression) {
        if (!expressions.contains(currentExpression)) {
            expressions.add(currentExpression);
            values.addAll(unpackValues(currentExpression));
            calculateParameterGroup(currentExpression);
        }
    }

    public @Nullable String name() {
        return name;
    }

    public boolean notPseudo() {
        return !pseudo;
    }

    public boolean includes(@NotNull PsiElement expression) {
        return expressions.contains(expression) || values.contains(expression);
    }

    public boolean explicitlyNamed() {
        return holder instanceof DQLParameterExpression;
    }

    public @NotNull List<PsiElement> unpackExpressions() {
        List<PsiElement> result = new ArrayList<>();
        List<PsiElement> toProcess = new ArrayList<>(expressions);

        while (!toProcess.isEmpty()) {
            PsiElement element = DQLUtil.unpackParenthesis(toProcess.removeFirst());
            if (element instanceof DQLBracketExpression bracket) {
                toProcess.addAll(bracket.getExpressionList());
            } else if (element != null) {
                result.add(element);
            }
        }
        return result;
    }

    private @Nullable String calculateName() {
        if (definition != null) {
            return definition.name();
        }
        if (holder instanceof DQLParameterExpression named) {
            return named.getName();
        }
        return null;
    }

    private void calculateParameterGroup(@NotNull PsiElement element) {
        DQLExpression prevSibling = PsiTreeUtil.getPrevSiblingOfType(element, DQLExpression.class);
        for (List<PsiElement> parameterGroup : parameterGroups) {
            if (parameterGroup.getLast() == prevSibling) {
                parameterGroup.add(element);
                return;
            }
        }
        ArrayList<PsiElement> newGroup = new ArrayList<>();
        newGroup.add(element);
        parameterGroups.add(newGroup);
    }

    private @NotNull List<PsiElement> unpackValues(@NotNull PsiElement expression) {
        List<PsiElement> result = new ArrayList<>();
        List<PsiElement> toProcess = new ArrayList<>();
        toProcess.add(expression);
        while (!toProcess.isEmpty()) {
            PsiElement element = DQLUtil.unpackParenthesis(toProcess.removeFirst());
            switch (element) {
                case DQLBracketExpression bracket -> toProcess.addAll(bracket.getExpressionList());
                case DQLParameterExpression parameter -> {
                    if (StringUtil.equalsIgnoreCase("alias", parameter.getName())) {
                        result.add(parameter);
                    } else if (holder == parameter) {
                        toProcess.add(parameter.getExpression());
                    }
                }
                case null -> {
                }
                default -> result.add(element);
            }
        }
        return result;
    }
}