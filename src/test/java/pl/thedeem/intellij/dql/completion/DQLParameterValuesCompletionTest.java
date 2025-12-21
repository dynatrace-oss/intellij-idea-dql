package pl.thedeem.intellij.dql.completion;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.testFramework.ServiceContainerUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import pl.thedeem.intellij.dql.DQLTestsUtils;
import pl.thedeem.intellij.dql.definition.DQLDefinitionService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DQLParameterValuesCompletionTest extends LightPlatformCodeInsightFixture4TestCase {
    @Mock
    private DQLDefinitionService serviceMock;

    @Override
    protected String getTestDataPath() {
        return "src/test/testData/completion/dql";
    }

    @Before
    public void createService() {
        serviceMock = mock(DQLDefinitionService.class);
        ServiceContainerUtil.registerOrReplaceServiceInstance(
                getProject(),
                DQLDefinitionService.class,
                serviceMock,
                getTestRootDisposable()
        );

        when(serviceMock.getCommandByName("data")).thenReturn(
                DQLTestsUtils.createCommand("data", "data_source", List.of(
                        DQLTestsUtils.createParameter("record", List.of("dql.dataType.record"), true, List.of(), List.of(), null, false)
                ))
        );
        when(serviceMock.getCommandByName("fetch")).thenReturn(
                DQLTestsUtils.createCommand("fetch", "data_source", List.of(
                        DQLTestsUtils.createParameter("dataObject", List.of("dql.dataType.dataObject"), true, List.of(), null, null, false),
                        DQLTestsUtils.createParameter("scanLimitGBytes", List.of("dql.dataType.long"), true, List.of(), null, null, false)
                ))
        );
        when(serviceMock.getFunctionByName("record")).thenReturn(List.of(
                DQLTestsUtils.createFunction("record", List.of("dql.dataType.record"), List.of(
                        DQLTestsUtils.createParameter("record", List.of("dql.dataType.record"), true, List.of(), List.of(), null, false)
                )))
        );
        when(serviceMock.getFunctionCategoriesForParameterTypes(any())).thenReturn(List.of());
    }

    @After
    public void cleanup() {
        DQLDefinitionService service = myFixture.getProject().getService(DQLDefinitionService.class);
        service.invalidateCache();
    }

    @Test
    public void testInsideExpressionWithNumbersShouldCompleteFunctionsWithMatchingOutputs() {
        when(serviceMock.getOperator(anyString())).thenReturn(
                DQLTestsUtils.createOperator("dql.operator.multiply", List.of("dql.dataType.long"), List.of(
                        DQLTestsUtils.createParameter("first", List.of("dql.dataType.long"), true, List.of(), null, null, false),
                        DQLTestsUtils.createParameter("second", List.of("dql.dataType.long"), true, List.of(), null, null, false)
                ))
        );
        when(serviceMock.getFunctionsByCategoryAndReturnType(any(), any())).thenReturn(List.of(
                DQLTestsUtils.createFunction("functionLong", List.of("dql.dataType.long"), List.of())
        ));

        myFixture.configureByFiles("part-of-arithmetic-expression.dql");
        myFixture.complete(CompletionType.BASIC);

        List<String> lookupElementStrings = myFixture.getLookupElementStrings();

        assertNotNull(lookupElementStrings);
        assertSameElements(lookupElementStrings, "functionLong");
    }

    @Test
    public void testInsideExpressionWithBooleansShouldCompleteFunctionsWithMatchingOutputsAndBooleans() {
        when(serviceMock.getOperator(anyString())).thenReturn(
                DQLTestsUtils.createOperator("dql.operator.and", List.of("dql.dataType.boolean"), List.of(
                        DQLTestsUtils.createParameter("first", List.of("dql.dataType.boolean"), true, List.of(), null, null, false),
                        DQLTestsUtils.createParameter("second", List.of("dql.dataType.boolean"), true, List.of(), null, null, false)
                ))
        );
        when(serviceMock.getFunctionsByCategoryAndReturnType(any(), any())).thenReturn(List.of(
                DQLTestsUtils.createFunction("functionBoolean", List.of("dql.dataType.boolean"), List.of())
        ));

        myFixture.configureByFiles("part-of-condition-expression.dql");
        myFixture.complete(CompletionType.BASIC);

        List<String> lookupElementStrings = myFixture.getLookupElementStrings();

        assertNotNull(lookupElementStrings);
        assertSameElements(lookupElementStrings, "functionBoolean", "true", "false", "not");
    }

    @Test
    public void testInsideCommandParameterShouldCompleteFunctionsWithMatchingOutputs() {
        when(serviceMock.getFunctionsByCategoryAndReturnType(any(), any())).thenReturn(List.of(
                DQLTestsUtils.createFunction("functionLong", List.of("dql.dataType.long"), List.of())
        ));

        myFixture.configureByFiles("part-of-command-parameter.dql");
        myFixture.complete(CompletionType.BASIC);

        List<String> lookupElementStrings = myFixture.getLookupElementStrings();

        assertNotNull(lookupElementStrings);
        assertSameElements(lookupElementStrings, "functionLong");
    }

    @Test
    public void testInsideFunctionParameterShouldCompleteFunctionsWithMatchingOutputs() {
        when(serviceMock.getFunctionByName("matchesPattern")).thenReturn(List.of(
                DQLTestsUtils.createFunction("matchesPattern", List.of("dql.dataType.boolean"), List.of(
                        DQLTestsUtils.createParameter("value", List.of("dql.dataType.string"), true, List.of(), null, null, false),
                        DQLTestsUtils.createParameter("pattern", List.of("dql.dataType.string"), true, List.of(), null, null, false)
                )))
        );
        when(serviceMock.getFunctionsByCategoryAndReturnType(any(), any())).thenReturn(List.of(
                DQLTestsUtils.createFunction("functionString", List.of("dql.dataType.string"), List.of())
        ));
        myFixture.configureByFiles("part-of-function-parameter.dql");
        myFixture.complete(CompletionType.BASIC);

        List<String> lookupElementStrings = myFixture.getLookupElementStrings();

        assertNotNull(lookupElementStrings);
        assertSameElements(lookupElementStrings, "functionString", "\"\"\"...\"\"\"", "\"...\"");
    }

    @Test
    public void testInsideEnumParameterShouldCompleteEnumValues() {
        when(serviceMock.getFunctionByName("matchesPattern")).thenReturn(List.of(
                DQLTestsUtils.createFunction("matchesPattern", List.of("dql.dataType.boolean"), List.of(
                        DQLTestsUtils.createParameter("value", List.of("dql.dataType.string"), true, List.of(), null, null, false),
                        DQLTestsUtils.createParameter("pattern", null, true, List.of(), List.of("value1", "value2", "value3"), null, false)
                )))
        );
        when(serviceMock.getFunctionsByCategoryAndReturnType(any(), any())).thenReturn(List.of(
                DQLTestsUtils.createFunction("functionString", List.of("dql.dataType.string"), List.of())
        ));
        myFixture.configureByFiles("part-of-function-parameter.dql");
        myFixture.complete(CompletionType.BASIC);

        List<String> lookupElementStrings = myFixture.getLookupElementStrings();

        assertNotNull(lookupElementStrings);
        assertSameElements(lookupElementStrings, "value1", "value2", "value3");
    }

    @Test
    public void testInsideNestedExpressionsShouldGetACorrectContext() {
        when(serviceMock.getOperator(anyString())).thenReturn(
                DQLTestsUtils.createOperator("dql.operator.and", List.of("dql.dataType.boolean"), List.of(
                        DQLTestsUtils.createParameter("firstAnd", List.of("dql.dataType.boolean"), true, List.of(), null, null, false),
                        DQLTestsUtils.createParameter("secondAnd", List.of("dql.dataType.boolean"), true, List.of(), null, null, false)
                ))
        );
        when(serviceMock.getOperator(anyString())).thenReturn(
                DQLTestsUtils.createOperator("dql.operator.subtract", List.of("dql.dataType.long"), List.of(
                        DQLTestsUtils.createParameter("firstSubtract", List.of("dql.dataType.long"), true, List.of(), null, null, false),
                        DQLTestsUtils.createParameter("secondSubtract", List.of("dql.dataType.long"), true, List.of(), null, null, false)
                ))
        );
        when(serviceMock.getOperator(anyString())).thenReturn(
                DQLTestsUtils.createOperator("dql.operator.lower", List.of("dql.dataType.boolean"), List.of(
                        DQLTestsUtils.createParameter("firstLower", List.of("dql.dataType.long"), true, List.of(), null, null, false),
                        DQLTestsUtils.createParameter("secondLower", List.of("dql.dataType.long"), true, List.of(), null, null, false)
                ))
        );
        when(serviceMock.getFunctionsByCategoryAndReturnType(any(), any())).thenReturn(List.of(
                DQLTestsUtils.createFunction("functionLong", List.of("dql.dataType.long"), List.of())
        ));

        myFixture.configureByFiles("part-of-condition-expression.dql");
        myFixture.complete(CompletionType.BASIC);

        List<String> lookupElementStrings = myFixture.getLookupElementStrings();

        assertNotNull(lookupElementStrings);
        assertSameElements(lookupElementStrings, "functionLong");
    }
}
