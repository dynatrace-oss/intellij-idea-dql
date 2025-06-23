package pl.thedeem.intellij.dql.indexing;

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.rename.RenameInputValidator;
import com.intellij.util.ProcessingContext;
import pl.thedeem.intellij.dql.psi.DQLFieldExpression;
import pl.thedeem.intellij.dql.psi.DQLVariableExpression;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class DQLRenameInputValidator implements RenameInputValidator {
    private static final Pattern VALID_IDENTIFIER = Pattern.compile("`[^`]+`|[a-zA-Z_][a-zA-Z0-9_.]*");
    private static final Pattern VALID_VARIABLE = Pattern.compile("\\$[a-zA-Z_][a-zA-Z0-9_]*");

    @Override
    public @NotNull ElementPattern<? extends PsiElement> getPattern() {
        return PlatformPatterns.or(
                PlatformPatterns.psiElement(DQLFieldExpression.class),
                PlatformPatterns.psiElement(DQLVariableExpression.class)
        );
    }

    @Override
    public boolean isInputValid(@NotNull String newName, @NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
        if (psiElement instanceof DQLFieldExpression) {
            return VALID_IDENTIFIER.matcher(newName).matches();
        }
        if (psiElement instanceof DQLVariableExpression) {
            return VALID_VARIABLE.matcher(newName).matches();
        }
        return false;
    }
}
