package pl.thedeem.intellij.dpl.documentation.providers;

import com.intellij.openapi.util.text.HtmlChunk;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.definition.model.ExpressionDescription;
import pl.thedeem.intellij.dpl.psi.DPLCommandExpression;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DPLCommandDocumentationProvider extends ExpressionDefinitionDocumentationProvider<DPLCommandExpression> {
    public DPLCommandDocumentationProvider(@NotNull DPLCommandExpression command) {
        super(command, DPLBundle.message("documentation.command.type"), "AllIcons.Nodes.Class");
    }

    @Override
    protected @NotNull List<HtmlChunk> getSections() {
        ExpressionDescription definition = element.getDefinition();
        if (definition == null) {
            return List.of(buildDescription(DPLBundle.message("documentation.unknownCommand")));
        }
        List<HtmlChunk> sections = new ArrayList<>();
        List<String> outputs = new ArrayList<>();
        if (definition.output() != null) {
            outputs.add(definition.output());
        }
        sections.add(HtmlChunk.p().style("margin: 3px 0 5px 0").child(buildSeparatedElements(outputs, tagElement())));
        sections.add(buildDescription(
                definition.description() == null
                        ? DPLBundle.message("documentation.unknownCommand") : definition.description()
        ));

        if (definition.aliases() != null) {
            Set<String> names = new HashSet<>(definition.aliases());
            names.add(definition.name());
            sections.add(buildTitledSection(
                    DPLBundle.message("documentation.command.aliases"),
                    buildSeparatedElements(names, HtmlChunk.tag("tt"))
            ));
        }

        HtmlChunk parametersDocs = buildParametersDescription(definition.configuration());
        if (!parametersDocs.isEmpty()) {
            sections.add(parametersDocs);
        }
        HtmlChunk expressionDocs = buildExpressionDefinition();
        if (!expressionDocs.isEmpty()) {
            sections.add(expressionDocs);
        }
        return sections;
    }
}
