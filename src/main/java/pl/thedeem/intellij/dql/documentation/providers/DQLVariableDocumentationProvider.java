package pl.thedeem.intellij.dql.documentation.providers;

import com.intellij.openapi.util.text.HtmlChunk;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.psi.DQLVariableExpression;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DQLVariableDocumentationProvider extends BaseTypedElementDocumentationProvider {
    private final DQLVariableExpression expression;

    public DQLVariableDocumentationProvider(DQLVariableExpression expression) {
        super(expression, DQLBundle.message("documentation.variable.type"));
        this.expression = expression;
    }

    @Override
    protected List<HtmlChunk.Element> getSections() {
        List<HtmlChunk.Element> sections = new ArrayList<>();
        sections.add(buildTypeDescription());
        sections.add(buildVariableValueDescription());
        return sections;
    }

    protected HtmlChunk.Element buildVariableValueDescription() {
        String valueElement = expression.getValue();
        return buildStandardSection(
                DQLBundle.message("documentation.variable.value"),
                Objects.requireNonNullElseGet(valueElement, () -> DQLBundle.message("documentation.variable.missingDefinition"))
        );
    }
}
