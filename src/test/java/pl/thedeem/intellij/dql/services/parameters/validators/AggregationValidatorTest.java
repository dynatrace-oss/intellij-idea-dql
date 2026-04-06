package pl.thedeem.intellij.dql.services.parameters.validators;

import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.ServiceContainerUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pl.thedeem.intellij.dql.DQLTestsUtils;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.services.definition.DQLDefinitionService;
import pl.thedeem.intellij.dql.services.definition.model.Function;
import pl.thedeem.intellij.dql.services.definition.model.Parameter;
import pl.thedeem.intellij.dql.services.definition.model.Signature;
import pl.thedeem.intellij.dql.services.parameters.DQLParameterValueTypesValidator;
import pl.thedeem.intellij.dqlexpr.DQLExprFileType;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static pl.thedeem.intellij.dql.DQLTestsUtils.assertContains;

public class AggregationValidatorTest extends LightPlatformCodeInsightFixture4TestCase {
    private final static String AGGREGATION_TYPE = "aggregation";
    private final AggregationValidator validator = new AggregationValidator();
    private final DQLDefinitionService serviceMock = mock(DQLDefinitionService.class);
    private Parameter parameter;

    @Before
    public void setup() {
        parameter = new Parameter(
                "value",
                "description",
                true,
                true,
                false,
                false,
                "none",
                List.of(),
                List.of("dql.parameterValueType.expressionTimeseriesAggregation"),
                List.of(),
                null,
                null,
                null,
                null,
                null
        );

        mockFunctions(
                DQLTestsUtils.createFunction("max", List.of(AGGREGATION_TYPE), List.of()),
                DQLTestsUtils.createFunction("takeLast", List.of(AGGREGATION_TYPE), List.of()),
                DQLTestsUtils.createFunction("takeFirst", List.of(AGGREGATION_TYPE), List.of()),
                DQLTestsUtils.createFunction("unixMillisFromTimestamp", List.of(), List.of()),
                DQLTestsUtils.createFunction("coalesce", List.of(), List.of()),
                DQLTestsUtils.createFunction("toTimestamp", List.of(), List.of()),
                DQLTestsUtils.createFunction("now", List.of(), List.of())
        );

        ServiceContainerUtil.registerOrReplaceServiceInstance(
                getProject(),
                DQLDefinitionService.class,
                serviceMock,
                getTestRootDisposable()
        );
    }

    @After
    public void cleanup() {
        DQLDefinitionService service = myFixture.getProject().getService(DQLDefinitionService.class);
        service.invalidateCache();
    }

    @Test
    public void reportsNoIssuesForOtherParameterTypes() {
        DQLExpression expression = createExpression("false");
        Parameter parameter = new Parameter(
                "value",
                "description",
                true,
                true,
                false,
                false,
                "none",
                List.of(),
                List.of("any.other.parameterType"),
                List.of(),
                null,
                null,
                null,
                null,
                null
        );

        List<DQLParameterValueTypesValidator.ValueIssue> issues = validator.validate(expression, parameter);

        assertEmpty("Should report no issues, but returned: " + issues, issues);
    }

    @Test
    public void reportsNoIssuesForValidAggregationFunctionCall() {
        DQLExpression expression = createExpression("max(5)");

        List<DQLParameterValueTypesValidator.ValueIssue> issues = validator.validate(expression, parameter);

        assertEmpty("Should report no issues, but returned: " + issues, issues);
    }

    @Test
    public void reportsAnIssueWhenTheExpressionIsNotAFunctionCall() {
        DQLExpression expression = createExpression("max");

        List<DQLParameterValueTypesValidator.ValueIssue> issues = validator.validate(expression, parameter);

        assertNotEmpty(issues);
        assertContains("Invalid aggregation value", issues.getFirst().issue());
        assertEquals(expression, issues.getFirst().element());
    }

    @Test
    public void reportsAnIssueWhenTheExpressionIsAnotherFunctionCall() {
        DQLExpression expression = createExpression("toTimestamp()");

        List<DQLParameterValueTypesValidator.ValueIssue> issues = validator.validate(expression, parameter);

        assertNotEmpty(issues);
        assertContains("Invalid aggregation value", issues.getFirst().issue());
        assertEquals(expression, issues.getFirst().element());
    }

    @Test
    public void allowsAssigningAggregationResultToAField() {
        DQLExpression expression = createExpression("someField = max(5)");

        List<DQLParameterValueTypesValidator.ValueIssue> issues = validator.validate(expression, parameter);

        assertEmpty("Should report no issues, but returned: " + issues, issues);
    }

    @Test
    public void allowsUsingAggregationInArithmeticalExpressions() {
        DQLExpression expression = createExpression("billed_gib = sum(billed_bytes) / 1024 / 1024 / 1024");

        List<DQLParameterValueTypesValidator.ValueIssue> issues = validator.validate(expression, parameter);

        assertEmpty("Should report no issues, but returned: " + issues, issues);
    }

    @Test
    public void allowsUsingAggregationInConditionalExpressions() {
        DQLExpression expression = createExpression("isNotNull(sum(billed_bytes)) and true");

        List<DQLParameterValueTypesValidator.ValueIssue> issues = validator.validate(expression, parameter);

        assertEmpty("Should report no issues, but returned: " + issues, issues);
    }

