package pl.thedeem.intellij.dpl.documentation.providers;

import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.openapi.util.text.HtmlChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.psi.DPLMacroDefinitionExpression;
import pl.thedeem.intellij.dpl.psi.DPLVariable;

import java.util.Objects;

public class DPLVariableDocumentationProvider {
    @NotNull
    private final DPLVariable variable;

    public DPLVariableDocumentationProvider(@NotNull DPLVariable variable) {
        this.variable = variable;
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
        String name = Objects.requireNonNull(variable.getName());
        return DocumentationMarkup.PRE_ELEMENT
                .child(HtmlChunk.span().addText(name).attr("style", "padding-right: 10px;"))
                .child(DocumentationMarkup.GRAYED_ELEMENT.addText(DPLBundle.message("documentation.variable.type")));
    }

    private HtmlChunk buildDescription() {
        DPLMacroDefinitionExpression definition = variable.getDefinition();
        if (definition == null || definition.getExpressionDefinitionList().isEmpty()) {
            return HtmlChunk.span().addText(DPLBundle.message("documentation.variable.unknown"));
        }
        return HtmlChunk.empty();
    }
}
