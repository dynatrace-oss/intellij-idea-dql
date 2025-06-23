package pl.thedeem.intellij.dql.inspections.expressions;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;
import pl.thedeem.intellij.dql.definition.DQLParameterObject;
import pl.thedeem.intellij.dql.inspections.fixes.DropElementQuickFix;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLQueryStatement;
import pl.thedeem.intellij.dql.psi.DQLSortDirection;
import pl.thedeem.intellij.dql.psi.DQLVisitor;

import java.util.List;

public class SortingKeywordInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitSortDirection(@NotNull DQLSortDirection sortDirection) {
                super.visitSortDirection(sortDirection);

                if (!isSortingInvalid(sortDirection)) {
                    holder.registerProblem(
                            sortDirection,
                            DQLBundle.message("inspection.sortingKeyword.usage.notAllowed"),
                            new DropElementQuickFix()
                    );
                }
            }
        };
    }

    private boolean isSortingInvalid(DQLSortDirection sortDirection) {
        List<PsiElement> parents = DQLUtil.getElementsUntilParent(sortDirection, DQLQueryStatement.class);
        if (!parents.isEmpty() && parents.getFirst() instanceof DQLQueryStatement statement
                && parents.get(1) instanceof DQLExpression expression) {
            DQLParameterObject parameter = statement.getParameter(expression);

            return parameter != null && parameter.getDefinition() != null &&
                    parameter.getDefinition().satisfies(DQLDataType.SORTING_EXPRESSION);
        }
        return false;
    }
}
