package pl.thedeem.intellij.dql.inspections.migrations;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.inspections.fixes.RenameFileQuickFix;
import pl.thedeem.intellij.dql.psi.DQLQuery;
import pl.thedeem.intellij.dql.psi.DQLVisitor;

public class PartialFileMigrationInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {

            @Override
            public void visitQuery(@NotNull DQLQuery query) {
                super.visitQuery(query);

                String fileName = query.getContainingFile().getName();
                if (StringUtil.endsWithIgnoreCase(fileName, ".partial.dql")) {
                    holder.registerProblem(
                            query,
                            DQLBundle.message("inspection.deprecatedPartialFile.issueDetected"),
                            new RenameFileQuickFix(fileName.replaceFirst("(?i)\\.partial\\.dql$", ".dqlpart"))
                    );
                }
            }
        };
    }
}
