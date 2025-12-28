package pl.thedeem.intellij.dql.services.parameters;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.definition.model.Parameter;

import java.util.List;

public interface DQLParameterValueTypesValidator {
    static @NotNull DQLParameterValueTypesValidator getInstance(@NotNull Project project) {
        return project.getService(DQLParameterValueTypesValidator.class);
    }

    @NotNull List<ValueIssue> validate(@NotNull PsiElement parameter, @NotNull Parameter definition);

    record ValueIssue(@NotNull PsiElement element, @NotNull String issue) {
    }
}
