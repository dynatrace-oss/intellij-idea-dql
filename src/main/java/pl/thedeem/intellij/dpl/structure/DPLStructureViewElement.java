package pl.thedeem.intellij.dpl.structure;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLFile;
import pl.thedeem.intellij.dpl.psi.*;
import pl.thedeem.intellij.dpl.psi.elements.ExpressionElement;

import java.util.ArrayList;
import java.util.List;

public class DPLStructureViewElement implements StructureViewTreeElement, SortableTreeElement {
    private final NavigatablePsiElement element;

    public DPLStructureViewElement(NavigatablePsiElement element) {
        this.element = element;
    }

    @Override
    public void navigate(boolean requestFocus) {
        element.navigate(requestFocus);
    }

    @Override
    public boolean canNavigate() {
        return element.canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
        return element.canNavigateToSource();
    }

    @Override
    public Object getValue() {
        return element;
    }

    @Override
    public @NotNull String getAlphaSortKey() {
        String name = element.getName();
        return name != null ? name : "";
    }

    @Override
    public @NotNull ItemPresentation getPresentation() {
        ItemPresentation presentation = element.getPresentation();
        return presentation != null ? presentation : new PresentationData();
    }

    @Override
    public @NotNull TreeElement[] getChildren() {
        List<TreeElement> result = new ArrayList<>();

        switch (element) {
            case DPLFile file -> {
                DPLDpl dpl = PsiTreeUtil.getChildOfType(file, DPLDpl.class);
                PsiElement[] macros = PsiTreeUtil.getChildrenOfType(dpl, DPLMacroDefinitionExpression.class);
                DPLExpressionsSequence[] expressions = PsiTreeUtil.getChildrenOfType(dpl, DPLExpressionsSequence.class);
                List<PsiElement> elements = new ArrayList<>();
                if (macros != null) {
                    elements.addAll(List.of(macros));
                }
                if (expressions != null) {
                    for (DPLExpressionsSequence expression : expressions) {
                        elements.addAll(expression.getExpressionDefinitionList());
                    }
                }
                result.addAll(convert(elements));
            }
            case DPLExpressionsSequence sequence -> result.addAll(convert(sequence.getExpressionDefinitionList()));
            case DPLMacroDefinitionExpression macro -> {
                List<PsiElement> elements = new ArrayList<>();
                elements.add(macro.getVariable());
                DPLExpressionsSequence sequence = macro.getExpressionsSequence();
                if (sequence != null) {
                    elements.addAll(sequence.getExpressionDefinitionList());
                }
                result.addAll(convert(elements));
            }
            case DPLExpressionDefinition expression -> {
                ExpressionElement.ExpressionParts parts = expression.getExpressionParts();
                List<PsiElement> elements = new ArrayList<>();
                if (expression.getDefinedExpression() != null) {
                    switch (expression.getDefinedExpression()) {
                        case DPLCommandExpression command -> elements.add(command.getCommandKeyword());
                        case DPLGroupExpression group when group.getExpressionsSequence() != null ->
                                elements.addAll(group.getExpressionsSequence().getExpressionDefinitionList());
                        case DPLGroupExpression group when group.getAlternativesExpression() != null ->
                                elements.addAll(group.getAlternativesExpression().getExpressionDefinitionList());
                        case DPLCharacterGroupExpression group -> elements.add(group.getCharacterGroupContent());
                        case DPLVariableUsageExpression variableUsage -> elements.add(variableUsage.getVariable());
                        case DPLLiteralExpression literal -> elements.add(literal.getString());
                        default -> {
                        }
                    }
                }
                for (DPLLookaroundExpression lookaround : parts.lookarounds()) {
                    elements.add(lookaround.getLookaround());
                }
                for (DPLConfigurationExpression configuration : parts.configurations()) {
                    elements.add(configuration.getConfigurationContent());
                }
                for (DPLQuantifierExpression quantifier : parts.quantifiers()) {
                    elements.add(quantifier.getQuantifierContent());
                }
                for (DPLExportNameExpression name : parts.names()) {
                    elements.add(name.getFieldName());
                }
                elements.addAll(parts.matchers());
                result.addAll(convert(elements));
            }
            case DPLExportNameExpression exportName when exportName.getFieldName() instanceof NavigatablePsiElement psi ->
                    result.add(new DPLStructureViewElement(psi));
            case DPLMatchersExpression commandMatchers -> {
                switch (commandMatchers.getCommandMatchersContent()) {
                    case DPLParametersMatchersList matchers -> result.addAll(convert(matchers.getMatcherList()));
                    case DPLMembersListMatchers matchers ->
                            result.addAll(convert(matchers.getExpressionDefinitionList()));
                    case DPLExpressionMatchersList matchers ->
                            result.addAll(convert(matchers.getExpressionDefinitionList()));
                    case null, default -> {
                    }
                }
            }
            case DPLQuantifierExpression quantifier when quantifier.getQuantifierContent() instanceof NavigatablePsiElement psi ->
                    result.add(new DPLStructureViewElement(psi));
            case DPLConfigurationExpression configuration ->
                    result.addAll(convert(configuration.getConfigurationContent().getParameterExpressionList()));
            case DPLConfigurationContent content -> result.addAll(convert(content.getParameterExpressionList()));
            case DPLLimitedQuantifier quantifier -> {
                switch (quantifier.getLimitedQuantifierRanges()) {
                    case DPLMinMaxQuantifier ranges -> result.addAll(convert(ranges.getQuantifierLimitList()));
                    case DPLMaxQuantifier ranges when ranges.getQuantifierLimit() instanceof NavigatablePsiElement psi ->
                            result.add(new DPLStructureViewElement(psi));
                    case DPLMinQuantifier ranges when ranges.getQuantifierLimit() instanceof NavigatablePsiElement psi ->
                            result.add(new DPLStructureViewElement(psi));
                    case DPLExactQuantifier ranges when ranges.getQuantifierLimit() instanceof NavigatablePsiElement psi ->
                            result.add(new DPLStructureViewElement(psi));
                    case null, default -> {
                    }
                }
            }
            default -> {
                return EMPTY_ARRAY;
            }
        }
        return result.toArray(new TreeElement[0]);
    }

    private <T extends PsiElement> @NotNull List<TreeElement> convert(List<T> element) {
        return element.stream()
                .filter(e -> e instanceof NavigatablePsiElement)
                .map(e -> new DPLStructureViewElement((NavigatablePsiElement) e))
                .map(e -> (TreeElement) e)
                .toList();
    }
}
