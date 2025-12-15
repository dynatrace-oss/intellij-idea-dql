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

public class BaseTypedElementDocumentationProvider extends BaseDocumentationProvider {
    private final PsiElement element;

    public BaseTypedElementDocumentationProvider(@NotNull PsiElement element, @Nullable String type) {
        super(element, type);
        this.element = element;
    }

    @Override
    protected List<HtmlChunk.Element> getSections() {
        return List.of(buildTypeDescription());
    }

    protected HtmlChunk.Element buildTypeDescription() {
        Collection<String> types = element instanceof BaseElement el ? el.getDataType() : Set.of();
        return buildStandardSection(DQLBundle.message("definition.returnedValues"), DQLBundle.types(types, element.getProject()));
    }
}
