package pl.thedeem.intellij.dql.inspections.expressions;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.psi.PsiUtils;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.inspections.fixes.DropElementQuickFix;
import pl.thedeem.intellij.dql.psi.*;
import pl.thedeem.intellij.dql.psi.elements.DQLParametersOwner;

import java.util.List;
import java.util.Set;

public class SortingKeywordInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitSortExpression(@NotNull DQLSortExpression sort) {
                super.visitSortExpression(sort);

                List<PsiElement> parents = PsiUtils.getElementsUntilParent(
                        sort,
                        t -> Set.of(DQLBracketExpression.class, DQLParameterExpression.class)
                                .stream().anyMatch(c -> c.isInstance(t)),
                        DQLParametersOwner.class);

                if (isSortingInvalid(parents, sort)) {
                    holder.registerProblem(
                            sort.getSortDirection(),
                            DQLBundle.message("inspection.sortingKeyword.usage.notAllowed"),
                            new DropElementQuickFix()
                    );
                }
            }
        };
    }

    private boolean isSortingInvalid(@NotNull List<PsiElement> elements, @NotNull DQLSortExpression sort) {
        if (elements.isEmpty()) {
            return true;
        }
        if (!(elements.getFirst() instanceof DQLParametersOwner parametersOwner)) {
            return true;
        }
        DQLExpression containing = elements.size() > 1 && elements.get(1) instanceof DQLExpression expr ? expr : sort;
        MappedParameter parameter = parametersOwner.getParameter(containing);
        return parameter == null || parameter.definition() == null || !parameter.definition().allowsSorting();
    }
}
