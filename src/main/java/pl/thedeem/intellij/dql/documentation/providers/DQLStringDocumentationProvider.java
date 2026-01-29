package pl.thedeem.intellij.dql.documentation.providers;

import com.intellij.openapi.util.text.HtmlChunk;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.psi.DQLString;

import java.util.List;

public class DQLStringDocumentationProvider extends BaseDocumentationProvider<DQLString> {
    public DQLStringDocumentationProvider(@NotNull DQLString element) {
        super(element, null, DQLBundle.message("documentation.string.type"), "AllIcons.Nodes.Word");
    }

    @Override
    protected @NotNull List<HtmlChunk> getSections() {
        return List.of(
                buildTitledSection(
                        DQLBundle.message("documentation.string.content"),
                        buildCodeBlock(element.getContent())
                )
        );
    }
}
