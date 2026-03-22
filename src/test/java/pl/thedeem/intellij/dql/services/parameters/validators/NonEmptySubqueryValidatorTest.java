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

public class NonEmptySubqueryValidatorTest extends LightPlatformCodeInsightFixture4TestCase {
    private NonEmptySubqueryValidator validator;
    private Parameter parameter;

    @Before
    public void createService() {
        validator = new NonEmptySubqueryValidator();
        parameter = new Parameter(
                "value",
                "description",
                true,
                true,
                false,
                false,
                "none",
                List.of(),
                List.of("dql.parameterValueType.nonEmptyExecutionBlock"),
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
        DQLExpression expression = createExpression("something - else");
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
    public void reportsNoIssuesForNotEmptySubquery() {
        DQLExpression expression = createExpression("[ data record() ]");

        List<DQLParameterValueTypesValidator.ValueIssue> issues = validator.validate(expression, parameter);

        assertEmpty("Should report no issues, but returned: " + issues, issues);
    }

    @Test
    public void reportsAnIssueWhenElementIsNotASubquery() {
        DQLExpression expression = createExpression("isNotNull(5 > someFieldName)");

        List<DQLParameterValueTypesValidator.ValueIssue> issues = validator.validate(expression, parameter);

        assertNotEmpty(issues);
        assertContains("requires a non-empty execution block", issues.getFirst().issue());
        assertEquals(expression, issues.getFirst().element());
    }

    @Test
    public void reportsAnIssueWhenElementIsAnEmptySubquery() {
        DQLExpression expression = createExpression("[]");

        List<DQLParameterValueTypesValidator.ValueIssue> issues = validator.validate(expression, parameter);

        assertNotEmpty(issues);
        assertContains("requires a non-empty execution block", issues.getFirst().issue());
        assertEquals(expression, issues.getFirst().element());
    }

    private @NotNull DQLExpression createExpression(@NotNull String content) {
        PsiFile file = myFixture.configureByText(DQLExprFileType.INSTANCE, content);
        DQLExpression functionExpression = PsiTreeUtil.findChildOfType(file, DQLExpression.class);
        assertNotNull("The provided DQL fragment does not contain valid expressions: " + content, functionExpression);
        return functionExpression;
    }
}
