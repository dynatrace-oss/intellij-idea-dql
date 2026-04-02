package pl.thedeem.intellij.dql.services.query;

import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import pl.thedeem.intellij.dql.psi.DQLCommand;
import pl.thedeem.intellij.dql.services.parameters.model.MappedParameter;
import pl.thedeem.intellij.dqlpart.DQLPartFileType;

import java.util.List;

public class DQLFieldsCalculatorServiceImplFieldDefinitionsTest extends LightPlatformCodeInsightFixture4TestCase {
    private final DQLFieldsCalculatorServiceImpl service = new DQLFieldsCalculatorServiceImpl();

    @Test
    public void returnsFieldNameWhenItsSimpleReadExpression() {
        MappedParameter parameter = parse(/* language=DQLExpr */ "someFieldName");

        List<DQLFieldsCalculatorService.MappedField> fields = service.calculateDefinedFields(parameter);

        assertFieldsEqual(fields, List.of(
                new FieldMatcher("someFieldName", false, "someFieldName")
        ));
    }

    @Test
    public void returnsAssignedNameWhenItsWriteExpression() {
        MappedParameter parameter = parse(/* language=DQLExpr */ "newFieldName = someFieldName");

        List<DQLFieldsCalculatorService.MappedField> fields = service.calculateDefinedFields(parameter);

        assertFieldsEqual(fields, List.of(
                new FieldMatcher("newFieldName", true, "someFieldName")
        ));
    }

    @Test
    public void returnsCalculatedNameForComplexExpression() {
        MappedParameter parameter = parse(/* language=DQLExpr */ "5 * 10 > 15");

        List<DQLFieldsCalculatorService.MappedField> fields = service.calculateDefinedFields(parameter);

        assertFieldsEqual(fields, List.of(
                new FieldMatcher("5*10>15", false, "5 * 10 > 15")
        ));
    }

    @Test
    public void returnsFieldsNestedInBrackets() {
        MappedParameter parameter = parse(/* language=DQLExpr */ "{field1 = 5, field2}, field3, field4");

        List<DQLFieldsCalculatorService.MappedField> fields = service.calculateDefinedFields(parameter);

        assertFieldsEqual(fields, List.of(
                new FieldMatcher("field1", true, "5"),
                new FieldMatcher("field2", false, "field2"),
                new FieldMatcher("field3", false, "field3"),
                new FieldMatcher("field4", false, "field4")
        ));
    }

    @Test
    public void returnsListOfFieldsWhenMultipleExpressionsAreDefined() {
        MappedParameter parameter = parse(/* language=DQLExpr */ "someFieldName, otherField = 5 * 10 > 15, yetAnotherField = myField");

        List<DQLFieldsCalculatorService.MappedField> fields = service.calculateDefinedFields(parameter);

        assertFieldsEqual(fields, List.of(
                new FieldMatcher("someFieldName", false, "someFieldName"),
                new FieldMatcher("otherField", true, "5 * 10 > 15"),
                new FieldMatcher("yetAnotherField", true, "myField")
        ));
    }

    @Test
    public void supportsAliasedExpressions() {
        MappedParameter parameter = parse(/* language=DQLExpr */ "5 * 10 > 15 and true, alias: myName");

        List<DQLFieldsCalculatorService.MappedField> fields = service.calculateDefinedFields(parameter);

        assertFieldsEqual(fields, List.of(
                new FieldMatcher("myName", true, "5 * 10 > 15 and true")
        ));
    }

    @Test
    public void supportsAliasOnLeftSideWhenSupportedExpression() {
        MappedParameter parameter = parse(/* language=DQLExpr */ "alias: field2, field1");

        List<DQLFieldsCalculatorService.MappedField> fields = service.calculateDefinedFields(parameter);

        assertFieldsEqual(fields, List.of(
                new FieldMatcher("field2", true, "field1")
        ));
    }

    @Test
    public void assignsAliasToBracketExpressionWhenItContainsOneField() {
        MappedParameter parameter = parse(/* language=DQLExpr */ "{field1}, alias: field2");

        List<DQLFieldsCalculatorService.MappedField> fields = service.calculateDefinedFields(parameter);

        assertFieldsEqual(fields, List.of(
                new FieldMatcher("field2", true, "{field1}")
        ));
    }

