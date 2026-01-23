package pl.thedeem.intellij.dpl.documentation.providers;

import com.intellij.openapi.util.text.HtmlChunk;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.psi.DPLCharacterGroupContent;

import java.util.ArrayList;
import java.util.List;

public class DPLCharacterClassDocumentationProvider extends ExpressionDefinitionDocumentationProvider<DPLCharacterGroupContent> {
    public DPLCharacterClassDocumentationProvider(@NotNull DPLCharacterGroupContent regex) {
        super(regex, DPLBundle.message("documentation.characterClass.type"), "AllIcons.Nodes.Record");
    }

    @Override
    protected @NotNull List<HtmlChunk> getSections() {
        List<HtmlChunk> sections = new ArrayList<>();
        sections.add(buildCodeBlock(element.getRegex()));
        HtmlChunk expressionDocs = buildExpressionDefinition();
        if (!expressionDocs.isEmpty()) {
            sections.add(expressionDocs);
        }
        return sections;
    }
}
