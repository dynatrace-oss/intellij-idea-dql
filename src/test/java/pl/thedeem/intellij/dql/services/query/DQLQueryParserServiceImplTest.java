package pl.thedeem.intellij.dql.services.query;

import com.intellij.testFramework.ServiceContainerUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase;
import org.junit.Before;
import org.junit.Test;
import pl.thedeem.intellij.dql.services.variables.DQLVariablesService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DQLQueryParserServiceImplTest extends LightPlatformCodeInsightFixture4TestCase {
    private final DQLQueryParserServiceImpl service = new DQLQueryParserServiceImpl();

    @Before
    public void setup() {
        DQLVariablesService variablesServiceMock = mock(DQLVariablesService.class);
        when(variablesServiceMock.findVariableDefinitionFiles(any(), any())).thenReturn(List.of());
        ServiceContainerUtil.registerOrReplaceServiceInstance(
                getProject(),
                DQLVariablesService.class,
                variablesServiceMock,
                getTestRootDisposable()
        );
    }

    @Test
    public void shouldReturnQueryUnchangedWhenNoVariablesAreDefined() {
        DQLQueryParserService.ParseResult result = service.getSubstitutedQuery(
                "fetch logs | filter true", getProject(), null
        );
        assertEquals("fetch logs | filter true", result.parsed());
        assertTrue(result.offsetMappings().isEmpty());
    }

    @Test
    public void shouldSubstituteSingleVariableFromExplicitDefinition() {
        DQLQueryParserService.ParseResult result = service.getSubstitutedQuery(
                "fetch $source",
                getProject(),
                List.of(new DQLVariablesService.VariableDefinition("source", "logs", List.of()))
        );
        assertEquals("fetch logs", result.parsed());
    }

    @Test
    public void shouldSubstituteMultipleVariablesFromExplicitDefinitions() {
        DQLQueryParserService.ParseResult result = service.getSubstitutedQuery(
                "fetch $source | limit $maxRecords",
                getProject(),
                List.of(
                        new DQLVariablesService.VariableDefinition("source", "logs", List.of()),
                        new DQLVariablesService.VariableDefinition("maxRecords", "100", List.of())
                )
        );
        assertEquals("fetch logs | limit 100", result.parsed());
    }

    @Test
    public void shouldReplaceUndefinedVariableWithNullLiteral() {
        DQLQueryParserService.ParseResult result = service.getSubstitutedQuery(
                "fetch logs | filter $undefinedVar == 1", getProject(), null
        );
        assertEquals("fetch logs | filter null == 1", result.parsed());
    }

    @Test
    public void shouldSubstituteSameVariableUsedMultipleTimes() {
        DQLQueryParserService.ParseResult result = service.getSubstitutedQuery(
                "fetch logs | filter $field == $field",
                getProject(),
                List.of(new DQLVariablesService.VariableDefinition("field", "severity", List.of()))
        );
        assertEquals("fetch logs | filter severity == severity", result.parsed());
        assertEquals(2, result.offsetMappings().size());
    }

    @Test
    public void shouldNotSubstituteVariableDefinitionsInStringAndCommends() {
        String expected = """
                /* $myVariable */
                data record(field = "$myVariable")
                """;
        DQLQueryParserService.ParseResult result = service.getSubstitutedQuery(
                expected,
                getProject(),
                List.of(new DQLVariablesService.VariableDefinition("myVariable", "value", List.of()))
        );
        assertEquals(expected, result.parsed());
    }

    @Test
    public void shouldProduceCorrectOffsetMappingWhenSubstitutedValueIsLonger() {
        DQLQueryParserService.ParseResult result = service.getSubstitutedQuery(
                "fetch $x",
                getProject(),
                List.of(new DQLVariablesService.VariableDefinition("x", "logs", List.of()))
        );
        assertEquals("fetch logs", result.parsed());
        assertEquals(1, result.offsetMappings().size());

        DQLQueryParserService.OffsetMapping mapping = result.offsetMappings().getFirst();
        assertEquals(6, mapping.originalStartOffset());
        assertEquals(6, mapping.substitutedStartOffset());
        assertEquals(2, mapping.originalLength());
        assertEquals(4, mapping.substitutedLength());
    }

    @Test
    public void shouldProduceCorrectOffsetMappingWhenSubstitutedValueIsShorter() {
        DQLQueryParserService.ParseResult result = service.getSubstitutedQuery(
                "fetch $severity",
                getProject(),
                List.of(new DQLVariablesService.VariableDefinition("severity", "s", List.of()))
        );
        assertEquals("fetch s", result.parsed());
        assertEquals(1, result.offsetMappings().size());

        DQLQueryParserService.OffsetMapping mapping = result.offsetMappings().getFirst();
        assertEquals(6, mapping.originalStartOffset());
        assertEquals(6, mapping.substitutedStartOffset());
        assertEquals(9, mapping.originalLength());
        assertEquals(1, mapping.substitutedLength());
    }

    @Test
    public void shouldReturnSameOffsetForPositionsBeforeAnyVariable() {
        DQLQueryParserService.ParseResult result = service.getSubstitutedQuery(
                "fetch $x",
                getProject(),
                List.of(new DQLVariablesService.VariableDefinition("x", "logs", List.of()))
        );
        assertEquals(3, result.getOriginalOffset(3));
    }

    @Test
    public void shouldMapOffsetInsideSubstitutedVariableToOriginalVariableStart() {
        DQLQueryParserService.ParseResult result = service.getSubstitutedQuery(
                "fetch $x",
                getProject(),
                List.of(new DQLVariablesService.VariableDefinition("x", "logs", List.of()))
        );
        assertEquals(6, result.getOriginalOffset(6));
        assertEquals(6, result.getOriginalOffset(8));
    }

    @Test
    public void shouldAdjustOffsetForPositionsAfterSubstitutedVariable() {
        DQLQueryParserService.ParseResult result = service.getSubstitutedQuery(
                "fetch $x | limit 10",
                getProject(),
                List.of(new DQLVariablesService.VariableDefinition("x", "logs", List.of()))
        );
        assertEquals("fetch logs | limit 10", result.parsed());
        assertEquals(8, result.getOriginalOffset(10));
    }

    @Test
    public void shouldReturnNegativeOneForOutOfBoundsOffset() {
        DQLQueryParserService.ParseResult result = service.getSubstitutedQuery(
                "fetch logs", getProject(), null
        );
        assertEquals(-1, result.getOriginalOffset(-1));
        assertEquals(-1, result.getOriginalOffset(100));
    }
}
