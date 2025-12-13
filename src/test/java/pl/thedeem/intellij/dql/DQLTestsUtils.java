package pl.thedeem.intellij.dql;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.definition.model.Command;
import pl.thedeem.intellij.dql.definition.model.Function;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.definition.model.Signature;

import java.util.List;

public class DQLTestsUtils {
    public static Command createCommand(
            @NotNull String name,
            @NotNull String type,
            @NotNull List<Parameter> parameters
    ) {
        return new Command(name, "empty description", "synopsis", false, parameters, type);
    }

    public static Function createFunction(
            @NotNull String name,
            @NotNull List<String> returnedValues,
            @NotNull List<Parameter> parameters
    ) {
        Signature signature = new Signature(parameters, returnedValues, false);
        return new Function(name, "empty description", "some category", "synopis", false, false, List.of(signature));
    }

    public static Parameter createParameter(
            @NotNull String name,
            @NotNull List<String> types,
            boolean required,
            @NotNull List<String> disallows,
            @NotNull List<String> enumValues,
            @Nullable String defaultValue,
            boolean requiresName
    ) {
        Parameter parameter = new Parameter(
                name,
                "description",
                required,
                requiresName,
                false,
                false,
                "none",
                types,
                List.of(),
                enumValues,
                defaultValue,
                null,
                null,
                null,
                null
        );
        parameter.setExcludes(disallows);
        return parameter;
    }

    public static Parameter createParameter(
            @NotNull String name,
            @NotNull List<String> types
    ) {
        return new Parameter(
                name,
                "description",
                true,
                true,
                false,
                false,
                "none",
                types,
                List.of(),
                List.of(),
                null,
                null,
                null,
                null,
                null
        );
    }
}
