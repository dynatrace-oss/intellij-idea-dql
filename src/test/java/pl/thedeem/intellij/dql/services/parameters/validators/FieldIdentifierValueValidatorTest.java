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

public class FieldIdentifierValueValidatorTest extends LightPlatformCodeInsightFixture4TestCase {
    private FieldIdentifierValidator validator;
    private Parameter parameter;

    @Before
    public void createService() {
        validator = new FieldIdentifierValidator();
        parameter = new Parameter(
                "value",
                "description",
                true,
                true,
                false,
                false,
                "none",
                List.of(),
                List.of("dql.parameterValueType.identifierForFieldOnRootLevel"),
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
        DQLExpression expression = createExpression("otherField == someField");
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
    public void reportsNoIssuesForFieldIdentifierValue() {
        DQLExpression expression = createExpression("fieldName");

        List<DQLParameterValueTypesValidator.ValueIssue> issues = validator.validate(expression, parameter);

        assertEmpty("Should report no issues, but returned: " + issues, issues);
    }

    @Test
    public void reportsNoIssuesForNegativeFieldValue() {
        DQLExpression expression = createExpression("-fieldName");

        List<DQLParameterValueTypesValidator.ValueIssue> issues = validator.validate(expression, parameter);

        assertEmpty("Should report no issues, but returned: " + issues, issues);
    }

    @Test
    public void reportsNoIssuesForFieldAssignmentValue() {
        DQLExpression expression = createExpression("fieldName = 5 * 10");

        List<DQLParameterValueTypesValidator.ValueIssue> issues = validator.validate(expression, parameter);

        assertEmpty("Should report no issues, but returned: " + issues, issues);
    }

    @Test
    public void reportsNoIssuesForFieldAccessorValue() {
        DQLExpression expression = createExpression("fieldName[5]");

        List<DQLParameterValueTypesValidator.ValueIssue> issues = validator.validate(expression, parameter);

        assertEmpty("Should report no issues, but returned: " + issues, issues);
    }

    @Test
    public void reportsNoIssuesForFieldsListValue() {
        DQLExpression expression = createExpression("{ field1, field2 }");

        List<DQLParameterValueTypesValidator.ValueIssue> issues = validator.validate(expression, parameter);

        assertEmpty("Should report no issues, but returned: " + issues, issues);
    }

    @Test
    public void reportsNoIssueForStringsWhenParameterAllowsPatterns() {
        DQLExpression expression = createExpression("\"string.fieldName\"");
        Parameter parameter = new Parameter(
                "value",
                "description",
                true,
                true,
                false,
                false,
                "none",
                List.of(),
                List.of("dql.parameterValueType.fieldPattern"),
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
    public void reportsAnIssueForStringsWhenParameterDoesNotAllowPatterns() {
        DQLExpression expression = createExpression("\"string.fieldName\"");

        List<DQLParameterValueTypesValidator.ValueIssue> issues = validator.validate(expression, parameter);

        assertNotEmpty(issues);
        assertContains("expects a field identifier", issues.getFirst().issue());
        assertEquals(expression, issues.getFirst().element());
    }

    private @NotNull DQLExpression createExpression(@NotNull String content) {
        PsiFile file = myFixture.configureByText(DQLExprFileType.INSTANCE, content);
        DQLExpression functionExpression = PsiTreeUtil.findChildOfType(file, DQLExpression.class);
        assertNotNull("The provided DQL fragment does not contain valid expressions: " + content, functionExpression);
        return functionExpression;
    }
}
