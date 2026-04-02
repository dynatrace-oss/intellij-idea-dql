package pl.thedeem.intellij.dql.services.query;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.services.parameters.model.MappedParameter;

import java.util.Collection;
import java.util.List;

public interface DQLFieldsCalculatorService {
    static @NotNull DQLFieldsCalculatorService getInstance() {
        return ApplicationManager.getApplication().getService(DQLFieldsCalculatorService.class);
    }

    @NotNull String calculateFieldName(@Nullable Object... parts);

    @NotNull List<MappedField> calculateDefinedFields(@NotNull MappedParameter parameter);

    record SeparatedChildren(@NotNull Collection<?> children, @Nullable Object separator) {
    }

    record MappedField(
            @NotNull PsiElement expression,
            @NotNull String name,
            @Nullable PsiElement nameExpression
    ) {
    }
}
