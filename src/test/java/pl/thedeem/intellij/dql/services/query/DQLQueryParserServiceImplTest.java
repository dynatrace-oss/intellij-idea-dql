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
    public void shouldNotSubstituteVariableDefinitionsInStringAndComments() {
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

    // getSubstitutedOffset tests (reverse direction: original offset → substituted offset)

    @Test
    public void shouldReturnSameSubstitutedOffsetForPositionsBeforeAnyVariable() {
        DQLQueryParserService.ParseResult result = service.getSubstitutedQuery(
                "fetch $x",
                getProject(),
                List.of(new DQLVariablesService.VariableDefinition("x", "logs", List.of()))
        );
        assertEquals(3, result.getSubstitutedOffset(3));
    }

    @Test
    public void shouldMapOriginalOffsetInsideVariableReferenceToEndOfSubstitutedValue() {
        // "fetch $x" → "fetch logs", $x at orig=6 len=2, substituted at sub=6 len=4
        DQLQueryParserService.ParseResult result = service.getSubstitutedQuery(
                "fetch $x",
                getProject(),
                List.of(new DQLVariablesService.VariableDefinition("x", "logs", List.of()))
        );
        // cursor at start of $x → end of "logs"
        assertEquals(10, result.getSubstitutedOffset(6));
        // cursor inside $x → end of "logs"
        assertEquals(10, result.getSubstitutedOffset(7));
    }

    @Test
    public void shouldAdjustSubstitutedOffsetForPositionsAfterVariableWhenSubstitutedIsLonger() {
        // "fetch $x | limit 10" → "fetch logs | limit 10"
        // $x at orig=6 len=2, substituted len=4, so positions after gain +2
        DQLQueryParserService.ParseResult result = service.getSubstitutedQuery(
                "fetch $x | limit 10",
                getProject(),
                List.of(new DQLVariablesService.VariableDefinition("x", "logs", List.of()))
        );
        assertEquals("fetch logs | limit 10", result.parsed());
        assertEquals(10, result.getSubstitutedOffset(8));
    }

    @Test
    public void shouldAdjustSubstitutedOffsetForPositionsAfterVariableWhenSubstitutedIsShorter() {
        // "fetch $severity | limit 10" → "fetch s | limit 10"
        // $severity at orig=6 len=9, substituted len=1, so positions after lose -8
        DQLQueryParserService.ParseResult result = service.getSubstitutedQuery(
                "fetch $severity | limit 10",
                getProject(),
                List.of(new DQLVariablesService.VariableDefinition("severity", "s", List.of()))
        );
        assertEquals("fetch s | limit 10", result.parsed());
        assertEquals(7, result.getSubstitutedOffset(15));
    }

    @Test
    public void shouldReturnNegativeOneForNegativeSubstitutedOffset() {
        DQLQueryParserService.ParseResult result = service.getSubstitutedQuery(
                "fetch $x",
                getProject(),
                List.of(new DQLVariablesService.VariableDefinition("x", "logs", List.of()))
        );
        assertEquals(-1, result.getSubstitutedOffset(-1));
    }

    @Test
    public void shouldCorrectlyMapSubstitutedOffsetAfterMultipleVariables() {
        // "fetch $x | limit $y" → "fetch logs | limit 100"
        // $x at orig=6 len=2, subst len=4 (+2 shift)
        // $y at orig=17 len=2, subst len=3 (+1 additional shift)
        // cursor at orig=19 (end of string) → subst=22 (end of "fetch logs | limit 100")
        DQLQueryParserService.ParseResult result = service.getSubstitutedQuery(
                "fetch $x | limit $y",
                getProject(),
                List.of(
                        new DQLVariablesService.VariableDefinition("x", "logs", List.of()),
                        new DQLVariablesService.VariableDefinition("y", "100", List.of())
                )
        );
        assertEquals("fetch logs | limit 100", result.parsed());
        assertEquals(22, result.getSubstitutedOffset(19));
    }

    @Test
    public void shouldBeInverseOfGetOriginalOffsetForPositionsOutsideVariables() {
        // getSubstitutedOffset and getOriginalOffset should be inverses for positions
        // that don't fall inside a substituted or original variable span
        DQLQueryParserService.ParseResult result = service.getSubstitutedQuery(
                "fetch $x | limit 10",
                getProject(),
                List.of(new DQLVariablesService.VariableDefinition("x", "logs", List.of()))
        );
        // position 8 in original → 10 in substituted → back to 8 in original
        int substituted = result.getSubstitutedOffset(8);
        assertEquals(8, result.getOriginalOffset(substituted));
    }
}
