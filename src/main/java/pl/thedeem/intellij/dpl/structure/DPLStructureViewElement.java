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
                PsiElement[] expressions = PsiTreeUtil.getChildrenOfType(dpl, DPLExpressionDefinition.class);
                List<PsiElement> elements = new ArrayList<>();
                if (macros != null) {
                    elements.addAll(List.of(macros));
                }
                if (expressions != null) {
                    elements.addAll(List.of(expressions));
                }
                result.addAll(convert(elements));
            }
            case DPLMacroDefinitionExpression macro -> {
                List<PsiElement> elements = new ArrayList<>();
                elements.add(macro.getVariable());
                List<DPLExpressionDefinition> list = macro.getExpressionDefinitionList();
                elements.addAll(list);
                result.addAll(convert(elements));
            }
            case DPLExpressionDefinition expression -> {
                List<PsiElement> elements = new ArrayList<>();
                switch (expression.getExpression()) {
                    case DPLCommandExpression command -> {
                        elements.add(command.getCommandKeyword());
                        elements.add(command.getCommandMatchers());
                    }
                    case DPLSequenceGroupExpression group -> elements.addAll(group.getExpressionDefinitionList());
                    case DPLAlternativeGroupExpression group -> elements.addAll(group.getExpressionDefinitionList());
                    case DPLCharacterGroupExpression group -> elements.add(group.getCharacterGroupContent());
                    case DPLVariableUsageExpression variableUsage -> elements.add(variableUsage.getVariable());
                    case DPLLiteralExpression literal -> elements.add(literal.getString());
                    default -> {
                    }
                }
                elements.add(expression.getLookaround());
                elements.add(expression.getConfiguration());
                elements.add(expression.getQuantifier());
                elements.add(expression.getExportedName());
                elements.add(expression.getMemberName());
                elements.add(expression.getMemberName());
                result.addAll(convert(elements));
            }
            case DPLCommandMatchers commandMatchers -> {
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
            case DPLConfiguration configuration -> result.addAll(convert(configuration.getParameterList()));
            case DPLParameter parameter -> {
                List<PsiElement> elements = new ArrayList<>();
                if (parameter instanceof DPLNamedParameter namedParameter) {
                    elements.add(namedParameter.getParameterName());
                    elements.add(namedParameter.getParameterValue());
                } else if (parameter instanceof DPLUnnamedParameter unnamedParameter) {
                    elements.add(unnamedParameter.getParameterValue());
                }
                result.addAll(convert(elements));
            }
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
