package pl.thedeem.intellij.dpl.documentation.providers;

import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.openapi.util.text.HtmlChunk;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.documentation.GenericDocumentationProvider;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.definition.model.Configuration;
import pl.thedeem.intellij.dpl.psi.DPLExpressionDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExpressionPartDocumentationProvider<T extends PsiElement> extends GenericDocumentationProvider<T> {
    public ExpressionPartDocumentationProvider(@NotNull T element, @Nullable String type, @Nullable String icon) {
        super(element, type, icon);
    }

    @Override
    protected @NotNull List<HtmlChunk> getSections() {
        List<HtmlChunk> sections = new ArrayList<>();

        HtmlChunk expressionDocs = buildExpressionDefinition();
        if (!expressionDocs.isEmpty()) {
            sections.add(expressionDocs);
        }
        return sections;
    }

    protected @NotNull HtmlChunk buildExpressionDefinition() {
        DPLExpressionDefinition def = PsiTreeUtil.getParentOfType(element, DPLExpressionDefinition.class);
        if (def == null) {
            return HtmlChunk.empty();
        }

        HtmlChunk.Element result = HtmlChunk.div();
        List<String> tags = new ArrayList<>();
        tags.add(DPLBundle.message(def.getNullable() == null ? "documentation.expression.required" : "documentation.expression.optional"));
        result = result.child(HtmlChunk.p().style("margin: 3px 0 5px 0").child(buildSeparatedElements(tags, tagElement())));
        HtmlChunk.Element table = DocumentationMarkup.SECTIONS_TABLE.style("width: 100%")
                .child(HtmlChunk.tag("tr")
                        .child(DocumentationMarkup.SECTION_HEADER_CELL.addText(DPLBundle.message("documentation.expression.fieldName")))
                        .child(DocumentationMarkup.SECTION_CONTENT_CELL.child(def.getExportedName() != null ?
                                DocumentationMarkup.PRE_ELEMENT.addText(def.getExportedName().getExportName())
                                : HtmlChunk.span().addText(DPLBundle.message("documentation.expression.noFieldName"))))
                );
        return buildTitledSection(
                DPLBundle.message("documentation.expression.definitionParts"),
                result.child(table)
        );
    }

    protected @NotNull HtmlChunk buildParametersDescription(@Nullable Map<String, Configuration> configurationDefinition) {
        if (configurationDefinition == null || configurationDefinition.isEmpty()) {
            return HtmlChunk.empty();
        }
        HtmlChunk.Element table = DocumentationMarkup.SECTIONS_TABLE.style("width: 100%");

        for (Configuration config : configurationDefinition.values()) {
            table = table.child(
                    HtmlChunk.tag("tr")
                            .child(DocumentationMarkup.SECTION_CONTENT_CELL
                                    .child(DocumentationMarkup.GRAYED_ELEMENT.style("white-space:nowrap;")
                                            .addText(config.name()).child(HtmlChunk.span().addText(": ")))
                                    .child(buildSeparatedElements(List.of(config.type()), tagElement()))
                            )
                            .child(DocumentationMarkup.SECTION_CONTENT_CELL.child(HtmlChunk.p().addText(config.description())))
            );
        }
        return HtmlChunk.div()
                .child(DocumentationMarkup.GRAYED_ELEMENT.addText(DPLBundle.message("documentation.configuration.parameters")))
                .child(HtmlChunk.hr())
                .child(table);
    }
}
