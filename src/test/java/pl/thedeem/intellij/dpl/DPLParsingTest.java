package pl.thedeem.intellij.dpl;

import com.intellij.testFramework.ParsingTestCase;

public class DPLParsingTest extends ParsingTestCase {
    public DPLParsingTest() {
        super("", "dpl", new DPLParserDefinition());
    }

    public void testDPLFormattingTest() {
        doTest(true);
    }

    public void testDPLConfigurationParameters() {
        doTest(true);
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/testData/parsing/dpl";
    }

    @Override
    protected boolean includeRanges() {
        return true;
    }
}
