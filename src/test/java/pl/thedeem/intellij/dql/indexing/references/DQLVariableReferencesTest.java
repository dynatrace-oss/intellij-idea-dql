package pl.thedeem.intellij.dql.indexing.references;

import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase;
import org.junit.Test;

public class DQLVariableReferencesTest extends LightPlatformCodeInsightFixture4TestCase {
    @Override
    protected String getTestDataPath() {
        return "src/test/testData/indexing/references/dql";
    }

    @Test
    public void testVariableReferenceWhenVariableHasAssignedValue() {
        PsiReference referenceAtCaret = myFixture.getReferenceAtCaretPositionWithAssertion("assigned-variable-reference.dql", "dql-variables.json");
        final JsonProperty resolved = assertInstanceOf(referenceAtCaret.resolve(), JsonProperty.class);
        final JsonStringLiteral value = assertInstanceOf(resolved.getValue(), JsonStringLiteral.class);
        assertEquals("\"logs\"", value.getText());
    }

    @Test
    public void testVariableReferenceForUnassignedVariable() {
        PsiReference referenceAtCaret = myFixture.getReferenceAtCaretPositionWithAssertion("unassigned-variable-reference.dql", "dql-variables.json");
        assertNull(referenceAtCaret.resolve());
    }
}
