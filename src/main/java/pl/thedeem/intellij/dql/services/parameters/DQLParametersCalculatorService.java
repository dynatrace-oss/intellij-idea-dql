package pl.thedeem.intellij.dql.services.parameters;

import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.psi.elements.DQLParametersOwner;
import pl.thedeem.intellij.dql.services.definition.model.Parameter;
import pl.thedeem.intellij.dql.services.parameters.model.MappedParameter;

import java.util.List;

public interface DQLParametersCalculatorService {
    static @NotNull DQLParametersCalculatorService getInstance() {
        return ApplicationManager.getApplication().getService(DQLParametersCalculatorService.class);
    }

    @NotNull List<MappedParameter> mapParameters(@NotNull DQLParametersOwner holder, @NotNull List<Parameter> definitions);
}
