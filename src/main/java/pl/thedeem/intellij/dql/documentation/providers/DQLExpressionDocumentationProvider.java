package pl.thedeem.intellij.dql.documentation.providers;

import com.intellij.openapi.util.text.HtmlChunk;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.elements.BaseTypedElement;
import pl.thedeem.intellij.dql.psi.elements.TwoSidesExpression;

import java.util.ArrayList;
import java.util.List;

public class DQLExpressionDocumentationProvider extends BaseTypedElementDocumentationProvider {
    private final DQLExpression expression;

    public DQLExpressionDocumentationProvider(DQLExpression expression) {
        super(expression, DQLBundle.message("documentation.twoSidedExpression.type"));
        this.expression = expression;
    }

    @Override
    protected List<HtmlChunk.Element> getSections() {
        List<HtmlChunk.Element> sections = new ArrayList<>();

        if (expression instanceof TwoSidesExpression twoSidesExpression) {
            DQLExpression left = twoSidesExpression.getLeftExpression();
            DQLExpression right = twoSidesExpression.getRightExpression();
            if (left instanceof BaseTypedElement element) {
                sections.add(buildStandardSection(DQLBundle.message("documentation.twoSidedExpression.leftType"), DQLBundle.print(
                        DQLDataType.getTypes(element.getDataType())
                )));
            }
            if (right instanceof BaseTypedElement element) {
                sections.add(buildStandardSection(DQLBundle.message("documentation.twoSidedExpression.rightType"), DQLBundle.print(
                        DQLDataType.getTypes(element.getDataType())
                )));
            }
        }

        sections.add(buildTypeDescription());

        return sections;
    }
}
