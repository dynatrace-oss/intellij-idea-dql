package pl.thedeem.intellij.dql.structure;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import pl.thedeem.intellij.dql.DQLFile;
import pl.thedeem.intellij.dql.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DQLStructureViewElement implements StructureViewTreeElement, SortableTreeElement {
    private final NavigatablePsiElement element;

    public DQLStructureViewElement(NavigatablePsiElement element) {
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
    public TreeElement @NotNull [] getChildren() {
        return switch (element) {
            case DQLFile file -> {
                DQLQuery[] queries = PsiTreeUtil.getChildrenOfType(file, DQLQuery.class);
                yield handleChildren(queries != null ? List.of(queries) : List.of());
            }
            case DQLQuery query -> handleChildren(query.getQueryStatementList());
            case DQLQueryStatement query -> handleChildren(query.getExpressionList());
            case DQLFunctionCallExpression expression -> handleChildren(expression.getExpressionList());
            case DQLExpression expression -> {
                List<PsiElement> expressions = PsiTreeUtil.getChildrenOfAnyType(expression, DQLExpression.class, DQLQuery.class);
                yield handleChildren(expressions);
            }
            default -> EMPTY_ARRAY;
        };
    }

    private TreeElement @NotNull [] handleChildren(Collection<?> children) {
        if (children == null) {
            return EMPTY_ARRAY;
        }
        List<TreeElement> result = new ArrayList<>(children.size());
        for (Object child : children) {
            if (child instanceof DQLQuery query) {
                result.addAll(List.of(handleChildren(query.getQueryStatementList())));
            }
            else if (child instanceof DQLParenthesisedExpression expr) {
                if (expr.getExpression() != null) {
                    result.addAll(List.of(handleChildren(List.of(expr.getExpression()))));
                }
            }
            else {
                result.add(new DQLStructureViewElement((NavigatablePsiElement) child));
            }
        }
        return result.toArray(new TreeElement[0]);
    }
}
