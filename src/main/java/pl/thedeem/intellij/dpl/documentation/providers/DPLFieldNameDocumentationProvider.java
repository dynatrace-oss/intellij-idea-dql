package pl.thedeem.intellij.dpl.documentation.providers;

import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.psi.DPLFieldName;
import pl.thedeem.intellij.dpl.psi.DPLMembersListMatchers;

public class DPLFieldNameDocumentationProvider extends ExpressionPartDocumentationProvider<DPLFieldName> {
    public DPLFieldNameDocumentationProvider(@NotNull DPLFieldName field) {
        super(
                field,
                field.getParent().getParent() instanceof DPLMembersListMatchers ?
                        DPLBundle.message("documentation.memberField.type")
                        : DPLBundle.message("documentation.fieldName.type"),
                "AllIcons.Nodes.Field");
    }
}
