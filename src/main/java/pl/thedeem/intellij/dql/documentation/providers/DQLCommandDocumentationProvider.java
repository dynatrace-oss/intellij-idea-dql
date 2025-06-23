package pl.thedeem.intellij.dql.documentation.providers;

import com.intellij.openapi.util.text.HtmlChunk;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.DQLCommandDefinition;
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
        DQLCommandDefinition definition = command.getDefinition();
        sections.add(buildDescription(definition));
        if (definition != null) {
            if (definition.syntax != null && !definition.syntax.isEmpty()) {
                sections.add(buildSyntaxSection(definition.syntax));
            }
            if (definition.parameters != null && !definition.parameters.isEmpty()) {
                sections.add(buildParametersDescription(definition.parameters));
            }
            sections.add(buildMoreInfoLink(
                    DQLBundle.message("documentation.statement.moreInfoLink", definition.getCommandGroup().getName(), definition.name)
            ));
        }
        return sections;
    }

    protected HtmlChunk.Element buildDescription(DQLCommandDefinition definition) {
        return HtmlChunk.p().addText(definition != null ? definition.description : DQLBundle.message("documentation.command.unknown"));
    }
}
