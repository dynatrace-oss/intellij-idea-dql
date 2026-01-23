package pl.thedeem.intellij.dql.documentation.providers;

import com.intellij.openapi.util.text.HtmlChunk;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.elements.BaseTypedElement;
import pl.thedeem.intellij.dql.psi.elements.OperatorElement;

import java.util.ArrayList;
import java.util.List;

public class DQLExpressionDocumentationProvider extends BaseTypedElementDocumentationProvider<DQLExpression> {
    public DQLExpressionDocumentationProvider(@NotNull DQLExpression expression) {
        super(expression, DQLBundle.message("documentation.twoSidedExpression.type"), "AllIcons.Nodes.Method");
    }

    @Override
    protected @NotNull List<HtmlChunk> getSections() {
        List<HtmlChunk> sections = new ArrayList<>();

        if (element instanceof OperatorElement operator) {
            PsiElement left = operator.getLeftExpression();
            PsiElement right = operator.getRightExpression();
            if (left instanceof BaseTypedElement el) {
                sections.add(buildTitledSection(
                        DQLBundle.message("documentation.twoSidedExpression.leftType"),
                        prepareValuesDescription(el.getDataType(), element.getProject())
                ));
            }
            if (right instanceof BaseTypedElement el) {
                sections.add(buildTitledSection(
                        DQLBundle.message("documentation.twoSidedExpression.rightType"),
                        prepareValuesDescription(el.getDataType(), element.getProject())
                ));
            }
        }

        sections.add(buildTypeDescription());

        return sections;
    }
}
