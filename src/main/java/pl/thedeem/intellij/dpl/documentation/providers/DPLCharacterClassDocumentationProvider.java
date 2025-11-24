package pl.thedeem.intellij.dpl.documentation.providers;

import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.openapi.util.text.HtmlChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.psi.DPLCharacterGroupContent;

public class DPLCharacterClassDocumentationProvider {
    @NotNull
    private final DPLCharacterGroupContent regex;

    public DPLCharacterClassDocumentationProvider(@NotNull DPLCharacterGroupContent regex) {
        this.regex = regex;
    }

    public @Nullable String generateDocumentation() {
        HtmlChunk.Element documentation = DocumentationMarkup.DEFINITION_ELEMENT
                .child(buildHeader())
                .child(DocumentationMarkup.CONTENT_ELEMENT
                        .child(buildDescription()));

        return documentation.toString();
    }

    private HtmlChunk buildHeader() {
        return DocumentationMarkup.PRE_ELEMENT
                .child(DocumentationMarkup.GRAYED_ELEMENT.addText(DPLBundle.message("documentation.characterClass.type")));
    }

    private HtmlChunk buildDescription() {
        return DocumentationMarkup.PRE_ELEMENT.addText(regex.getRegex());
    }
}
