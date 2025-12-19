package pl.thedeem.intellij.dql.inspections.commands;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.inspections.fixes.AddMissingParametersQuickFix;
import pl.thedeem.intellij.dql.psi.DQLCommand;
import pl.thedeem.intellij.dql.psi.DQLCommandKeyword;
import pl.thedeem.intellij.dql.psi.DQLVisitor;

import java.util.Collection;
import java.util.List;

public class MissingCommandParametersInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitCommand(@NotNull DQLCommand command) {
                super.visitCommand(command);

                Collection<Parameter> missingParameters = command.getMissingRequiredParameters();
                DQLCommandKeyword keyword = command.getCommandKeyword();
                if (!missingParameters.isEmpty()) {
                    List<LocalQuickFix> fixes = missingParameters.stream()
                            .map(p -> (LocalQuickFix) new AddMissingParametersQuickFix(
                                    List.of(p),
                                    command.getTextRange().getEndOffset(),
                                    !command.getParameters().isEmpty())
                            )
                            .toList();

                    holder.registerProblem(
                            keyword,
                            DQLBundle.message(
                                    "inspection.command.body.missingRequiredParameters",
                                    command.getName(),
                                    DQLBundle.print(missingParameters.stream().map(Parameter::name).toList())
                            ),
                            fixes.toArray(new LocalQuickFix[0])
                    );
                }
            }
        };
    }
}
