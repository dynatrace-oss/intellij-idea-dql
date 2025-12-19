package pl.thedeem.intellij.dql.inspections.fixes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.psi.PsiUtils;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.psi.DQLTypes;

import java.util.List;

public class DropInvalidParameterQuickFix implements LocalQuickFix {
    @SafeFieldForPreview
    private final MappedParameter parameter;

    public DropInvalidParameterQuickFix(MappedParameter parameter) {
        this.parameter = parameter;
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) throws IncorrectOperationException {
        PsiElement element = descriptor.getPsiElement();
        Document document = PsiDocumentManager.getInstance(project).getDocument(element.getContainingFile());
        if (document == null) {
            return;
        }

        for (List<PsiElement> parameterGroup : parameter.getParameterGroups()) {
            int start = parameterGroup.getFirst().getTextRange().getStartOffset();
            int end = parameterGroup.getLast().getTextRange().getEndOffset();

            PsiElement previousElement = PsiUtils.getPreviousElement(parameter.holder());
            if (previousElement != null && previousElement.getNode().getElementType() == DQLTypes.COMMA) {
                start = previousElement.getTextRange().getStartOffset();
            } else {
                PsiElement nextElement = PsiUtils.getNextElement(parameter.included().isEmpty() ? parameter.holder() : parameter.included().getLast());
                if (nextElement != null && nextElement.getNode().getElementType() == DQLTypes.COMMA) {
                    end = nextElement.getTextRange().getEndOffset();
                }
            }
            document.deleteString(start, end);
        }
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return DQLBundle.message("inspection.fix.genericFamilyName");
    }

    @NotNull
    @Override
    public String getName() {
        return DQLBundle.message("inspection.fix.dropInvalidParameter");
    }
}
