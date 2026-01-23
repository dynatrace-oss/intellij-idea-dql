package pl.thedeem.intellij.dql.documentation.providers;

import com.intellij.openapi.util.text.HtmlChunk;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.psi.elements.BaseElement;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class BaseTypedElementDocumentationProvider<T extends PsiElement> extends BaseDocumentationProvider<T> {
    public BaseTypedElementDocumentationProvider(@NotNull T element, @Nullable String type, @Nullable String icon) {
        super(element, type, icon);
    }

    @Override
    protected @NotNull List<HtmlChunk> getSections() {
        return List.of(buildTypeDescription());
    }

    protected @NotNull HtmlChunk buildTypeDescription() {
        Collection<String> types = element instanceof BaseElement el ? el.getDataType() : Set.of();
        return buildTitledSection(
                DQLBundle.message("definition.returnedValues"),
                prepareValuesDescription(types, getProject())
        );
    }
}
