package pl.thedeem.intellij.dql.code;

import com.intellij.lang.Language;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DynatracePatternLanguage;
import pl.thedeem.intellij.dql.definition.DQLParameterObject;
import pl.thedeem.intellij.dql.psi.DQLParameterExpression;
import pl.thedeem.intellij.dql.psi.DQLQueryStatement;
import pl.thedeem.intellij.dql.psi.DQLString;

import java.util.List;

public class DQLLanguagesInjector implements MultiHostInjector {
    @Override
    public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
        Language json = Language.findLanguageByID("JSON");

        if (context instanceof DQLQueryStatement statement) {
            String commandName = statement.getName();
            if (json != null && "data".equals(commandName)) {
                DQLParameterObject parameter = statement.findParameter("json");
                if (parameter != null && parameter.getExpression() instanceof DQLParameterExpression expression && expression.getExpression() instanceof DQLString string) {
                    registrar.startInjecting(json)
                            .addPlace("", "", string, string.getHostTextRange())
                            .doneInjecting();
                }
            } else if ("parse".equals(commandName)) {
                DQLParameterObject parameter = statement.findParameter("pattern");
                if (parameter != null && parameter.getExpression() != null && parameter.getExpression() instanceof DQLString string) {
                    registrar.startInjecting(DynatracePatternLanguage.INSTANCE)
                            .addPlace("", "", string, string.getHostTextRange())
                            .doneInjecting();
                }
            }
        }
    }

    @Override
    public @NotNull List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
        return List.of(DQLParameterExpression.class, DQLQueryStatement.class);
    }
}
