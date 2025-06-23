package pl.thedeem.intellij.dql.documentation.providers;

import com.intellij.openapi.util.text.HtmlChunk;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.DQLFunctionDefinition;
import pl.thedeem.intellij.dql.definition.DQLParameterDefinition;
import pl.thedeem.intellij.dql.psi.DQLFunctionCallExpression;

import java.util.ArrayList;
import java.util.List;

public class DQLFunctionDocumentationProvider extends BaseDocumentationProvider {
    private final DQLFunctionCallExpression function;

    public DQLFunctionDocumentationProvider(@NotNull DQLFunctionCallExpression function) {
        super(function, DQLBundle.message("documentation.function.type"));
        this.function = function;
    }

    @Override
    protected List<HtmlChunk.Element> getSections() {
        List<HtmlChunk.Element> sections = new ArrayList<>();
        DQLFunctionDefinition definition = function.getDefinition();
        sections.add(buildDescription(definition));
        if (definition != null) {
            if (definition.syntax != null && !definition.syntax.isEmpty()) {
                sections.add(buildSyntaxSection(definition.syntax));
            }
            if (definition.aliases != null && definition.aliases.size() > 1) {
                sections.add(buildStandardSection(DQLBundle.message("documentation.function.aliases"), DQLBundle.print(definition.aliases)));
            }
            if (definition.returns != null && !definition.returns.isEmpty()) {
                sections.add(buildStandardSection(DQLBundle.message("documentation.function.returnValues"), DQLBundle.print(definition.returns)));
            }
            List<DQLParameterDefinition> parameters = definition.getParameters(function);
            if (!parameters.isEmpty()) {
                sections.add(buildParametersDescription(definition.parameters));
            }
            sections.add(buildMoreInfoLink(
                    DQLBundle.message("documentation.function.moreInfoLink", definition.getFunctionGroup().getName(), definition.name)
            ));
        }
        return sections;
    }

    protected HtmlChunk.Element buildDescription(DQLFunctionDefinition definition) {
        return HtmlChunk.p().addText(definition != null ?
                StringUtil.defaultIfEmpty(definition.longDescription, definition.description)
                : DQLBundle.message("documentation.function.unknown"));
    }
}
