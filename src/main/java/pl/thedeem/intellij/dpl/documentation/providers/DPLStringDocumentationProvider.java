package pl.thedeem.intellij.dpl.documentation.providers;

import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.openapi.util.text.HtmlChunk;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.psi.DPLExpressionDefinition;
import pl.thedeem.intellij.dpl.psi.DPLStringContentElement;

import java.util.Objects;

public class DPLStringDocumentationProvider {
    @NotNull
    private final DPLStringContentElement string;

    public DPLStringDocumentationProvider(@NotNull DPLStringContentElement string) {
        this.string = string;
    }

    public @Nullable String generateDocumentation() {
        HtmlChunk.Element content = DocumentationMarkup.CONTENT_ELEMENT;
        HtmlChunk.Element documentation = DocumentationMarkup.DEFINITION_ELEMENT
                .child(buildHeader());

        DPLExpressionDefinition definition = PsiTreeUtil.getParentOfType(string, DPLExpressionDefinition.class);
        if (definition != null) {
            DPLExpressionDefinitionDocumentationProvider parent = new DPLExpressionDefinitionDocumentationProvider(definition);
            content = content
                    .child(HtmlChunk.hr())
                    .child(parent.buildDescription());
        }

        return documentation
                .child(content)
                .toString();
    }

    private HtmlChunk buildHeader() {
        String name = Objects.requireNonNull(string.getName());
        return DocumentationMarkup.PRE_ELEMENT
                .child(HtmlChunk.span().addText(name).attr("style", "padding-right: 10px;"))
                .child(DocumentationMarkup.GRAYED_ELEMENT.addText(DPLBundle.message("documentation.string.type")));
    }
}
