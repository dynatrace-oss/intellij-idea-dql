package pl.thedeem.intellij.dql.inspections.fields;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.inspections.fixes.DropElementQuickFix;
import pl.thedeem.intellij.dql.inspections.fixes.SetFieldNameQuickFix;
import pl.thedeem.intellij.dql.psi.DQLCommand;
import pl.thedeem.intellij.dql.psi.DQLVisitor;
import pl.thedeem.intellij.dql.services.parameters.model.MappedParameter;
import pl.thedeem.intellij.dql.services.query.DQLFieldsCalculatorService;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DuplicatedFieldNamesInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            public void visitCommand(@NotNull DQLCommand command) {
                super.visitCommand(command);
                inspectParameters(command.getParameters(), holder);
            }
        };
    }

    private void inspectParameters(@NotNull List<MappedParameter> parameters, @NotNull ProblemsHolder holder) {
        DQLFieldsCalculatorService service = DQLFieldsCalculatorService.getInstance();
        for (MappedParameter parameter : parameters) {
            Collection<DQLFieldsCalculatorService.MappedField> definedFields = service.calculateDefinedFields(parameter);
            Set<String> seenNames = new HashSet<>();
            for (DQLFieldsCalculatorService.MappedField definedField : definedFields) {
                if (definedField.nameExpression() != null && !seenNames.add(definedField.name())) {
                    holder.registerProblem(
                            definedField.expression(),
                            DQLBundle.message("inspection.command.duplicatedFieldNames.duplicated", definedField.name()),
                            new DropElementQuickFix(),
                            new SetFieldNameQuickFix()
                    );
                }
            }

        }
    }
}
