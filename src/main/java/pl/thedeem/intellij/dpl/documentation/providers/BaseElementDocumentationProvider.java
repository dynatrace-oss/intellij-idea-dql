package pl.thedeem.intellij.dpl.documentation.providers;

import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.text.HtmlChunk;
import com.intellij.psi.impl.PsiElementBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BaseElementDocumentationProvider {

    @NotNull
    private final PsiElementBase base;

    public BaseElementDocumentationProvider(@NotNull PsiElementBase base) {
        this.base = base;
    }

    public @Nullable String generateDocumentation() {
        ItemPresentation presentation = base.getPresentation();
        if (presentation == null || presentation.getPresentableText() == null) {
            return null;
        }
        HtmlChunk.Element documentation = DocumentationMarkup.DEFINITION_ELEMENT
                .child(buildHeader(presentation.getPresentableText()))
                .child(DocumentationMarkup.CONTENT_ELEMENT);

        return documentation.toString();
    }

    private HtmlChunk buildHeader(String presentationText) {
        return DocumentationMarkup.PRE_ELEMENT
                .child(DocumentationMarkup.GRAYED_ELEMENT.addText(presentationText));
    }

}
