package pl.thedeem.intellij.dpl.code;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.DumbAware;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.psi.*;

import java.util.ArrayList;
import java.util.List;

public class DPLFoldingBuilder extends FoldingBuilderEx implements DumbAware {
    @Override
    public FoldingDescriptor @NotNull [] buildFoldRegions(@NotNull PsiElement root,
                                                          @NotNull Document document,
                                                          boolean quick) {
        // Initialize the list of folding regions
        List<FoldingDescriptor> descriptors = new ArrayList<>();
        root.accept(new PsiRecursiveElementVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                if (element instanceof DPLDpl || element instanceof DPLCommandMatchers || element instanceof DPLMacroDefinitionExpression) {
                    if (element.getTextRange().getStartOffset() != element.getTextRange().getEndOffset()) {
                        descriptors.add(new FoldingDescriptor(element.getNode(), element.getTextRange()));
                    }
                }
                super.visitElement(element);
            }
        });

        return descriptors.toArray(FoldingDescriptor.EMPTY_ARRAY);
    }

    @Nullable
    @Override
    public String getPlaceholderText(@NotNull ASTNode node) {
        PsiElement element = node.getPsi();
        return switch (element) {
            case DPLDpl query -> {
                List<DPLExpressionDefinition> list = query.getExpressionDefinitionList();
                yield !list.isEmpty() ? list.getFirst().getText() : "";
            }
            case DPLCommandMatchers matchers -> {
                DPLCommandMatchersContent content = matchers.getCommandMatchersContent();
                if (content != null) {
                    int elements = switch (content) {
                        case DPLParametersMatchersList params -> params.getMatcherList().size();
                        case DPLMembersListMatchers params -> params.getExpressionDefinitionList().size();
                        case DPLExpressionMatchersList params -> params.getExpressionDefinitionList().size();
                        default -> 0;
                    };
                    yield DPLBundle.message("folding.elements", elements);
                }
                yield null;
            }
            case DPLMacroDefinitionExpression macro ->
                    DPLBundle.message("folding.variable", macro.getVariable().getText());
            default -> null;
        };
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull ASTNode node) {
        return false;
    }
}
