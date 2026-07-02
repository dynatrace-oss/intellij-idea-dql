package pl.thedeem.intellij.dql.services.variables;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase;
import org.junit.Test;
import pl.thedeem.intellij.dql.DQLFileType;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.psi.DQLVariableExpression;

import java.util.Objects;

public class DQLVariablesLiveReloadTest extends LightPlatformCodeInsightFixture4TestCase {
    @Test
    public void definingUnresolvedVariableResolvesIt() {
        myFixture.configureByText(DQLFileType.INSTANCE, "data record(x = $var)");
        DQLVariableExpression variable = variable("var");

        assertNull(variable.getDefinition());
        assertNull(variable.getValue());

        myFixture.addFileToProject("dql-variables.json", "{\"var\": \"value\"}");

        assertNotNull(variable.getDefinition());
        assertEquals("\"value\"", variable.getValue());
    }

    @Test
    public void editingValueUpdatesResolvedValue() {
        myFixture.addFileToProject("dql-variables.json", "{\"source\": \"logs\"}");
        myFixture.configureByText(DQLFileType.INSTANCE, "data record(x = $source)");
        DQLVariableExpression variable = variable("source");

        assertNotNull(variable.getDefinition());
        assertEquals("\"logs\"", variable.getValue());

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            Document document = Objects.requireNonNull(PsiDocumentManager.getInstance(getProject()).getDocument(variable.getDefinition().getContainingFile()));
            document.replaceString(0, document.getTextLength(), "{\"source\": \"metrics\"}");
            PsiDocumentManager.getInstance(getProject()).commitDocument(document);
        });

        assertEquals("\"metrics\"", variable.getValue());
    }

    @Test
    public void removingDefinitionRefreshesVariableValue() {
        myFixture.addFileToProject("dql-variables.json", "{\"data\": \"logs\"}");
        myFixture.configureByText(DQLFileType.INSTANCE, "data record(x = $data)");
        DQLVariableExpression variable = variable("data");

        assertNotNull(variable.getDefinition());

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            Document document = Objects.requireNonNull(PsiDocumentManager.getInstance(getProject()).getDocument(variable.getDefinition().getContainingFile()));
            document.replaceString(0, document.getTextLength(), "{}");
            PsiDocumentManager.getInstance(getProject()).commitDocument(document);
        });

        assertNull(variable.getDefinition());
    }

    private DQLVariableExpression variable(String name) {
        return DQLUtil.findVariablesInFile(myFixture.getFile()).stream()
                .filter(v -> name.equals(v.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Variable $" + name + " not found"));
    }
}
