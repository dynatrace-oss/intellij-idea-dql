package pl.thedeem.intellij.dpl.documentation;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.PsiElementBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.documentation.GenericDocumentationProvider;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.documentation.providers.*;
import pl.thedeem.intellij.dpl.psi.*;

public class DPLDocumentationProvider extends AbstractDocumentationProvider {
    @Override
    public @Nullable String generateDoc(@NotNull PsiElement element, @Nullable PsiElement originalElement) {
        GenericDocumentationProvider<?> provider = switch (element) {
            case DPLCommandKeyword keyword when keyword.getParent() instanceof DPLCommandExpression command ->
                    new DPLCommandDocumentationProvider(command);
            case DPLVariable variable -> new DPLVariableDocumentationProvider(variable);
            case DPLFieldName fieldName -> new DPLFieldNameDocumentationProvider(fieldName);
            case DPLDoubleQuotedString string when string.getStringContentElement() != null ->
                    new DPLStringDocumentationProvider(string.getStringContentElement());
            case DPLSingleQuotedString string when string.getStringContentElement() != null ->
                    new DPLStringDocumentationProvider(string.getStringContentElement());
            case DPLStringContentElement string -> new DPLStringDocumentationProvider(string);
            case DPLCharacterGroupContent regex -> new DPLCharacterClassDocumentationProvider(regex);
            case DPLParameterName parameter -> new ExpressionPartDocumentationProvider<>(
                    parameter,
                    DPLBundle.message("documentation.configurationParameter.type"),
                    "AllIcons.Actions.Edit"
            );
            case PsiElementBase generic -> new GenericDocumentationProvider<>(generic);
            default -> null;
        };
        return provider != null ? provider.generateDocumentation() : null;
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
