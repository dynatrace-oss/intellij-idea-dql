package pl.thedeem.intellij.dql.documentation.providers;

import com.intellij.openapi.util.text.HtmlChunk;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.DQLParameterDefinition;
import pl.thedeem.intellij.dql.definition.DQLParameterObject;

import java.util.ArrayList;
import java.util.List;

public class DQLParameterDocumentationProvider extends BaseTypedElementDocumentationProvider {
    private final DQLParameterObject parameter;

    public DQLParameterDocumentationProvider(@NotNull DQLParameterObject parameter) {
        super(parameter.getExpression(), DQLBundle.message("documentation.parameter.type"));
        this.parameter = parameter;
    }

    @Override
    protected List<HtmlChunk.Element> getSections() {
        List<HtmlChunk.Element> sections = new ArrayList<>();

        DQLParameterDefinition definition = parameter.getDefinition();
        sections.add(buildDescription(definition));
        if (definition != null) {
            sections.add(buildStandardSection(DQLBundle.message("documentation.parameter.allowedValues"), DQLBundle.print(definition.type)));
        }
        sections.add(buildTypeDescription());
        return sections;
    }

    protected HtmlChunk.Element buildDescription(DQLParameterDefinition definition) {
        return HtmlChunk.p().addText(definition != null ?
                definition.description
                : DQLBundle.message("documentation.parameter.unknown"));
    }
}
