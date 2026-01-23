package pl.thedeem.intellij.dql.documentation.providers;

import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.openapi.util.text.HtmlChunk;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.psi.DQLVariableExpression;

import java.util.ArrayList;
import java.util.List;

public class DQLVariableDocumentationProvider extends BaseTypedElementDocumentationProvider<DQLVariableExpression> {
    public DQLVariableDocumentationProvider(@NotNull DQLVariableExpression expression) {
        super(expression, DQLBundle.message("documentation.variable.type"), "AllIcons.Nodes.Variable");
    }

    @Override
    protected @NotNull List<HtmlChunk> getSections() {
        List<HtmlChunk> sections = new ArrayList<>();
        sections.add(buildTypeDescription());
        sections.add(buildTitledSection(
                DQLBundle.message("documentation.variable.value"),
                element.getValue() != null
                        ? buildCodeBlock(element.getValue())
                        : HtmlChunk.span().addText(DQLBundle.message("documentation.variable.missingDefinition"))
        ));
        PsiElement definition = element.getDefinition();
        if (definition != null) {
            String path = null;
            VirtualFile virtualFile = definition.getContainingFile().getVirtualFile();
            if (virtualFile != null) {
                path = virtualFile.getUrl();
            }

            if (path != null) {
                sections.add(buildTitledSection(
                        DQLBundle.message("documentation.variable.source"),
                        DocumentationMarkup.BOTTOM_ELEMENT
                                .child(HtmlChunk.link(path, DocumentationMarkup.EXTERNAL_LINK_ICON.addText(virtualFile.getPresentableName())))
                ));
            }
        }
        return sections;
    }
}
