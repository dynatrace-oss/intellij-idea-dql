package pl.thedeem.intellij.dql.services.variables;

import com.intellij.json.psi.JsonFile;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase;
import org.junit.Test;
import pl.thedeem.intellij.dql.psi.DQLVariableExpression;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class DQLVariableFindUsagesTest extends LightPlatformCodeInsightFixture4TestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        String schemaRoot = PathManager.getResourceRoot(getClass(), "/schemas/dql-variables.schema.json");
        if (schemaRoot != null) {
            VfsRootAccess.allowRootAccess(getTestRootDisposable(), schemaRoot);
        }
    }

    @Test
    public void resolvesAllUsagesInDQLQueries() {
        JsonProperty source = defineVariable("dql-variables.json", "{\"variable\": \"logs\"}", "variable");
        myFixture.addFileToProject("a.dql", "data record(x = $variable)");
        myFixture.addFileToProject("b.dql", "data record(y = $variable)");
        myFixture.addFileToProject("c.dql", "data record(z = $other)");

        List<DQLVariableExpression> usages = service().findVariableUsages(source);

        assertEquals(2, usages.size());
        assertSameElements(usages.stream().map(usage -> usage.getContainingFile().getName()).toList(), "a.dql", "b.dql");
    }

    @Test
    public void correctlyFindsUsagesWithDqlVariablesPrecedence() {
        JsonProperty outer = defineVariable("dql-variables.json", "{\"source\": \"logs\"}", "source");
        JsonProperty inner = defineVariable("nested/dql-variables.json", "{\"source\": \"metrics\"}", "source");
        myFixture.addFileToProject("root.dql", "data record(x = $source)");
        myFixture.addFileToProject("nested/child.dql", "data record(y = $source)");

        List<DQLVariableExpression> outerUsages = service().findVariableUsages(outer);
        assertEquals(1, outerUsages.size());
        assertEquals("root.dql", outerUsages.getFirst().getContainingFile().getName());

        List<DQLVariableExpression> innerUsages = service().findVariableUsages(inner);
        assertEquals(1, innerUsages.size());
        assertEquals("child.dql", innerUsages.getFirst().getContainingFile().getName());
    }

    @Test
    public void reportsNoFoundUsagesForUnusedVariable() {
        JsonProperty source = defineVariable("dql-variables.json", "{\"unused\": \"logs\"}", "unused");
        myFixture.addFileToProject("a.dql", "data record(z = $other)");

        assertTrue(service().findVariableUsages(source).isEmpty());
    }

    @Test
    public void referenceSearchFindsAllRelatedFiles() {
        JsonProperty source = defineVariable("dql-variables.json", "{\"source\": \"logs\"}", "source");
        myFixture.addFileToProject("a.dql", "data record(x = $source)");
        myFixture.addFileToProject("some/subdir/b.dql", "data record(y = $source)");

        Collection<PsiReference> references = ReferencesSearch.search(source, GlobalSearchScope.allScope(getProject())).findAll();

        assertEquals(2, references.size());
    }

    private DQLVariablesService service() {
        return DQLVariablesService.getInstance(getProject());
    }

    private JsonProperty defineVariable(String path, String content, String name) {
        JsonFile file = (JsonFile) myFixture.addFileToProject(path, content);
        JsonProperty property = ((JsonObject) Objects.requireNonNull(file.getTopLevelValue())).findProperty(name);
        assertNotNull("Property " + name + " should exist", property);
        return property;
    }
}
