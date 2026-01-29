package pl.thedeem.intellij.dpl.documentation.providers;

import com.intellij.openapi.util.text.HtmlChunk;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.psi.DPLStringContentElement;

import java.util.List;

public class DPLStringDocumentationProvider extends ExpressionDefinitionDocumentationProvider<DPLStringContentElement> {
    public DPLStringDocumentationProvider(@NotNull DPLStringContentElement string) {
        super(string, DPLBundle.message("documentation.string.type"), "AllIcons.Nodes.Word");
    }

    @Override
    protected @NotNull List<HtmlChunk> getSections() {
        return List.of(
                buildTitledSection(
                        DPLBundle.message("documentation.string.content"),
                        buildCodeBlock(element.getText())
                )
        );
    }
}
