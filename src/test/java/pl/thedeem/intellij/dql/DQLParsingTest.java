package pl.thedeem.intellij.dql;

import com.intellij.testFramework.ParsingTestCase;

public class DQLParsingTest extends ParsingTestCase {
    public DQLParsingTest() {
        super("", "dql", new DQLParserDefinition());
    }

    public void testDQLFetchLogsTest() {
        doTest(true);
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/testData/parsing";
    }

    @Override
    protected boolean includeRanges() {
        return true;
    }
}
