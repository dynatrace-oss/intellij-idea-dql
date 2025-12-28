package pl.thedeem.intellij.dql.code;

import com.intellij.lang.Language;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dpl.DynatracePatternLanguage;
import pl.thedeem.intellij.dql.services.definition.DQLDefinitionService;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.psi.DQLCommand;
import pl.thedeem.intellij.dql.psi.DQLFunctionExpression;
import pl.thedeem.intellij.dql.psi.DQLParameterExpression;
import pl.thedeem.intellij.dql.psi.DQLString;
import pl.thedeem.intellij.dql.psi.elements.DQLParametersOwner;

import java.util.List;
import java.util.function.Predicate;

public class DQLLanguagesInjector implements MultiHostInjector {
    @Override
    public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
        Language json = Language.findLanguageByID("JSON");
        Language dpl = DynatracePatternLanguage.INSTANCE;

        if (context instanceof DQLParametersOwner paramsOwner) {
            for (MappedParameter parameter : paramsOwner.getParameters()) {
                injectLanguagesInParameter(parameter, json, (List<String> paramTypes) -> paramTypes.contains("dql.dataType.json"), registrar);
                injectLanguagesInParameter(parameter, dpl, (List<String> paramTypes) -> paramTypes.stream().anyMatch(DQLDefinitionService.DPL_VALUE_TYPES::contains), registrar);
            }
        }
    }

    private void injectLanguagesInParameter(@NotNull MappedParameter parameter, @Nullable Language language, @NotNull Predicate<List<String>> filter, @NotNull MultiHostRegistrar registrar) {
        if (language == null) {
            return;
        }
        Parameter definition = parameter.definition();
        if (definition == null || definition.parameterValueTypes() == null || !filter.test(definition.parameterValueTypes())) {
            return;
        }
        for (PsiElement expression : parameter.getExpressions()) {
            PsiElement toCheck = expression instanceof DQLParameterExpression param ? param.getExpression() : expression;
            if (toCheck instanceof DQLString string) {
                registrar.startInjecting(language)
                        .addPlace("", "", string, string.getHostTextRange())
                        .doneInjecting();
            }
        }
    }

    @Override
    public @NotNull List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
        return List.of(DQLParameterExpression.class, DQLCommand.class, DQLFunctionExpression.class);
    }
}
