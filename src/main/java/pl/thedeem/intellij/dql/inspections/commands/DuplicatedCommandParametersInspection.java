package pl.thedeem.intellij.dql.inspections.commands;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.definition.model.Parameter;
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
                for (MappedParameter parameter : command.getParameters()) {
                    Parameter definition = parameter.definition();
                    if (definition != null && !definition.variadic() && !alreadyProcessed.add(definition.name())) {
                        holder.registerProblem(
                                parameter.holder(),
                                DQLBundle.message("inspection.command.duplicatedParameters.duplicated", definition.name()),
                                new DropInvalidParameterQuickFix(parameter)
                        );
                    }
                }
            }
        };
    }
}
