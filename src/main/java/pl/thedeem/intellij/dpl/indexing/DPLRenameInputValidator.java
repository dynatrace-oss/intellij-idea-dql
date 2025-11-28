package pl.thedeem.intellij.dpl.indexing;

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.rename.RenameInputValidator;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.psi.DPLFieldName;
import pl.thedeem.intellij.dpl.psi.DPLTypes;
import pl.thedeem.intellij.dpl.psi.DPLVariable;

import java.util.regex.Pattern;

public class DPLRenameInputValidator implements RenameInputValidator {
    private static final Pattern VALID_FIELD_NAME = Pattern.compile("('[^']+')|(\"[^\"]+\")|([a-zA-Z_][a-zA-Z0-9_.]*)");
    private static final Pattern VALID_VARIABLE = Pattern.compile("\\$[a-zA-Z_][a-zA-Z0-9_]*");

    @Override
    public @NotNull ElementPattern<? extends PsiElement> getPattern() {
        return PlatformPatterns.or(
                PlatformPatterns.psiElement(DPLTypes.VARIABLE),
                PlatformPatterns.psiElement(DPLTypes.FIELD_NAME)
        );
    }

    @Override
    public boolean isInputValid(@NotNull String newName, @NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
        if (psiElement instanceof DPLVariable) {
            return VALID_VARIABLE.matcher(newName).matches();
        }
        if (psiElement instanceof DPLFieldName) {
            return VALID_FIELD_NAME.matcher(newName).matches();
        }
        return false;
    }
}
