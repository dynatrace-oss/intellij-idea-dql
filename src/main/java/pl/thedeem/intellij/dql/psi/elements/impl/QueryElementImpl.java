package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.Key;
import com.intellij.psi.util.*;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.psi.DQLVariableExpression;
import pl.thedeem.intellij.dql.psi.elements.QueryElement;
import pl.thedeem.intellij.dql.psi.elements.VariableElement;
import pl.thedeem.intellij.dql.services.query.DQLFieldNamesService;

import java.util.*;

public abstract class QueryElementImpl extends ASTWrapperPsiElement implements QueryElement {
    private static final Key<CachedValue<Map<String, List<VariableElement>>>> CACHED_DEFINED_VARIABLES =
            Key.create("DQL_QUERY_DEFINED_VARIABLES");

    public QueryElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(DQLBundle.message("presentation.query"), this, DQLIcon.DYNATRACE_LOGO);
    }

    @Override
    public String getFieldName() {
        return DQLFieldNamesService.getInstance().calculateFieldName(getName());
    }

    @Override
    public @NotNull Map<String, List<VariableElement>> getDefinedVariables() {
        CachedValue<Map<String, List<VariableElement>>> cached = getUserData(CACHED_DEFINED_VARIABLES);
        if (cached == null) {
            cached = CachedValuesManager.getManager(getProject()).createCachedValue(
                    () -> new CachedValueProvider.Result<>(recalculateVariables(),
                            PsiModificationTracker.MODIFICATION_COUNT
                    ),
                    false
            );
            putUserData(CACHED_DEFINED_VARIABLES, cached);
        }
        return Objects.requireNonNullElse(cached.getValue(), Map.of());
    }

    private @NotNull Map<String, List<VariableElement>> recalculateVariables() {
        Map<String, List<VariableElement>> result = new HashMap<>();
        for (DQLVariableExpression var : PsiTreeUtil.findChildrenOfType(this, DQLVariableExpression.class)) {
            String name = var.getName();
            result.computeIfAbsent(name, k -> new ArrayList<>()).add(var);
        }
        return result;
    }
}
