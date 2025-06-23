package pl.thedeem.intellij.dql.code;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementVisitor;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.DQLParameterObject;
import pl.thedeem.intellij.dql.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DQLFoldingBuilder extends FoldingBuilderEx implements DumbAware {
    @Override
    public FoldingDescriptor @NotNull [] buildFoldRegions(@NotNull PsiElement root,
                                                          @NotNull Document document,
                                                          boolean quick) {
        // Initialize the list of folding regions
        List<FoldingDescriptor> descriptors = new ArrayList<>();
        root.accept(new PsiRecursiveElementVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                if (element instanceof DQLQuery || element instanceof DQLSubqueryExpression || element instanceof DQLBracketExpression) {
                    if (element.getTextRange().getStartOffset() != element.getTextRange().getEndOffset()) {
                        descriptors.add(new FoldingDescriptor(element.getNode(), element.getTextRange()));
                    }
                } else if (element instanceof DQLQueryStatement list) {
                    for (DQLParameterObject parameter : list.getParameters()) {
                        if (parameter.getValues().size() > 1) {
                            DQLExpression first = parameter.getValues().getFirst();
                            DQLExpression last = parameter.getValues().getLast();
                            descriptors.add(new FoldingDescriptor(first.getNode(), new TextRange(first.getTextRange().getStartOffset(), last.getTextRange().getEndOffset())));
                        }
                    }
                }
                else if (element instanceof DQLMultilineString string) {
                    descriptors.add(new FoldingDescriptor(string.getNode(), string.getTextRange()));
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
        if (element instanceof DQLQuery query) {
            return getPlaceholderForQuery(query);
        } else if (element instanceof DQLSubqueryExpression) {
            for (PsiElement child : element.getChildren()) {
                if (child instanceof DQLQuery query) {
                    return getPlaceholderForQuery(query);
                }
            }
        }
        else if (element instanceof DQLBracketExpression) {
            return getPlaceholderForFieldsList(element.getChildren().length);
        }
        else if (element instanceof DQLString string) {
            String text = string.getContent().trim();
            return DQLBundle.shorten(text);
        }
        return null;
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull ASTNode node) {
        return false;
    }

    private String getPlaceholderForQuery(DQLQuery query) {
        for (PsiElement child : query.getChildren()) {
            return child.getText();
        }
        return null;
    }

    private String getPlaceholderForFieldsList(int size) {
        if (size == 1) {
            return DQLBundle.message("folding.lists.oneElement");
        }
        return DQLBundle.message("folding.lists.multipleElements", size);
    }
}
