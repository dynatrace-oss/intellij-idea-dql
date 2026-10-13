package pl.thedeem.intellij.dql.structure;

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
import pl.thedeem.intellij.dql.DQLFile;
import pl.thedeem.intellij.dql.psi.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DQLStructureViewElement extends PsiTreeElementBase<NavigatablePsiElement> implements SortableTreeElement {

    public DQLStructureViewElement(NavigatablePsiElement element) {
        super(element);
    }

    @Override
    public @NotNull ItemPresentation getPresentation() {
        NavigatablePsiElement element = getElement();
        if (element != null && element.getPresentation() != null) {
            return element.getPresentation();
        }
        return new PresentationData();
    }

    @Override
    public @NotNull String getAlphaSortKey() {
        ItemPresentation presentation = getPresentation();
        return presentation.getPresentableText() != null ? presentation.getPresentableText() : "";
    }

    @Override
    public @NotNull Collection<StructureViewTreeElement> getChildrenBase() {
        NavigatablePsiElement element = getElement();
        if (element == null) return List.of();

        return switch (element) {
            case DQLFile file -> {
                DQLQuery[] queries = PsiTreeUtil.getChildrenOfType(file, DQLQuery.class);
                yield handleChildren(queries != null ? List.of(queries) : List.of());
            }
            case DQLQuery query -> handleChildren(query.getCommandList());
            case DQLCommand query -> handleChildren(query.getExpressionList());
            case DQLFunctionExpression expression -> handleChildren(expression.getExpressionList());
            case DQLExpression expression -> {
                List<PsiElement> expressions = PsiTreeUtil.getChildrenOfAnyType(expression, DQLExpression.class, DQLQuery.class);
                yield handleChildren(expressions);
            }
            default -> List.of();
        };
    }

    private Collection<StructureViewTreeElement> handleChildren(Collection<?> children) {
        if (children == null) {
            return List.of();
        }
        List<StructureViewTreeElement> result = new ArrayList<>(children.size());
        for (Object child : children) {
            if (child instanceof DQLQuery query) {
                result.addAll(handleChildren(query.getCommandList()));
            } else if (child instanceof DQLParenthesisedExpression expr) {
                if (expr.getExpression() != null) {
                    result.addAll(handleChildren(List.of(expr.getExpression())));
                }
            } else {
                result.add(new DQLStructureViewElement((NavigatablePsiElement) child));
            }
        }
        return result;
    }

    @Override
    public @Nullable String getPresentableText() {
        return getPresentation().getPresentableText();
    }
}