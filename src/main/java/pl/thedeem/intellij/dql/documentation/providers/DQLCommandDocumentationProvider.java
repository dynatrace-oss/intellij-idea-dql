package pl.thedeem.intellij.dql.documentation.providers;

import com.intellij.openapi.util.text.HtmlChunk;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.Command;
import pl.thedeem.intellij.dql.psi.DQLCommand;

import java.util.ArrayList;
import java.util.List;

public class DQLCommandDocumentationProvider extends BaseDocumentationProvider<DQLCommand> {
    public DQLCommandDocumentationProvider(@NotNull DQLCommand command) {
        super(command, DQLBundle.message("documentation.command.type"), "AllIcons.Nodes.Class");
    }

    @Override
    protected @NotNull List<HtmlChunk> getSections() {
        List<HtmlChunk> sections = new ArrayList<>();
        Command definition = element.getDefinition();
        sections.add(buildDescription(
                definition != null
                        ? definition.description()
                        : DQLBundle.message("documentation.command.unknown")
        ));
        if (definition != null) {
            if (definition.synopsis() != null && !definition.synopsis().isEmpty()) {
                sections.add(buildSyntaxSection(definition.synopsis()));
            }
            if (definition.parameters() != null && !definition.parameters().isEmpty()) {
                sections.add(buildParametersDescription(definition.parameters()));
            }
            sections.add(buildMoreInfoLink(DQLBundle.message("documentation.statement.moreInfoLink")));
        }
        return sections;
    }
}