    @Test
    public void allowsUsingAggregationInComparisonExpressions() {
        DQLExpression expression = createExpression("shouldBeTriggered = count() < 1");

        List<DQLParameterValueTypesValidator.ValueIssue> issues = validator.validate(expression, parameter);

        assertEmpty("Should report no issues, but returned: " + issues, issues);
    }

    @Test
    public void allowsUsingAggregationInAccessorExpressions() {
        DQLExpression expression = createExpression("collectArray(field)[2]");

        List<DQLParameterValueTypesValidator.ValueIssue> issues = validator.validate(expression, parameter);

        assertEmpty("Should report no issues, but returned: " + issues, issues);
    }

    @Test
    public void allowsUsingAggregationInParameterExpressions() {
        DQLExpression expression = createExpression("timeframe(from: takeMin(timeframe[start]), to: takeMax(timeframe[end]))");

        List<DQLParameterValueTypesValidator.ValueIssue> issues = validator.validate(expression, parameter);

        assertEmpty("Should report no issues, but returned: " + issues, issues);
    }

    @Test
    public void allowsUsingAggregationInUnaryExpressions() {
        DQLExpression expression = createExpression("not (sum(field) < 5)");

        List<DQLParameterValueTypesValidator.ValueIssue> issues = validator.validate(expression, parameter);

        assertEmpty("Should report no issues, but returned: " + issues, issues);
    }

    @Test
    public void allowsUsingAggregationInNegativeValueExpressions() {
        DQLExpression expression = createExpression("-sum(field)");

        List<DQLParameterValueTypesValidator.ValueIssue> issues = validator.validate(expression, parameter);

        assertEmpty("Should report no issues, but returned: " + issues, issues);
    }

    @Test
    public void allowsUsingNestedFunctionsToWrapAggregation() {
        DQLExpression expression = createExpression("toTimestamp(max(field))");

        List<DQLParameterValueTypesValidator.ValueIssue> issues = validator.validate(expression, parameter);

        assertEmpty("Should report no issues, but returned: " + issues, issues);
    }

    @Test
    public void allowsVeryComplexExpressionsWhenDefiningAggregation() {
        DQLExpression expression = createExpression("""
                if(
                    sum(totalAmount) / 100 * (sum(currentAmount) - sum(totalAmount))>=0,
                    concat("🟢", sum(totalAmount) / 100 * (sum(currentAmount) - sum(totalAmount)),"%"),
                    else: concat("🔴", sum(totalAmount) / 100 * (sum(currentAmount) - sum(totalAmount)),"%") )
                """);

        List<DQLParameterValueTypesValidator.ValueIssue> issues = validator.validate(expression, parameter);

        assertEmpty("Should report no issues, but returned: " + issues, issues);
    }

    @Test
    public void reportsNoIssuesForUnknownFunctions() {
        DQLExpression expression = createExpression("otherFunction(unknown(5))");

        List<DQLParameterValueTypesValidator.ValueIssue> issues = validator.validate(expression, parameter);

        assertEmpty("Should report no issues, but returned: " + issues, issues);
    }

    @Test
    public void reportsNoIssuesForDeeplyNestedAggregationFunctions() {
        DQLExpression expression = createExpression("""
                duration_minutes = (
                     (
                       unixMillisFromTimestamp(coalesce(toTimestamp(takeLast(valid_to)), now()))
                           - unixMillisFromTimestamp(toTimestamp(takeFirst(valid_from)))
                     ) / 60000
                   )
                """);

        List<DQLParameterValueTypesValidator.ValueIssue> issues = validator.validate(expression, parameter);

        assertEmpty("Should report no issues, but returned: " + issues, issues);
    }

    @Test
    public void reportsAnIssueForExpressionWithoutAggregationFunction() {
        DQLExpression expression = createExpression("""
                duration_minutes = (
                     (
                       unixMillisFromTimestamp(coalesce(toTimestamp((valid_to)), now()))
                           - unixMillisFromTimestamp(toTimestamp(valid_from))
                     ) / 60000
                   )
                """);

        List<DQLParameterValueTypesValidator.ValueIssue> issues = validator.validate(expression, parameter);

        assertNotEmpty(issues);
        assertContains("Invalid aggregation value", issues.getFirst().issue());
    }

    private @NotNull DQLExpression createExpression(@NotNull String content) {
        PsiFile file = myFixture.configureByText(DQLExprFileType.INSTANCE, content);
        DQLExpression functionExpression = PsiTreeUtil.findChildOfType(file, DQLExpression.class);
        assertNotNull("The provided DQL fragment does not contain valid expressions: " + content, functionExpression);
        return functionExpression;
    }

    private void mockFunctions(@NotNull Function... functions) {
        List<Function> aggregations = new ArrayList<>();
        for (Function function : functions) {
            when(serviceMock.getFunctionByName(function.name())).thenReturn(List.of(function));
            for (Signature signature : function.signatures()) {
                if (signature.outputs().contains(AGGREGATION_TYPE)) {
                    aggregations.add(function);
                }
            }
        }
        when(serviceMock.getFunctionCategoriesForParameterTypes(any())).thenReturn(List.of());
        when(serviceMock.getFunctionsByCategoryAndReturnType(any(), any())).thenReturn(aggregations);
    }
}
