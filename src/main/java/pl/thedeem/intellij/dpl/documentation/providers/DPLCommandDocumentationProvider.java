package pl.thedeem.intellij.dpl.documentation.providers;

import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.openapi.util.text.HtmlChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.definition.model.Command;
import pl.thedeem.intellij.dpl.psi.DPLCommandExpression;
import pl.thedeem.intellij.dpl.psi.DPLExpressionDefinition;

import java.util.Objects;

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
            if (definition.description() != null) {
                content = content.child(HtmlChunk.span().addText(definition.description()));
            }
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
}
