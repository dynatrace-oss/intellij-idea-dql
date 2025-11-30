package pl.thedeem.intellij.dpl.documentation.providers;

import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.openapi.util.text.HtmlChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.definition.model.Command;
import pl.thedeem.intellij.dpl.psi.DPLCommandExpression;
import pl.thedeem.intellij.dpl.psi.DPLExpressionDefinition;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class DPLCommandDocumentationProvider {
    private final @NotNull DPLCommandExpression command;

    public DPLCommandDocumentationProvider(@NotNull DPLCommandExpression command) {
        this.command = command;
    }

    public @Nullable String generateDocumentation() {
        Command definition = command.getDefinition();
        HtmlChunk.Element content = DocumentationMarkup.CONTENT_ELEMENT;
        HtmlChunk.Element documentation = DocumentationMarkup.DEFINITION_ELEMENT
                .child(buildHeader(definition));
        if (definition == null) {
            content = content.child(HtmlChunk.span().addText(DPLBundle.message("documentation.unknownCommand")));
        } else {
            content = content.child(buildDescription());
        }
        if (command.getParent() instanceof DPLExpressionDefinition expression) {
            content = content.child(new DPLConfigurationDocumentationProvider(expression).buildDescription());
            DPLExpressionDefinitionDocumentationProvider parent = new DPLExpressionDefinitionDocumentationProvider(expression);
            content = content
                    .child(HtmlChunk.hr())
                    .child(parent.buildDescription());
        }
        return documentation
                .child(content)
                .toString();
    }

    private HtmlChunk buildHeader(@Nullable Command definition) {
        String name = Objects.requireNonNull(command.getCommandKeyword().getName());
        HtmlChunk.Element header = DocumentationMarkup.PRE_ELEMENT.child(
                HtmlChunk.span()
                        .addText(name).attr("style", "padding-right: 10px;")
        );
        if (definition != null && definition.output() != null) {
            header = header.child(DocumentationMarkup.GRAYED_ELEMENT.addText(definition.output()));
        }
        return header;
    }

    private HtmlChunk buildDescription() {
        Command definition = command.getDefinition();
        if (definition == null) {
            return HtmlChunk.empty();
        }
        HtmlChunk.Element result = HtmlChunk.span();
        if (definition.description() != null) {
            result = result.child(HtmlChunk.span().addText(definition.description()));
        }
        if (definition.aliases() != null) {
            Set<String> names = new HashSet<>(definition.aliases());
            names.add(definition.name());
            result = result.child(DocumentationMarkup.SECTIONS_TABLE
                    .child(HtmlChunk.tag("tr")
                            .child(DocumentationMarkup.SECTION_HEADER_CELL.addText(DPLBundle.message("documentation.command.aliases")))
                    )
                    .child(HtmlChunk.tag("tr")
                            .child(DocumentationMarkup.SECTION_CONTENT_CELL.child(DocumentationMarkup.PRE_ELEMENT.addText(String.join(", ", names))))
                    )
            );

        }
        return result;
    }
}
