package pl.thedeem.intellij.dql.documentation.providers;

import com.intellij.openapi.util.text.HtmlChunk;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.psi.DQLParameterExpression;

import java.util.ArrayList;
import java.util.List;

public class DQLParameterDocumentationProvider extends BaseTypedElementDocumentationProvider<DQLParameterExpression> {
    public DQLParameterDocumentationProvider(@NotNull DQLParameterExpression parameter) {
        super(parameter, DQLBundle.message("documentation.parameter.type"), "AllIcons.Nodes.Parameter");
    }

    @Override
    protected @NotNull List<HtmlChunk> getSections() {
        List<HtmlChunk> sections = new ArrayList<>();
        Parameter definition = element.definition();

        sections.add(prepareParameterAttributesDescription(definition));
        sections.add(describeParameter(definition, element.getProject(), true));
        sections.add(buildTypeDescription());

        if (definition != null) {
            if (definition.defaultValue() != null) {
                sections.add(buildTitledSection(
                        DQLBundle.message("documentation.parameter.defaultValue"),
                        buildCodeBlock(definition.defaultValue())
                ));
            }
            if (definition.excludes() != null) {
                sections.add(buildTitledSection(
                        DQLBundle.message("documentation.parameter.excludes"),
                        buildSeparatedElements(definition.excludes(), HtmlChunk.tag("tt"))
                ));
            }
        }
        return sections;
    }
}
