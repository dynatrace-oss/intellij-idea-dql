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

public class JoinConditionValidatorTest extends LightPlatformCodeInsightFixture4TestCase {
    private JoinConditionValidator validator;
    private Parameter parameter;

    @Before
    public void createService() {
        validator = new JoinConditionValidator();
        parameter = new Parameter(
                "condition",
                "description",
                true,
                true,
                false,
                false,
                "none",
                List.of(),
                List.of("dql.parameterValueType.joinCondition"),
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
        DQLExpression expression = createExpression("field = 5");
        Parameter parameter = new Parameter(
                "condition",
                "description",
                true,
                true,
                false,
                false,
                "none",
                List.of(),
                List.of("any.other.parameter"),
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
    public void reportsNoIssuesForValidJoiningCondition() {
        DQLExpression expression = createExpression("left[test] == right[test]");

        List<DQLParameterValueTypesValidator.ValueIssue> issues = validator.validate(expression, parameter);

        assertEmpty("Should report no issues, but returned: " + issues, issues);
    }

    @Test
    public void reportsAnIssueWhenExpressionIsNotACondition() {
        DQLExpression expression = createExpression("left[test] + right[test]");

        List<DQLParameterValueTypesValidator.ValueIssue> issues = validator.validate(expression, parameter);

        assertNotEmpty(issues);
        assertContains("not a valid join condition", issues.getFirst().issue());
        assertEquals(expression, issues.getFirst().element());
    }

    @Test
    public void reportsAnIssueWhenLeftExpressionIsNotUsingLeftAlias() {
        DQLExpression expression = createExpression("other[test] == right[test]");

        List<DQLParameterValueTypesValidator.ValueIssue> issues = validator.validate(expression, parameter);

        assertNotEmpty(issues);
        assertContains("not a valid join condition", issues.getFirst().issue());
        assertEquals(expression, issues.getFirst().element());
    }

    @Test
    public void reportsAnIssueWhenRightExpressionIsNotUsingLeftAlias() {
        DQLExpression expression = createExpression("left[test] == other[test]");

        List<DQLParameterValueTypesValidator.ValueIssue> issues = validator.validate(expression, parameter);

        assertNotEmpty(issues);
        assertContains("not a valid join condition", issues.getFirst().issue());
        assertEquals(expression, issues.getFirst().element());
    }

    @Test
    public void allowsForComplexArrayExpressionsInBothOperands() {
        DQLExpression expression = createExpression("left[test][1] == right[test][other]");

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
