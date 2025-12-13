package pl.thedeem.intellij.dql.definition;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.psi.DQLExpression;

import java.util.List;

public interface DQLParametersCalculatorService {
    static @NotNull DQLParametersCalculatorService getInstance(@NotNull Project project) {
        return project.getService(DQLParametersCalculatorService.class);
    }

    @NotNull List<MappedParameter> mapParameters(@NotNull List<DQLExpression> definedParameters, @NotNull List<Parameter> definitions);
}
