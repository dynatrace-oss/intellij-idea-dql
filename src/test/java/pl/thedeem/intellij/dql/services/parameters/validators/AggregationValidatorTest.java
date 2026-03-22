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
import pl.thedeem.intellij.dql.services.parameters.DQLParameterValueTypesValidator;
import pl.thedeem.intellij.dqlexpr.DQLExprFileType;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static pl.thedeem.intellij.dql.DQLTestsUtils.assertContains;

public class AggregationValidatorTest extends LightPlatformCodeInsightFixture4TestCase {
    private AggregationValidator validator;
    private Parameter parameter;

    @Before
    public void createService() {
        validator = new AggregationValidator();
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
        DQLDefinitionService serviceMock = mock(DQLDefinitionService.class);
        Function functionDefinition = DQLTestsUtils.createFunction("max", List.of("dql.functionCategory.expression timeseries"), List.of());
        when(serviceMock.getFunctionByName("max")).thenReturn(List.of(functionDefinition));
        when(serviceMock.getFunctionByName("otherFunction")).thenReturn(List.of(DQLTestsUtils.createFunction("otherFunction", List.of(), List.of())));
        when(serviceMock.getFunctionCategoriesForParameterTypes(any())).thenReturn(List.of());
        when(serviceMock.getFunctionsByCategoryAndReturnType(any(), any())).thenReturn(List.of(functionDefinition));

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
        DQLExpression expression = createExpression("otherFunction()");

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
        DQLExpression expression = createExpression("otherFunction(max(5))");

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

    private @NotNull DQLExpression createExpression(@NotNull String content) {
        PsiFile file = myFixture.configureByText(DQLExprFileType.INSTANCE, content);
        DQLExpression functionExpression = PsiTreeUtil.findChildOfType(file, DQLExpression.class);
        assertNotNull("The provided DQL fragment does not contain valid expressions: " + content, functionExpression);
        return functionExpression;
    }
}
