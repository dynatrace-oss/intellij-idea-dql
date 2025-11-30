package pl.thedeem.intellij.dpl.documentation.providers;

import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.openapi.util.text.HtmlChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.definition.model.Configuration;
import pl.thedeem.intellij.dpl.definition.model.ExpressionDescription;
import pl.thedeem.intellij.dpl.psi.DPLExpressionDefinition;

import java.util.Map;
import java.util.Objects;

public class DPLConfigurationDocumentationProvider {
    @NotNull
    private final DPLExpressionDefinition expression;

    public DPLConfigurationDocumentationProvider(@NotNull DPLExpressionDefinition expression) {
        this.expression = expression;
    }

    public @Nullable String generateDocumentation() {
        HtmlChunk.Element documentation = DocumentationMarkup.DEFINITION_ELEMENT
                .child(buildHeader())
                .child(DocumentationMarkup.CONTENT_ELEMENT
                        .child(buildDescription())
                );

        return documentation.toString();
    }

    private HtmlChunk buildHeader() {
        return DocumentationMarkup.PRE_ELEMENT
                .child(DocumentationMarkup.GRAYED_ELEMENT.addText(DPLBundle.message("documentation.configurationParameter.type")));
    }

    public HtmlChunk buildDescription() {
        ExpressionDescription description = expression.getDefinition();
        Map<String, Configuration> configurationDefinition = description != null ? Objects.requireNonNullElse(description.configuration(), Map.of()) : Map.of();
        if (!configurationDefinition.isEmpty()) {
            HtmlChunk.Element table = DocumentationMarkup.SECTIONS_TABLE
                    .child(HtmlChunk.tag("tr")
                            .child(DocumentationMarkup.SECTION_HEADER_CELL.addText(DPLBundle.message("documentation.configuration.name")))
                            .child(DocumentationMarkup.SECTION_HEADER_CELL.addText(DPLBundle.message("documentation.configuration.type")))
                            .child(DocumentationMarkup.SECTION_HEADER_CELL.addText(DPLBundle.message("documentation.configuration.description")))
                    );

            for (Configuration config : configurationDefinition.values()) {
                table = table.child(HtmlChunk.tag("tr")
                        .child(DocumentationMarkup.SECTION_CONTENT_CELL.addText(config.name()))
                        .child(DocumentationMarkup.SECTION_CONTENT_CELL.child(DocumentationMarkup.PRE_ELEMENT.addText(config.type())))
                        .child(DocumentationMarkup.SECTION_CONTENT_CELL.addText(config.description()))
                );
            }
            return table;
        }
        return HtmlChunk.empty();
    }
}
