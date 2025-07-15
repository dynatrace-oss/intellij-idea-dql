package pl.thedeem.intellij.dql.indexing.references;

import com.intellij.psi.PsiReference;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase;
import org.junit.Test;
import pl.thedeem.intellij.dql.psi.DQLAssignExpression;
import pl.thedeem.intellij.dql.psi.DQLFieldExpression;

public class DQLFieldReferencesTest extends LightPlatformCodeInsightFixture4TestCase {
   @Override
   protected String getTestDataPath() {
      return "src/test/testData/indexing/references";
   }

   @Test
   public void testFieldReferenceWhenFieldHasAssignedValue() {
      PsiReference referenceAtCaret = myFixture.getReferenceAtCaretPositionWithAssertion("assigned-field-reference.dql");
      final DQLFieldExpression resolved = assertInstanceOf(referenceAtCaret.resolve(), DQLFieldExpression.class);
      DQLAssignExpression assignExpression = resolved.getAssignExpression();
      assertNotNull(assignExpression);
      assertEquals("\"hello there\"", assignExpression.getExpressionList().getLast().getText());
   }

   @Test
   public void testFieldReferenceWhenFieldValueWasReassigned() {
      PsiReference referenceAtCaret = myFixture.getReferenceAtCaretPositionWithAssertion("reassigned-field-reference.dql");
      DQLFieldExpression element = assertInstanceOf(referenceAtCaret.getElement(), DQLFieldExpression.class);
      assertNotNull(element);
      DQLAssignExpression assignExpression = element.getAssignExpression();
      assertNotNull(assignExpression);
      assertEquals("\"second\"", assignExpression.getExpressionList().getLast().getText());
   }

   @Test
   public void testFieldReferenceForUnassignedField() {
      PsiReference referenceAtCaret = myFixture.getReferenceAtCaretPositionWithAssertion("unassigned-field-reference.dql");
      assertNull(referenceAtCaret.resolve());
   }
}
