package pl.thedeem.intellij.dql.documentation.providers;

import com.intellij.openapi.util.text.HtmlChunk;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.definition.model.Parameter;

import java.util.ArrayList;
import java.util.List;

public class DQLParameterDocumentationProvider extends BaseTypedElementDocumentationProvider {
    private final MappedParameter parameter;

    public DQLParameterDocumentationProvider(@NotNull MappedParameter parameter) {
        super(parameter.holder(), DQLBundle.message("documentation.parameter.type"));
        this.parameter = parameter;
    }

    @Override
    protected List<HtmlChunk.Element> getSections() {
        List<HtmlChunk.Element> sections = new ArrayList<>();

        Parameter definition = parameter.definition();
        sections.add(buildDescription(definition));
        if (definition != null) {
            sections.add(
                    buildStandardSection(
                            DQLBundle.message("documentation.parameter.allowedValues"),
                            describeParameterTypes(definition, parameter.holder().getProject())
                    ));
        }
        sections.add(buildTypeDescription());
        return sections;
    }

    protected HtmlChunk.Element buildDescription(Parameter definition) {
        return HtmlChunk.p().addText(definition != null ? definition.description() : DQLBundle.message("documentation.parameter.unknown"));
    }
}
