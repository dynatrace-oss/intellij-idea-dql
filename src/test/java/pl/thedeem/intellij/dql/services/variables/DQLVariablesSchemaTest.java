package pl.thedeem.intellij.dql.services.variables;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase;
import com.jetbrains.jsonSchema.impl.inspections.JsonSchemaComplianceInspection;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

public class DQLVariablesSchemaTest extends LightPlatformCodeInsightFixture4TestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        String schemaRoot = PathManager.getResourceRoot(getClass(), "/schemas/dql-variables.schema.json");
        if (schemaRoot != null) {
            VfsRootAccess.allowRootAccess(getTestRootDisposable(), schemaRoot);
        }
        myFixture.enableInspections(new JsonSchemaComplianceInspection());
    }

    @Test
    public void reportsNoIssuesForValidDefinition() {
        String content = /* language=JSON */ """
                {
                  "str": "logs",
                  "num": 100,
                  "flag": true,
                  "nothing": null,
                  "list": ["a", 1, false],
                  "rec": {"a": "b", "c": 2},
                  "fragment": {"$type": "dql", "dql": "fetch logs"}
                }
                """;

        assertTrue(schemaProblems(highlight("dql-variables.json", content)).isEmpty());
    }

    @Test
    public void reportsIssueForInvalidRootType() {
        String content = /* language=JSON */ "[\"not\", \"an\", \"object\"]";

        assertFalse(schemaProblems(highlight("dql-variables.json", content)).isEmpty());
    }

    @Test
    public void reportsIssueForInvalidPropertyStructure() {
        String content = /* language=JSON */ "{\"fragment\": {\"$type\": \"dql\"}}";

        assertFalse(schemaProblems(highlight("dql-variables.json", content)).isEmpty());
    }

    @Test
    public void doesNotValidateOtherJsonFiles() {
        List<HighlightInfo> problems = schemaProblems(highlight("other-file.json", /* language=JSON */ "[\"top-level-array\"]"));
        assertTrue(problems.isEmpty());
    }

    private List<HighlightInfo> highlight(String fileName, String content) {
        myFixture.configureByText(fileName, content);
        return myFixture.doHighlighting();
    }

    private List<HighlightInfo> schemaProblems(List<HighlightInfo> highlights) {
        return highlights.stream()
                .filter(info -> info.getSeverity().compareTo(HighlightSeverity.WEAK_WARNING) >= 0)
                .filter(info -> info.getDescription() != null)
                .collect(Collectors.toList());
    }
}