    @Test
    public void doesNotAssignAliasToBracketExpressionWhenItContainsMultipleFields() {
        MappedParameter parameter = parse(/* language=DQLExpr */ "{field1, field3}, alias: field2");

        List<DQLFieldsCalculatorService.MappedField> fields = service.calculateDefinedFields(parameter);

        assertFieldsEqual(fields, List.of(
                new FieldMatcher("field1", false, "field1"),
                new FieldMatcher("field3", false, "field3")
        ));
    }

    @Test
    public void doesNotAssignAliasToAssignmentExpressionEvenIfThereIsFieldOnTheRight() {
        MappedParameter parameter = parse(/* language=DQLExpr */ "field1 = field3, alias: field2, field4");

        List<DQLFieldsCalculatorService.MappedField> fields = service.calculateDefinedFields(parameter);

        assertFieldsEqual(fields, List.of(
                new FieldMatcher("field1", true, "field3"),
                new FieldMatcher("field4", false, "field4")
        ));
    }

    @Test
    public void assignsAliasesToMultipleExpressions() {
        MappedParameter parameter = parse(/* language=DQLExpr */ "11 * 12 / 55 > 1, alias: math, true and false or not true, alias: condition");

        List<DQLFieldsCalculatorService.MappedField> fields = service.calculateDefinedFields(parameter);

        assertFieldsEqual(fields, List.of(
                new FieldMatcher("math", true, "11 * 12 / 55 > 1"),
                new FieldMatcher("condition", true, "true and false or not true")
        ));
    }

    @Test
    public void calculatesHigherPrioritiesForRightSidedAliases() {
        MappedParameter parameter = parse(/* language=DQLExpr */ "alias: alias1, (field and field), alias: alias2, false");

        List<DQLFieldsCalculatorService.MappedField> fields = service.calculateDefinedFields(parameter);

        assertFieldsEqual(fields, List.of(
                new FieldMatcher("alias2", true, "(field and field)"),
                new FieldMatcher("false", false, "false")
        ));
    }

    @Test
    public void assignsAliasesIfTheyDoNotConflictWithEachOther() {
        MappedParameter parameter = parse(/* language=DQLExpr */ "alias: alias1, (true or false), false, alias: alias2");

        List<DQLFieldsCalculatorService.MappedField> fields = service.calculateDefinedFields(parameter);

        assertFieldsEqual(fields, List.of(
                new FieldMatcher("alias1", true, "(true or false)"),
                new FieldMatcher("alias2", true, "false")
        ));
    }

    @Test
    public void setsAliasBoundaryToParentingBracket() {
        MappedParameter parameter = parse(/* language=DQLExpr */ "{alias: alias1, 11}, alias: alias2");

        List<DQLFieldsCalculatorService.MappedField> fields = service.calculateDefinedFields(parameter);

        assertFieldsEqual(fields, List.of(
                new FieldMatcher("alias1", true, "11")
        ));
    }

    @Test
    public void assignsAliasesOnBothSides() {
        MappedParameter parameter = parse(/* language=DQLExpr */ " 11, alias: lower, alias: higher, 55");

        List<DQLFieldsCalculatorService.MappedField> fields = service.calculateDefinedFields(parameter);

        assertFieldsEqual(fields, List.of(
                new FieldMatcher("lower", true, "11"),
                new FieldMatcher("higher", true, "55")
        ));
    }

    private void assertFieldsEqual(@NotNull List<DQLFieldsCalculatorService.MappedField> fields, @NotNull List<FieldMatcher> expected) {
        List<FieldMatcher> actual = fields.stream().map(f -> new FieldMatcher(f.name(), f.nameExpression() != null, f.expression().getText())).toList();

        assertOrderedEquals("Fields definitions are not correct", actual, expected);
    }

    private @NotNull MappedParameter parse(@NotNull String content) {
        PsiFile file = myFixture.configureByText(DQLPartFileType.INSTANCE, "fields " + content);
        DQLCommand command = PsiTreeUtil.findChildOfType(file, DQLCommand.class);
        assertNotNull("Expected DQLCommand in: " + content, command);
        return command.getParameters().getFirst();
    }

    private record FieldMatcher(
            @NotNull String name,
            boolean nameAssigned,
            @NotNull String expression
    ) {
    }
}
