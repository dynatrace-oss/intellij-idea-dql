package pl.thedeem.intellij.dql.inspections.commands;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.DQLParameterDefinition;
import pl.thedeem.intellij.dql.definition.DQLParameterObject;
import pl.thedeem.intellij.dql.inspections.fixes.DropInvalidParameterQuickFix;
import pl.thedeem.intellij.dql.psi.DQLQueryStatement;
import pl.thedeem.intellij.dql.psi.DQLVisitor;

import java.util.HashSet;
import java.util.Set;

public class DuplicatedCommandParametersInspection extends LocalInspectionTool {
  @Override
  public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new DQLVisitor() {
      @Override
      public void visitQueryStatement(@NotNull DQLQueryStatement command) {
        super.visitQueryStatement(command);

        Set<String> alreadyProcessed = new HashSet<>();
        for (DQLParameterObject parameter : command.getParameters()) {
          DQLParameterDefinition definition = parameter.getDefinition();
          if (definition != null && !definition.isRepetitive() && !alreadyProcessed.add(definition.name)) {
            holder.registerProblem(
                parameter.getExpression(),
                DQLBundle.message("inspection.command.duplicatedParameters.duplicated", definition.name),
                new DropInvalidParameterQuickFix(parameter)
            );
          }
        }
      }
    };
  }
}
