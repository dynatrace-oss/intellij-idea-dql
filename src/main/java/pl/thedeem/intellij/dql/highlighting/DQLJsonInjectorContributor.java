package pl.thedeem.intellij.dql.highlighting;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.Language;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.psi.LiteralTextEscaper;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.impl.source.tree.injected.StringLiteralEscaper;
import pl.thedeem.intellij.dql.psi.DQLParameterExpression;
import pl.thedeem.intellij.dql.psi.DQLString;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DQLJsonInjectorContributor implements MultiHostInjector {

    @Override
    public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
        Language json = Language.findLanguageByID("JSON");
        if (json != null && context instanceof DQLParameterExpression argument) {
            String name = argument.getName();
            if ("json".equals(name)) {
                if (argument.getExpression() instanceof DQLString string && argument.getExpression() instanceof ASTWrapperPsiElement psiElement) {
                    DQLJsonField host = new DQLJsonField(psiElement);
                    registrar.startInjecting(json)
                            .addPlace(null, null, host, string.getHostTextRange())
                            .doneInjecting();
                }
            }
        }
    }

    @Override
    public @NotNull List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
        return List.of(DQLParameterExpression.class);
    }

    private static class DQLJsonField extends ASTWrapperPsiElement implements PsiLanguageInjectionHost {
        public DQLJsonField(@NotNull ASTWrapperPsiElement element) {
            super(element.getNode());
        }

        @Override
        public boolean isValidHost() {
            return true;
        }

        @Override
        public PsiLanguageInjectionHost updateText(@NotNull String s) {
            return null;
        }

        @Override
        public @NotNull LiteralTextEscaper<? extends PsiLanguageInjectionHost> createLiteralTextEscaper() {
            return new StringLiteralEscaper<>(this);
        }
    }
}
