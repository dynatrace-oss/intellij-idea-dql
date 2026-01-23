package pl.thedeem.intellij.common.documentation;

import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.HtmlChunk;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.impl.PsiElementBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class GenericDocumentationProvider<T extends PsiElement> {
    private final String name;
    private final String type;
    private final String icon;
    protected final T element;

    public GenericDocumentationProvider(@NotNull T element) {
        this(element, null, null, null);
    }

    public GenericDocumentationProvider(@NotNull T element, @Nullable String type, @Nullable String icon) {
        this(element, getName(element), type, icon);
    }

    public GenericDocumentationProvider(@NotNull T element, @Nullable String name, @Nullable String type, @Nullable String icon) {
        this.element = element;
        this.name = name;
        this.type = type;
        this.icon = icon;
    }

    public @Nullable String generateDocumentation() {
        if (name == null && type == null) {
            return null;
        }
        return build().toString();
    }

    protected @NotNull Project getProject() {
        return element.getProject();
    }

    protected @NotNull HtmlChunk build() {
        HtmlChunk.Element definition = DocumentationMarkup.DEFINITION_ELEMENT;
        definition = definition.child(buildHeader());
        definition = definition.child(DocumentationMarkup.CONTENT_ELEMENT);
        for (HtmlChunk section : getSections()) {
            definition = definition.child(section);
        }
        return definition;
    }

    protected @NotNull HtmlChunk buildHeader() {
        HtmlChunk.Element header = DocumentationMarkup.PRE_ELEMENT;
        if (StringUtil.isNotEmpty(name)) {
            header = header.child(HtmlChunk.span().addText(this.name).attr("style", "padding-right: 10px;"));
        }
        HtmlChunk.Element typeInfo = HtmlChunk.span().style("display: inline-block; text-align: right;");
        if (icon != null) {
            typeInfo = typeInfo.child(HtmlChunk.tag("icon").attr("src", icon));
        }
        if (StringUtil.isNotEmpty(type)) {
            typeInfo = typeInfo.child(DocumentationMarkup.GRAYED_ELEMENT.addText(this.type).style("padding-left: 5px;"));
        }
        return header.child(typeInfo);
    }

    protected @NotNull List<HtmlChunk> getSections() {
        return List.of();
    }

    protected @NotNull HtmlChunk buildTitledSection(@NotNull String title, @NotNull HtmlChunk value) {
        return HtmlChunk.div()
                .child(DocumentationMarkup.GRAYED_ELEMENT.style("padding-top: 5px;").addText(title)
                        .child(HtmlChunk.hr())
                        .child(value));
    }

    protected @NotNull HtmlChunk buildEmbeddedSection(@NotNull String title, @NotNull HtmlChunk value) {
        if (value.isEmpty()) {
            return HtmlChunk.empty();
        }
        return HtmlChunk.p()
                .child(HtmlChunk.tag("strong").style("padding-top: 10px;").addText(title))
                .child(HtmlChunk.span().addText(": "))
                .child(value);
    }

    protected static @Nullable String getName(@Nullable PsiElement element) {
        if (element == null) {
            return null;
        }
        if (element instanceof PsiElementBase base) {
            ItemPresentation presentation = base.getPresentation();
            if (presentation != null) {
                return presentation.getPresentableText();
            }
        }
        if (element instanceof PsiNamedElement named) {
            return named.getName();
        }
        return null;
    }

    protected @NotNull HtmlChunk buildDescription(@NotNull String description) {
        return HtmlChunk.p().addText(description);
    }

    protected @NotNull HtmlChunk buildSeparatedElements(
            @NotNull Collection<String> elements,
            @NotNull HtmlChunk.Element tag
    ) {
        HtmlChunk.Element result = HtmlChunk.p();
        boolean shouldSeparate = false;
        for (String el : elements) {
            if (shouldSeparate) {
                result = result.child(HtmlChunk.span().addText(", "));
            }
            result = result.child(tag.addText(el));
            shouldSeparate = true;
        }
        return result;
    }

    protected @NotNull HtmlChunk buildCodeBlock(@NotNull String code) {
        return HtmlChunk.tag("pre").child(HtmlChunk.tag("code").addText(code));
    }

    protected @NotNull HtmlChunk.Element tagElement() {
        return HtmlChunk.tag("code").style("white-space:nowrap;");
    }
}
