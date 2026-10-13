package pl.thedeem.intellij.dpl.structure;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dpl.DPLFile;
import pl.thedeem.intellij.dpl.psi.*;
import pl.thedeem.intellij.dpl.psi.elements.ExpressionElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DPLStructureViewElement extends PsiTreeElementBase<NavigatablePsiElement> implements SortableTreeElement {
    public DPLStructureViewElement(NavigatablePsiElement element) {
        super(element);
    }

    @Override
    public @NotNull String getAlphaSortKey() {
        NavigatablePsiElement element = getElement();
        if (element != null) {
            String name = element.getName();
            return name != null ? name : "";
        }
        return "";
    }

    @Override
    public @NotNull ItemPresentation getPresentation() {
        NavigatablePsiElement element = getElement();
        if (element != null) {
            ItemPresentation presentation = element.getPresentation();
            return presentation != null ? presentation : new PresentationData();
        }
        return new PresentationData();
    }

    @Override
    public @Nullable String getPresentableText() {
        return getPresentation().getPresentableText();
    }

    @Override
    public @NotNull Collection<StructureViewTreeElement> getChildrenBase() {
        NavigatablePsiElement element = getElement();
        if (element == null) return List.of();

        List<StructureViewTreeElement> result = new ArrayList<>();

        switch (element) {
            case DPLFile file -> {
                DPLDpl dpl = PsiTreeUtil.getChildOfType(file, DPLDpl.class);
                if (dpl != null) {
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
            }
        }

        return result;
    }

    private <T extends PsiElement> @NotNull List<StructureViewTreeElement> convert(List<T> elements) {
        return elements.stream()
                .filter(e -> e instanceof NavigatablePsiElement)
                .map(e -> (StructureViewTreeElement) new DPLStructureViewElement((NavigatablePsiElement) e))
                .toList();
    }
}