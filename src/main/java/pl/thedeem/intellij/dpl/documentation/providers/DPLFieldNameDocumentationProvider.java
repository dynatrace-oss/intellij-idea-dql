package pl.thedeem.intellij.dpl.documentation.providers;

import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.openapi.util.text.HtmlChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.psi.DPLExpressionDefinition;
import pl.thedeem.intellij.dpl.psi.DPLFieldName;
import pl.thedeem.intellij.dpl.psi.DPLMembersListMatchers;

public class DPLFieldNameDocumentationProvider {
    @NotNull
    private final DPLFieldName field;

    public DPLFieldNameDocumentationProvider(@NotNull DPLFieldName field) {
        this.field = field;
    }

    public @Nullable String generateDocumentation() {
        HtmlChunk.Element content = DocumentationMarkup.CONTENT_ELEMENT;
        HtmlChunk.Element documentation = DocumentationMarkup.DEFINITION_ELEMENT
                .child(buildHeader());

        if (field.getParent() instanceof DPLExpressionDefinition expression) {
            DPLExpressionDefinitionDocumentationProvider parent = new DPLExpressionDefinitionDocumentationProvider(expression);
            content = content
                    .child(HtmlChunk.hr())
                    .child(parent.buildDescription());
        }

        return documentation
                .child(content)
                .toString();
    }

    private HtmlChunk buildHeader() {
        String type = field.getParent().getParent() instanceof DPLMembersListMatchers ? DPLBundle.message("documentation.memberField.type") : DPLBundle.message("documentation.fieldName.type");
        return DocumentationMarkup.PRE_ELEMENT
                .child(HtmlChunk.span().addText(field.getExportName()).attr("style", "padding-right: 10px;"))
                .child(DocumentationMarkup.GRAYED_ELEMENT.addText(type));
    }
}
