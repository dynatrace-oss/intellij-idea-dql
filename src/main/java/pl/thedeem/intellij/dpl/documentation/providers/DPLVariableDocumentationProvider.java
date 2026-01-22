package pl.thedeem.intellij.dpl.documentation.providers;

import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.text.HtmlChunk;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.psi.DPLExpressionsSequence;
import pl.thedeem.intellij.dpl.psi.DPLMacroDefinitionExpression;
import pl.thedeem.intellij.dpl.psi.DPLVariable;

import java.util.ArrayList;
import java.util.List;

public class DPLVariableDocumentationProvider extends ExpressionPartDocumentationProvider<DPLVariable> {
    public DPLVariableDocumentationProvider(@NotNull DPLVariable variable) {
        super(variable, DPLBundle.message("documentation.variable.type"), "AllIcons.Nodes.Variable");
    }

    @Override
    protected @NotNull List<HtmlChunk> getSections() {
        List<HtmlChunk> sections = new ArrayList<>();
        sections.add(buildVariableDefinition());
        HtmlChunk expressionDocs = buildExpressionDefinition();
        if (!expressionDocs.isEmpty()) {
            sections.add(expressionDocs);
        }
        return sections;
    }

    private @NotNull HtmlChunk buildVariableDefinition() {
        DPLMacroDefinitionExpression definition = element.getDefinition();
        DPLExpressionsSequence sequence = definition != null ? definition.getExpressionsSequence() : null;
        HtmlChunk content;
        if (definition == null || sequence == null || sequence.getExpressionDefinitionList().isEmpty()) {
            content = HtmlChunk.span().addText(DPLBundle.message("documentation.variable.unknown"));
        } else {
            List<@NlsSafe String> list = sequence.getExpressionDefinitionList().stream().map(PsiElement::getText).toList();
            content = buildCodeBlock(String.join("\n", list));

        }
        return buildTitledSection(DPLBundle.message("documentation.variable.definition"), content);
    }
}
