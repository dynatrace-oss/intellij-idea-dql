package pl.thedeem.intellij.dql.documentation.providers;

import com.intellij.openapi.util.text.HtmlChunk;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.Command;
import pl.thedeem.intellij.dql.psi.DQLQueryStatement;

import java.util.ArrayList;
import java.util.List;

public class DQLCommandDocumentationProvider extends BaseDocumentationProvider {
    private final DQLQueryStatement command;

    public DQLCommandDocumentationProvider(@NotNull DQLQueryStatement command) {
        super(command, DQLBundle.message("documentation.command.type"));
        this.command = command;
    }

    @Override
    protected List<HtmlChunk.Element> getSections() {
        List<HtmlChunk.Element> sections = new ArrayList<>();

        Command definition = command.getDefinition();
        sections.add(buildDescription(definition));
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

    protected HtmlChunk.Element buildDescription(Command definition) {
        return HtmlChunk.p().addText(definition != null ? definition.description() : DQLBundle.message("documentation.command.unknown"));
    }
}
