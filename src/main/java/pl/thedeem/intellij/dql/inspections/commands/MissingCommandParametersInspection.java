package pl.thedeem.intellij.dql.inspections.commands;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.DQLParameterDefinition;
import pl.thedeem.intellij.dql.inspections.fixes.AddMissingParametersQuickFix;
import pl.thedeem.intellij.dql.psi.DQLQueryStatement;
import pl.thedeem.intellij.dql.psi.DQLQueryStatementKeyword;
import pl.thedeem.intellij.dql.psi.DQLVisitor;

import java.util.ArrayList;
import java.util.List;

public class MissingCommandParametersInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitQueryStatement(@NotNull DQLQueryStatement command) {
                super.visitQueryStatement(command);

                List<DQLParameterDefinition> missingExclusive = command.getMissingExclusiveRequiredParameters();
                DQLQueryStatementKeyword keyword = command.getQueryStatementKeyword();
                if (!missingExclusive.isEmpty()) {
                    List<LocalQuickFix> fixes = new ArrayList<>();
                    for (DQLParameterDefinition parameter : missingExclusive) {
                        fixes.add(new AddMissingParametersQuickFix(List.of(parameter), command.getTextRange().getEndOffset(), false));
                    }

                    holder.registerProblem(
                            keyword,
                            DQLBundle.message(
                                    "inspection.command.body.missingExclusiveParameters",
                                    command.getName(),
                                    DQLBundle.print(missingExclusive.stream().map(p -> p.name).toList())
                            ),
                            fixes.toArray(new LocalQuickFix[0])
                    );
                }

                List<DQLParameterDefinition> missingRequiredParameters = command.getMissingRequiredParameters();
                if (!missingRequiredParameters.isEmpty()) {
                    holder.registerProblem(
                            keyword,
                            DQLBundle.message("inspection.command.body.missingRequiredParameters",
                                    DQLBundle.print(missingRequiredParameters.stream().map(p -> p.name).toList())
                            ),
                            new AddMissingParametersQuickFix(missingRequiredParameters, command.getTextRange().getEndOffset(), false)
                    );
                }
            }
        };
    }
}
