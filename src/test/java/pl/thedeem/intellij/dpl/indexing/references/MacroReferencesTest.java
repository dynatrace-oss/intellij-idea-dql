package pl.thedeem.intellij.dpl.indexing.references;

import com.intellij.psi.PsiReference;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase;
import org.junit.Test;
import pl.thedeem.intellij.dpl.psi.DPLVariable;

public class MacroReferencesTest extends LightPlatformCodeInsightFixture4TestCase {
    @Override
    protected String getTestDataPath() {
        return "src/test/testData/indexing/references/dpl";
    }

    @Test
    public void testWhenMacroIsUnassignedItShouldNotCreateAnyReferences() {
        PsiReference referenceAtCaret = myFixture.getReferenceAtCaretPositionWithAssertion("unknown-macro-reference.dpl");
        assertNull(referenceAtCaret.resolve());
    }

    @Test
    public void testWhenMacroIsDefinedItShouldPointToDefinition() {
        PsiReference referenceAtCaret = myFixture.getReferenceAtCaretPositionWithAssertion("defined-macro-reference.dpl");
        final DPLVariable resolved = assertInstanceOf(referenceAtCaret.resolve(), DPLVariable.class);
        assertNotNull(resolved.getDefinition());
        assertEquals("$macro = INT;", resolved.getDefinition().getText());
    }
}
