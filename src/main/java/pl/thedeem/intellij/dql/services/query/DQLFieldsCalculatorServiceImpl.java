package pl.thedeem.intellij.dql.services.query;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.psi.PsiUtils;
import pl.thedeem.intellij.dql.psi.*;
import pl.thedeem.intellij.dql.psi.elements.BaseElement;
import pl.thedeem.intellij.dql.psi.elements.BaseTypedElement;
import pl.thedeem.intellij.dql.services.parameters.model.MappedParameter;

import java.util.*;

public class DQLFieldsCalculatorServiceImpl implements DQLFieldsCalculatorService {
    public @NotNull String calculateFieldName(@Nullable Object... parts) {
        StringBuilder nameParts = new StringBuilder();
        for (Object part : parts) {
            String fieldPart = switch (part) {
                case BaseTypedElement e -> e.getFieldName();
                case PsiElement e -> e.getText().trim();
                case Collection<?> children ->
                        String.join(",", children.stream().map(this::calculateFieldName).toList());
                case SeparatedChildren children -> {
                    List<String> calculated = children.children().stream().map(this::calculateFieldName).toList();
                    if (children.separator() != null) {
                        yield String.join(calculateFieldName(children.separator()), calculated);
                    }
                    yield String.join(",", calculated);
                }
                case null -> null;
                default -> part.toString().trim();
            };
            if (fieldPart != null) {
                nameParts.append(fieldPart);
            }
        }
        return nameParts.toString();
    }

    @Override
    public @NotNull List<MappedField> calculateDefinedFields(@NotNull MappedParameter parameter) {
        List<MappedField> result = new ArrayList<>();

        List<PsiElement> toProcess = new ArrayList<>(unpackAliases(parameter.expressions(), result));

        while (!toProcess.isEmpty()) {
            PsiElement current = toProcess.removeFirst();
            switch (current) {
                case DQLAssignExpression ex -> {
                    PsiElement right = ex.getRightExpression();
                    if (right != null) {
                        result.add(new MappedField(right, ex.getFieldName(), ex));
                    }
                }
                case DQLBracketExpression ex when ex.getExpressionList().size() == 1 ->
                        toProcess.addAll(0, ex.getExpressionList());
                case DQLBracketExpression ex ->
                        toProcess.addAll(0, unpackAliases(ex.getExpressionList().stream().map(e -> (PsiElement) e).toList(), result));
                case BaseElement ex -> result.add(new MappedField(ex, ex.getFieldName(), null));
                case null, default -> {
                }
            }
        }
        return List.copyOf(result);
    }

    private @NotNull List<PsiElement> unpackAliases(@NotNull List<PsiElement> elements, @NotNull List<MappedField> fields) {
        List<PsiElement> result = new ArrayList<>();
        Set<PsiElement> processed = new HashSet<>();
        for (PsiElement element : elements) {
            switch (element) {
                case DQLParameterExpression ex when "alias".equalsIgnoreCase(ex.getName()) && ex.definition() == null -> {
                    DQLExpression next = PsiUtils.getNextSiblingOfTypeSkippingWhitespaces(ex, DQLExpression.class, Set.of(DQLTypes.COMMA));
                    DQLExpression prev = PsiUtils.getPrevSiblingOfTypeSkippingWhitespaces(ex, DQLExpression.class, Set.of(DQLTypes.COMMA));
                    if (!processed.contains(prev) && canBeAliased(next)) {
                        PsiElement nextSibling = PsiUtils.getNextSiblingOfTypeSkippingWhitespaces(next, DQLExpression.class, Set.of(DQLTypes.COMMA));
                        DQLExpression expression = ex.getExpression();
                        if (!(nextSibling instanceof DQLParameterExpression param) || !"alias".equalsIgnoreCase(param.getName())) {
                            if (processed.add(next)) {
                                fields.add(new MappedField(next, expression != null ? expression.getText() : "", ex));
                            }
                        }
                    }
                }
                case BaseElement ex -> {
                    DQLExpression next = PsiUtils.getNextSiblingOfTypeSkippingWhitespaces(ex, DQLExpression.class, Set.of(DQLTypes.COMMA));
                    if (canBeAliased(ex) && next instanceof DQLParameterExpression param && ("alias".equalsIgnoreCase(param.getName()) && param.definition() == null)) {
                        DQLExpression expression = param.getExpression();
                        fields.add(new MappedField(ex, expression != null ? expression.getText() : "", param));
                        processed.add(ex);
                    } else if (processed.add(ex)) {
                        result.add(ex);
                    }
                }
                case null, default -> {
                }
            }
        }
        return result;
    }

    private boolean canBeAliased(@Nullable PsiElement element) {
        return element != null
                && !(element instanceof DQLBracketExpression bracket && bracket.getExpressionList().size() > 1)
                && !(element instanceof DQLParameterExpression)
                && !(element instanceof DQLAssignExpression);
    }
}
