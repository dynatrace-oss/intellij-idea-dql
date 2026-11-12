package pl.thedeem.intellij.dql.services.variables;

import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase;
import org.junit.Test;
import pl.thedeem.intellij.dql.DQLFileType;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class DQLVariablesRuntimeValuesTest extends LightPlatformCodeInsightFixture4TestCase {

    // 6.1 — RUNTIME_VALUES_KEY storage round-trip
    @Test
    public void runtimeValuesKeyRoundTrip() {
        PsiFile file = myFixture.configureByText(DQLFileType.INSTANCE, "data record(x = $var)");
        assertNull(file.getUserData(DQLVariablesService.RUNTIME_VALUES_KEY));

        Map<String, DQLVariablesService.VariableDefinition> stored = Map.of(
                "var", new DQLVariablesService.VariableDefinition("var", "\"hello\"", List.of())
        );
        file.putUserData(DQLVariablesService.RUNTIME_VALUES_KEY, stored);

        Map<String, DQLVariablesService.VariableDefinition> retrieved = file.getUserData(DQLVariablesService.RUNTIME_VALUES_KEY);
        assertNotNull(retrieved);
        assertEquals(1, retrieved.size());
        DQLVariablesService.VariableDefinition definition = retrieved.get("var");
        assertNotNull(definition);
        assertEquals("var", definition.name());
        assertEquals("\"hello\"", definition.value());
    }

    // 6.3 — getUndefinedVariables returns unresolved names; empty for fully-resolved file
    @Test
    public void getUndefinedVariablesReturnsUnresolvedName() {
        myFixture.configureByText(DQLFileType.INSTANCE, "data record(x = $missing)");

        Set<String> undefined = service().getUndefinedVariables(myFixture.getFile());

        assertEquals(Set.of("missing"), undefined);
    }

    @Test
    public void getUndefinedVariablesIsEmptyWhenAllVariablesAreResolved() {
        myFixture.addFileToProject("dql-variables.json", "{\"env\": \"prod\"}");
        myFixture.configureByText(DQLFileType.INSTANCE, "data record(x = $env)");

        Set<String> undefined = service().getUndefinedVariables(myFixture.getFile());

        assertTrue(undefined.isEmpty());
    }

    @Test
    public void getUndefinedVariablesIsEmptyForFileWithNoVariables() {
        myFixture.configureByText(DQLFileType.INSTANCE, "fetch logs");

        Set<String> undefined = service().getUndefinedVariables(myFixture.getFile());

        assertTrue(undefined.isEmpty());
    }

    // 6.4 — merge: runtime value included; file-based variable not overridden
    @Test
    public void getDefinedVariablesIncludesStoredRuntimeValues() {
        PsiFile file = myFixture.configureByText(DQLFileType.INSTANCE, "data record(x = $runtimeVar)");
        file.putUserData(DQLVariablesService.RUNTIME_VALUES_KEY, Map.of(
                "runtimeVar", new DQLVariablesService.VariableDefinition("runtimeVar", "\"injected\"", List.of())
        ));

        List<DQLVariablesService.VariableDefinition> defined = service().getDefinedVariables(file);

        assertEquals(1, defined.size());
        assertEquals("runtimeVar", defined.getFirst().name());
        assertEquals("\"injected\"", defined.getFirst().value());
    }

    @Test
    public void getDefinedVariablesFileBasedVariableWinsOverStoredRuntimeValue() {
        myFixture.addFileToProject("dql-variables.json", "{\"source\": \"logs\"}");
        PsiFile file = myFixture.configureByText(DQLFileType.INSTANCE, "data record(x = $source)");
        file.putUserData(DQLVariablesService.RUNTIME_VALUES_KEY, Map.of(
                "source", new DQLVariablesService.VariableDefinition("source", "\"runtime-override\"", List.of())
        ));

        List<DQLVariablesService.VariableDefinition> defined = service().getDefinedVariables(file);

        assertEquals(1, defined.size());
        assertEquals("source", defined.getFirst().name());
        assertEquals("\"logs\"", defined.getFirst().value());
    }

    // 6.5 — setting disabled path: without stored runtime values, unresolved variables are absent from definitions
    @Test
    public void getDefinedVariablesDoesNotIncludeUnresolvedVariablesWithoutStoredValues() {
        PsiFile file = myFixture.configureByText(DQLFileType.INSTANCE, "data record(x = $notDefined)");

        List<DQLVariablesService.VariableDefinition> defined = service().getDefinedVariables(file);

        assertTrue(defined.stream().noneMatch(d -> "notDefined".equals(d.name())));
    }

    private DQLVariablesService service() {
        return DQLVariablesService.getInstance(getProject());
    }
}
