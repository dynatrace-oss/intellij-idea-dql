package pl.thedeem.intellij.dql.services.parameters.validators;

import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.services.definition.model.Parameter;
import pl.thedeem.intellij.dql.services.parameters.DQLParameterValueTypesValidator;
import pl.thedeem.intellij.dqlexpr.DQLExprFileType;

import java.util.List;

import static pl.thedeem.intellij.dql.DQLTestsUtils.assertContains;

public class PrimitiveValueValidatorTest extends LightPlatformCodeInsightFixture4TestCase {
    private PrimitiveValueValidator validator;
    private Parameter parameter;

    @Before
    public void createService() {
        validator = new PrimitiveValueValidator();
        parameter = new Parameter(
                "value",
                "description",
                true,
                true,
                false,
                false,
                "none",
                List.of(),
                List.of("dql.parameterValueType.primitiveValue"),
                List.of(),
                null,
                null,
                null,
                null,
                null
        );
    }

    @Test
    public void reportsNoIssuesForOtherParameterTypes() {
        DQLExpression expression = createExpression("field == 5");
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
    public void reportsNoIssuesForPrimitiveElement() {
        DQLExpression expression = createExpression("\"some string\"");

        List<DQLParameterValueTypesValidator.ValueIssue> issues = validator.validate(expression, parameter);

        assertEmpty("Should report no issues, but returned: " + issues, issues);
    }

    @Test
    public void reportsAnIssueWhenExpressionIsComplex() {
        DQLExpression expression = createExpression("10 > 5");

        List<DQLParameterValueTypesValidator.ValueIssue> issues = validator.validate(expression, parameter);

        assertNotEmpty(issues);
        assertContains("requires a primitive value", issues.getFirst().issue());
        assertEquals(expression, issues.getFirst().element());
    }

    @Test
    public void reportsAnIssueWhenFieldIsProvidedButParameterDoesNotAllowIt() {
        DQLExpression expression = createExpression("field");

        List<DQLParameterValueTypesValidator.ValueIssue> issues = validator.validate(expression, parameter);

        assertNotEmpty(issues);
        assertContains("requires a primitive value", issues.getFirst().issue());
        assertEquals(expression, issues.getFirst().element());
    }

    @Test
    public void reportsNoIssueWhenFieldIsProvidedAndParameterAllowsIt() {
        DQLExpression expression = createExpression("field");

        Parameter parameter = new Parameter(
                "value",
                "description",
                true,
                true,
                false,
                false,
                "none",
                List.of(),
                List.of("dql.parameterValueType.primitiveValue", "dql.parameterValueType.fieldPattern"),
                List.of(),
                null,
                null,
                null,
                null,
                null
        );
        List<DQLParameterValueTypesValidator.ValueIssue> issues = validator.validate(expression, parameter);

        assertEmpty(issues);
    }

    private @NotNull DQLExpression createExpression(@NotNull String content) {
        PsiFile file = myFixture.configureByText(DQLExprFileType.INSTANCE, content);
        DQLExpression functionExpression = PsiTreeUtil.findChildOfType(file, DQLExpression.class);
        assertNotNull("The provided DQL fragment does not contain valid expressions: " + content, functionExpression);
        return functionExpression;
    }
}
