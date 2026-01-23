package pl.thedeem.intellij.dql.documentation.providers;

import com.intellij.openapi.util.text.HtmlChunk;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.Function;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.definition.model.Signature;
import pl.thedeem.intellij.dql.psi.DQLFunctionExpression;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DQLFunctionDocumentationProvider extends BaseDocumentationProvider<DQLFunctionExpression> {
    public DQLFunctionDocumentationProvider(@NotNull DQLFunctionExpression function) {
        super(function, DQLBundle.message("documentation.function.type"), "AllIcons.Nodes.Function");
    }

    @Override
    protected @NotNull List<HtmlChunk> getSections() {
        List<HtmlChunk> sections = new ArrayList<>();
        Function definition = element.getDefinition();
        sections.add(buildDescription(
                definition != null ?
                        definition.description()
                        : DQLBundle.message("documentation.function.unknown")
        ));
        if (definition != null) {
            if (definition.synopsis() != null) {
                sections.add(buildSyntaxSection(definition.synopsis()));
            }
            Signature signature = element.getSignature();
            if (signature != null) {
                if (signature.outputs() != null && !signature.outputs().isEmpty()) {
                    sections.add(buildTitledSection(
                            DQLBundle.message("documentation.function.returnValues"),
                            prepareValuesDescription(signature.outputs(), element.getProject())));
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
}
