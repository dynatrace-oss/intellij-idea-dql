package pl.thedeem.intellij.common.quickFixes;

import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.psi.PsiUtils;

import java.util.Collections;
import java.util.List;

public abstract class AbstractDropListedElementQuickFix extends AbstractDropElementQuickFix {
    @SafeFieldForPreview
    private final List<IElementType> separators;

    public AbstractDropListedElementQuickFix(IElementType separator) {
        this(Collections.singletonList(separator));
    }

    public AbstractDropListedElementQuickFix(@NotNull List<IElementType> separators) {
        super();
        this.separators = separators;
    }

    @Override
    protected void deleteElement(@NotNull PsiElement element, @NotNull Document document) {
        int start = element.getTextRange().getStartOffset();
        int end = element.getTextRange().getEndOffset();

        PsiElement previousElement = PsiUtils.getPreviousElement(element);
        if (previousElement != null && this.separators.contains(previousElement.getNode().getElementType())) {
            start = previousElement.getTextRange().getStartOffset();
        } else {
            PsiElement nextElement = PsiUtils.getNextElement(element);
            if (nextElement != null && this.separators.contains(nextElement.getNode().getElementType())) {
                end = nextElement.getTextRange().getEndOffset();
            }
        }

        document.deleteString(start, end);
    }
}
