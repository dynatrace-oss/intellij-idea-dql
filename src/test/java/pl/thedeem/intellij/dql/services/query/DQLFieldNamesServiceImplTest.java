package pl.thedeem.intellij.dql.services.query;

import com.intellij.psi.PsiElement;
import org.junit.Test;
import pl.thedeem.intellij.dql.psi.elements.BaseTypedElement;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DQLFieldNamesServiceImplTest {
    private final DQLFieldNamesServiceImpl service = new DQLFieldNamesServiceImpl();

    @Test
    public void shouldReturnEmptyStringWhenNoPartsGiven() {
        assertEquals("", service.calculateFieldName());
    }

    @Test
    public void shouldSkipNullPart() {
        assertEquals("", service.calculateFieldName((Object) null));
    }

    @Test
    public void shouldReturnTrimmedStringPart() {
        assertEquals("hello", service.calculateFieldName("  hello  "));
    }

    @Test
    public void shouldConcatenateMultipleStringParts() {
        assertEquals("abc", service.calculateFieldName("a", "b", "c"));
    }

    @Test
    public void shouldSkipNullPartsMixedWithOtherParts() {
        assertEquals("ab", service.calculateFieldName("a", null, "b"));
    }

    @Test
    public void shouldDelegateToPsiElementGetText() {
        PsiElement element = mock(PsiElement.class);
        when(element.getText()).thenReturn("  fieldText  ");
        assertEquals("fieldText", service.calculateFieldName(element));
    }

    @Test
    public void shouldDelegateToBaseTypedElementGetFieldName() {
        BaseTypedElement element = mock(BaseTypedElement.class);
        when(element.getFieldName()).thenReturn("computedFieldName");
        assertEquals("computedFieldName", service.calculateFieldName(element));
    }

    @Test
    public void shouldPreferBaseTypedElementGetFieldNameWhenImplementingBothInterfaces() {
        interface BaseTypedPsiElement extends BaseTypedElement, PsiElement {
        }
        BaseTypedPsiElement element = mock(BaseTypedPsiElement.class);
        when(element.getFieldName()).thenReturn("fromFieldName");
        when(element.getText()).thenReturn("fromGetText");
        assertEquals("fromFieldName", service.calculateFieldName(element));
    }

    @Test
    public void shouldJoinCollectionElementsWithComma() {
        assertEquals("a,b,c", service.calculateFieldName(List.of("a", "b", "c")));
    }

    @Test
    public void shouldJoinSeparatedChildrenWithCommaWhenSeparatorIsNull() {
        DQLFieldNamesService.SeparatedChildren children =
                new DQLFieldNamesService.SeparatedChildren(List.of("x", "y"), null);
        assertEquals("x,y", service.calculateFieldName(children));
    }

    @Test
    public void shouldJoinSeparatedChildrenWithGivenStringSeparator() {
        DQLFieldNamesService.SeparatedChildren children =
                new DQLFieldNamesService.SeparatedChildren(List.of("a", "b", "c"), ",");
        assertEquals("a,b,c", service.calculateFieldName(children));
    }

    @Test
    public void shouldJoinSeparatedChildrenUsingBaseTypedElementSeparatorFieldName() {
        BaseTypedElement separator = mock(BaseTypedElement.class);
        when(separator.getFieldName()).thenReturn(" + ");
        DQLFieldNamesService.SeparatedChildren children =
                new DQLFieldNamesService.SeparatedChildren(List.of("left", "right"), separator);
        assertEquals("left + right", service.calculateFieldName(children));
    }

    @Test
    public void shouldBuildFunctionLikeFieldName() {
        DQLFieldNamesService.SeparatedChildren args =
                new DQLFieldNamesService.SeparatedChildren(List.of("arg1", "arg2"), ", ");
        assertEquals("myFunc(arg1,arg2)", service.calculateFieldName("myFunc", "(", args, ")"));
    }

    @Test
    public void shouldBuildBinaryOperatorFieldNameFromPsiElementParts() {
        PsiElement left = mock(PsiElement.class);
        when(left.getText()).thenReturn("fieldA");
        PsiElement op = mock(PsiElement.class);
        when(op.getText()).thenReturn(" > ");
        PsiElement right = mock(PsiElement.class);
        when(right.getText()).thenReturn("100");
        assertEquals("fieldA>100", service.calculateFieldName(left, op, right));
    }
}
