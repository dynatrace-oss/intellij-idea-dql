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
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.definition.DQLParameterObject;
import pl.thedeem.intellij.dql.psi.DQLTypes;

public class DropInvalidParameterQuickFix implements LocalQuickFix {
  @SafeFieldForPreview
  private final DQLParameterObject parameter;

  public DropInvalidParameterQuickFix(DQLParameterObject parameter) {
    this.parameter = parameter;
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) throws IncorrectOperationException {
    PsiElement element = descriptor.getPsiElement();
    Document document = PsiDocumentManager.getInstance(project).getDocument(element.getContainingFile());
    if (document == null) {
      return;
    }
    PsiElement firstElement = parameter.getValues().isEmpty() ? parameter.getExpression() : parameter.getValues().getFirst();
    PsiElement lastElement = parameter.getValues().isEmpty() ? parameter.getExpression() : parameter.getValues().getLast();
    int start = firstElement.getTextRange().getStartOffset();
    int end = lastElement.getTextRange().getEndOffset();

    PsiElement previousElement = DQLUtil.getPreviousElement(firstElement);
    if (previousElement != null && previousElement.getNode().getElementType() == DQLTypes.COMMA) {
      start = previousElement.getTextRange().getStartOffset();
    } else {
      PsiElement nextElement = DQLUtil.getNextElement(lastElement);
      if (nextElement != null && nextElement.getNode().getElementType() == DQLTypes.COMMA) {
        end = nextElement.getTextRange().getEndOffset();
      }
    }
    document.deleteString(start, end);
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
