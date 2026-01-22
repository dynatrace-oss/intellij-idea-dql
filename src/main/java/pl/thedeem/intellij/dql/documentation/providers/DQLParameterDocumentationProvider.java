package pl.thedeem.intellij.dql.documentation.providers;

import com.intellij.openapi.util.text.HtmlChunk;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.definition.model.Parameter;

import java.util.ArrayList;
import java.util.List;

public class DQLParameterDocumentationProvider extends BaseTypedElementDocumentationProvider<PsiElement> {
    private final MappedParameter parameter;

    public DQLParameterDocumentationProvider(@NotNull MappedParameter parameter) {
        super(parameter.holder(), DQLBundle.message("documentation.parameter.type"), "AllIcons.Nodes.Parameter");
        this.parameter = parameter;
    }

    @Override
    protected @NotNull List<HtmlChunk> getSections() {
        List<HtmlChunk> sections = new ArrayList<>();
        Parameter definition = parameter.definition();
        sections.add(prepareParameterAttributesDescription(definition));
        sections.add(describeParameter(definition, parameter.holder().getProject(), true));
        sections.add(buildTypeDescription());
        return sections;
    }
}
