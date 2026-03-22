package pl.thedeem.intellij.dql.services.parameters;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.services.definition.model.Parameter;

import java.util.List;

public interface DQLParameterValueTypesValidator {
    static @NotNull DQLParameterValueTypesValidator getInstance() {
        return ApplicationManager.getApplication().getService(DQLParameterValueTypesValidator.class);
    }

    @NotNull List<ValueIssue> validate(@NotNull PsiElement parameter, @NotNull Parameter definition);

    record ValueIssue(@NotNull PsiElement element, @NotNull String issue) {
        @Override
        public @NotNull String toString() {
            return "ValueIssue{" +
                    "element=" + element +
                    ", issue='" + issue + '\'' +
                    '}';
        }
    }
}
