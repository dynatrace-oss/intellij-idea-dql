package pl.thedeem.intellij.dql.documentation.providers;

import com.intellij.openapi.util.text.HtmlChunk;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.Function;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.definition.model.Signature;
import pl.thedeem.intellij.dql.psi.DQLFunctionCallExpression;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DQLFunctionDocumentationProvider extends BaseDocumentationProvider {
    private final DQLFunctionCallExpression function;

    public DQLFunctionDocumentationProvider(@NotNull DQLFunctionCallExpression function) {
        super(function, DQLBundle.message("documentation.function.type"));
        this.function = function;
    }

    @Override
    protected List<HtmlChunk.Element> getSections() {
        List<HtmlChunk.Element> sections = new ArrayList<>();
        Function definition = function.getDefinition();
        sections.add(buildDescription(definition));
        if (definition != null) {
            if (definition.synopsis() != null) {
                sections.add(buildSyntaxSection(definition.synopsis()));
            }
            Signature signature = function.getSignature();
            if (signature != null) {
                if (signature.outputs() != null && !signature.outputs().isEmpty()) {
                    sections.add(buildStandardSection(
                            DQLBundle.message("documentation.function.returnValues"),
                            DQLBundle.types(signature.outputs(), function.getProject())));
                }
                List<Parameter> parameters = Objects.requireNonNullElse(signature.parameters(), List.of());
                if (!parameters.isEmpty()) {
                    sections.add(buildParametersDescription(signature.parameters()));
                }
                sections.add(buildMoreInfoLink(DQLBundle.message("documentation.function.moreInfoLink")));
            }

        }
        return sections;
    }

    protected HtmlChunk.Element buildDescription(Function definition) {
        return HtmlChunk.p().addText(definition != null ?
                definition.description()
                : DQLBundle.message("documentation.function.unknown"));
    }
}
