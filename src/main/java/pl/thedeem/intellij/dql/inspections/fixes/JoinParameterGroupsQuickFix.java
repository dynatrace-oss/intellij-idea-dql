package pl.thedeem.intellij.dql.inspections.fixes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.psi.PsiUtils;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.psi.DQLTypes;

import java.util.List;

public class JoinParameterGroupsQuickFix implements LocalQuickFix {
    @SafeFieldForPreview
    private final MappedParameter parameter;

    public JoinParameterGroupsQuickFix(@NotNull MappedParameter parameter) {
        this.parameter = parameter;
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return DQLBundle.message("inspection.fix.genericFamilyName");
    }

    @NotNull
    @Override
    public String getName() {
        return DQLBundle.message("inspection.parameter.multipleParameterGroups.groupDetected.fix");
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        PsiElement element = descriptor.getPsiElement();
        PsiFile psiFile = element.getContainingFile();
        Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
        if (document == null) {
            return;
        }

        StringBuilder toInsert = new StringBuilder();
        List<List<PsiElement>> groups = parameter.getParameterGroups();

        for (int i = groups.size() - 1; i >= 1; i--) {
            List<PsiElement> group = groups.get(i);
            for (PsiElement expression : group) {
                toInsert.append(",").append(expression.getText());
            }

            int startOffset = group.getFirst().getTextRange().getStartOffset();
            PsiElement previousElement = PsiUtils.getPreviousElement(group.getFirst());
            if (previousElement != null && previousElement.getNode().getElementType() == DQLTypes.COMMA) {
                startOffset = previousElement.getTextRange().getStartOffset();
            }
            document.deleteString(startOffset, group.getLast().getTextRange().getEndOffset());
        }

        document.insertString(groups.getFirst().getLast().getTextRange().getEndOffset(), toInsert.toString());
    }
}
