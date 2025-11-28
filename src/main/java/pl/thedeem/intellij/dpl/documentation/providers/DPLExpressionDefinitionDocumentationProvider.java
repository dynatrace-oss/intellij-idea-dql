package pl.thedeem.intellij.dpl.documentation.providers;

import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.openapi.util.text.HtmlChunk;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.psi.DPLExpressionDefinition;

public class DPLExpressionDefinitionDocumentationProvider {
    private final @NotNull DPLExpressionDefinition expression;

    public DPLExpressionDefinitionDocumentationProvider(@NotNull DPLExpressionDefinition expression) {
        this.expression = expression;
    }

    public HtmlChunk buildDescription() {
        HtmlChunk fieldName = expression.getExportedName() != null ?
                DocumentationMarkup.PRE_ELEMENT.addText(expression.getExportedName().getExportName())
                : HtmlChunk.span().addText(DPLBundle.message("documentation.expression.noFieldName"));
        return DocumentationMarkup.SECTIONS_TABLE
                .child(HtmlChunk.tag("tr")
                        .child(DocumentationMarkup.SECTION_HEADER_CELL.addText(DPLBundle.message("documentation.expression.fieldName")))
                        .child(DocumentationMarkup.SECTION_CONTENT_CELL.child(fieldName))
                )
                .child(HtmlChunk.tag("tr")
                        .child(DocumentationMarkup.SECTION_HEADER_CELL.addText(DPLBundle.message("documentation.expression.requirement")))
                        .child(DocumentationMarkup.SECTION_CONTENT_CELL.addText(DPLBundle.message(
                                expression.getNullable() == null ? "documentation.expression.required" : "documentation.expression.optional"
                        )))
                );
    }
}
