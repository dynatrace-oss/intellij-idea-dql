package pl.thedeem.intellij.dpl;

import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.definition.model.ExpressionDescription;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DPLTestsUtils {
    public static @NotNull Map<String, ExpressionDescription> createMockedCommands(@NotNull ExpressionDescription... definitions) {
        return Stream.of(definitions).collect(Collectors.toMap(ExpressionDescription::name, Function.identity()));
    }
}
