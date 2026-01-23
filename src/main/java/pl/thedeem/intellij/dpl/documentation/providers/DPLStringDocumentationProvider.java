package pl.thedeem.intellij.dpl.documentation.providers;

import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.psi.DPLStringContentElement;

public class DPLStringDocumentationProvider extends ExpressionDefinitionDocumentationProvider<DPLStringContentElement> {
    public DPLStringDocumentationProvider(@NotNull DPLStringContentElement string) {
        super(string, DPLBundle.message("documentation.string.type"), "AllIcons.Nodes.Word");
    }
}
