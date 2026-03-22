package pl.thedeem.intellij.dql.services.parameters.validators;

import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import pl.thedeem.intellij.dql.DQLTestsUtils;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.services.definition.model.Parameter;
import pl.thedeem.intellij.dql.services.parameters.DQLParameterValueTypesValidator;
import pl.thedeem.intellij.dqlexpr.DQLExprFileType;

import java.util.List;

import static pl.thedeem.intellij.dql.DQLTestsUtils.assertContains;

public class ValueValidatorTest extends LightPlatformCodeInsightFixture4TestCase {
    private ValueValidator validator;

    @Before
    public void createService() {
        validator = new ValueValidator();
    }

    @Test
    public void reportsNoIssuesForParametersWithoutValueRequirements() {
        DQLExpression expression = createExpression("5");
        Parameter parameter = DQLTestsUtils.createParameter("value", List.of());

        List<DQLParameterValueTypesValidator.ValueIssue> issues = validator.validate(expression, parameter);

        assertEmpty("Should report no issues, but returned: " + issues, issues);
    }

    @Test
    public void reportsNoIssuesWhenValueHasCorrectDataType() {
        DQLExpression expression = createExpression("true");
        Parameter parameter = DQLTestsUtils.createParameter("value", List.of("dql.dataType.boolean"));

        List<DQLParameterValueTypesValidator.ValueIssue> issues = validator.validate(expression, parameter);

        assertEmpty("Should report no issues, but returned: " + issues, issues);
    }

    @Test
    public void reportsAnIssueWhenValueHasOtherDataType() {
        DQLExpression expression = createExpression("5");
        Parameter parameter = DQLTestsUtils.createParameter("value", List.of("dql.dataType.boolean"));

        List<DQLParameterValueTypesValidator.ValueIssue> issues = validator.validate(expression, parameter);

        assertNotEmpty(issues);
        assertContains("has an invalid value", issues.getFirst().issue());
        assertEquals(expression, issues.getFirst().element());
    }

    @Test
    public void reportsNoIssuesWhenReturnedTypeIsNull() {
        DQLExpression expression = createExpression("null");
        Parameter parameter = DQLTestsUtils.createParameter("value", List.of("dql.dataType.boolean"));

        List<DQLParameterValueTypesValidator.ValueIssue> issues = validator.validate(expression, parameter);

        assertEmpty("Should report no issues, but returned: " + issues, issues);
    }

    @Test
    public void reportsNoIssuesForComplexExpressionsWhenTheDataTypeIsCorrect() {
        DQLExpression expression = createExpression("isNotNull(5 > 10 * field)");
        Parameter parameter = DQLTestsUtils.createParameter("value", List.of("dql.dataType.boolean"));

        List<DQLParameterValueTypesValidator.ValueIssue> issues = validator.validate(expression, parameter);

        assertEmpty("Should report no issues, but returned: " + issues, issues);
    }

    @Test
    public void reportsAnIssueForComplexExpressionsWhenTheDataTypeIsNotCorrect() {
        DQLExpression expression = createExpression("toDuration(someField)");
        Parameter parameter = DQLTestsUtils.createParameter("value", List.of("dql.dataType.boolean"));

        List<DQLParameterValueTypesValidator.ValueIssue> issues = validator.validate(expression, parameter);

        assertNotEmpty(issues);
        assertContains("has an invalid value", issues.getFirst().issue());
        assertEquals(expression, issues.getFirst().element());
    }

    private @NotNull DQLExpression createExpression(@NotNull String content) {
        PsiFile file = myFixture.configureByText(DQLExprFileType.INSTANCE, content);
        DQLExpression functionExpression = PsiTreeUtil.findChildOfType(file, DQLExpression.class);
        assertNotNull("The provided DQL fragment does not contain valid expressions: " + content, functionExpression);
        return functionExpression;
    }
}
