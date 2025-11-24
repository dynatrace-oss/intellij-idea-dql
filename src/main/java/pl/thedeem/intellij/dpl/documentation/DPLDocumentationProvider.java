package pl.thedeem.intellij.dpl.documentation;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.PsiElementBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dpl.documentation.providers.*;
import pl.thedeem.intellij.dpl.psi.*;

public class DPLDocumentationProvider extends AbstractDocumentationProvider {
    @Override
    public @Nullable String generateDoc(@NotNull PsiElement element, @Nullable PsiElement originalElement) {
        return switch (element) {
            case DPLCommandKeyword keyword when keyword.getParent() instanceof DPLCommandExpression command ->
                    new DPLCommandDocumentationProvider(command).generateDocumentation();
            case DPLVariable variable -> new DPLVariableDocumentationProvider(variable).generateDocumentation();
            case DPLFieldName fieldName -> new DPLFieldNameDocumentationProvider(fieldName).generateDocumentation();
            case DPLDoubleQuotedString string when string.getStringContentElement() != null ->
                    new DPLStringDocumentationProvider(string.getStringContentElement()).generateDocumentation();
            case DPLSingleQuotedString string when string.getStringContentElement() != null ->
                    new DPLStringDocumentationProvider(string.getStringContentElement()).generateDocumentation();
            case DPLStringContentElement string -> new DPLStringDocumentationProvider(string).generateDocumentation();
            case DPLCharacterGroupContent regex ->
                    new DPLCharacterClassDocumentationProvider(regex).generateDocumentation();
            case DPLParameterName parameter when parameter.getParent().getParent() instanceof DPLConfiguration configuration ->
                    new DPLConfigurationDocumentationProvider(configuration).generateDocumentation();
            case PsiElementBase generic -> new BaseElementDocumentationProvider(generic).generateDocumentation();
            default -> null;
        };
    }

    @Override
    public @Nullable String generateHoverDoc(@NotNull PsiElement element, @Nullable PsiElement originalElement) {
        return generateDoc(element, originalElement);
    }

    @Override
    public @Nullable PsiElement getCustomDocumentationElement(
            @NotNull Editor editor,
            @NotNull PsiFile file,
            @Nullable PsiElement context,
            int targetOffset) {
        PsiElement result = super.getCustomDocumentationElement(editor, file, context, targetOffset);
        if (context instanceof PsiElement psi && psi.getParent() instanceof DPLStringContentElement content && content.getParent() instanceof DPLString string && string.getParent() instanceof DPLFieldName fieldName) {
            return fieldName;
        }
        if (context instanceof PsiElement psi && psi.getParent() instanceof DPLString string && string.getParent() instanceof DPLFieldName fieldName) {
            return fieldName;
        }
        return result;
    }
}
