package pl.thedeem.intellij.dql.services.query;

import com.intellij.psi.PsiElement;
import org.junit.Test;
import pl.thedeem.intellij.dql.psi.elements.BaseTypedElement;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DQLFieldsCalculatorServiceImplFieldNamesTest {
    private final DQLFieldsCalculatorServiceImpl service = new DQLFieldsCalculatorServiceImpl();

    @Test
    public void returnsEmptyStringWhenNoPartsGiven() {
        assertEquals("", service.calculateFieldName());
    }

    @Test
    public void skipsNullPart() {
        assertEquals("", service.calculateFieldName((Object) null));
    }

    @Test
    public void returnsTrimmedStringPart() {
        assertEquals("hello", service.calculateFieldName("  hello  "));
    }

    @Test
    public void concatenatesMultipleStringParts() {
        assertEquals("abc", service.calculateFieldName("a", "b", "c"));
    }

    @Test
    public void skipsNullPartsMixedWithOtherParts() {
        assertEquals("ab", service.calculateFieldName("a", null, "b"));
    }

    @Test
    public void delegatesToPsiElementGetText() {
        PsiElement element = mock(PsiElement.class);
        when(element.getText()).thenReturn("  fieldText  ");
        assertEquals("fieldText", service.calculateFieldName(element));
    }

    @Test
    public void delegatesToBaseTypedElementGetFieldName() {
        BaseTypedElement element = mock(BaseTypedElement.class);
        when(element.getFieldName()).thenReturn("computedFieldName");
        assertEquals("computedFieldName", service.calculateFieldName(element));
    }

    @Test
    public void prefersBaseTypedElementGetFieldNameWhenImplementingBothInterfaces() {
        interface BaseTypedPsiElement extends BaseTypedElement, PsiElement {
        }
        BaseTypedPsiElement element = mock(BaseTypedPsiElement.class);
        when(element.getFieldName()).thenReturn("fromFieldName");
        when(element.getText()).thenReturn("fromGetText");
        assertEquals("fromFieldName", service.calculateFieldName(element));
    }

    @Test
    public void joinsCollectionElementsWithComma() {
        assertEquals("a,b,c", service.calculateFieldName(List.of("a", "b", "c")));
    }

    @Test
    public void joinsSeparatedChildrenWithCommaWhenSeparatorIsNull() {
        DQLFieldsCalculatorService.SeparatedChildren children =
                new DQLFieldsCalculatorService.SeparatedChildren(List.of("x", "y"), null);
        assertEquals("x,y", service.calculateFieldName(children));
    }

    @Test
    public void joinsSeparatedChildrenWithGivenStringSeparator() {
        DQLFieldsCalculatorService.SeparatedChildren children =
                new DQLFieldsCalculatorService.SeparatedChildren(List.of("a", "b", "c"), ",");
        assertEquals("a,b,c", service.calculateFieldName(children));
    }

    @Test
    public void joinsSeparatedChildrenUsingBaseTypedElementSeparatorFieldName() {
        BaseTypedElement separator = mock(BaseTypedElement.class);
        when(separator.getFieldName()).thenReturn(" + ");
        DQLFieldsCalculatorService.SeparatedChildren children =
                new DQLFieldsCalculatorService.SeparatedChildren(List.of("left", "right"), separator);
        assertEquals("left + right", service.calculateFieldName(children));
    }

    @Test
    public void buildsFunctionLikeFieldName() {
        DQLFieldsCalculatorService.SeparatedChildren args =
                new DQLFieldsCalculatorService.SeparatedChildren(List.of("arg1", "arg2"), ", ");
        assertEquals("myFunc(arg1,arg2)", service.calculateFieldName("myFunc", "(", args, ")"));
    }

    @Test
    public void buildsBinaryOperatorFieldNameFromPsiElementParts() {
        PsiElement left = mock(PsiElement.class);
        when(left.getText()).thenReturn("fieldA");
        PsiElement op = mock(PsiElement.class);
        when(op.getText()).thenReturn(" > ");
        PsiElement right = mock(PsiElement.class);
        when(right.getText()).thenReturn("100");
        assertEquals("fieldA>100", service.calculateFieldName(left, op, right));
    }
}
