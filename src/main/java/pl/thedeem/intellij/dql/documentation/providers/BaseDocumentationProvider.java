package pl.thedeem.intellij.dql.documentation.providers;

import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.text.HtmlChunk;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.impl.PsiElementBase;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.DQLParameterDefinition;

import java.util.List;

public class BaseDocumentationProvider {
    private final String name;
    private final String type;

    public BaseDocumentationProvider(@Nullable PsiElement element, @Nullable String type) {
        this.name = getName(element);
        this.type = type;
    }

    public BaseDocumentationProvider() {
        this.name = null;
        this.type = null;
    }

    public @Nullable String buildDocumentation() {
        if (name == null && type == null) {
            return DQLBundle.message("documentation.missingDocs");
        }
        return build().toString();
    }

    protected HtmlChunk.Element build() {
        HtmlChunk.Element definition = DocumentationMarkup.DEFINITION_ELEMENT;
        HtmlChunk.Element header = DocumentationMarkup.PRE_ELEMENT;
        if (StringUtil.isNotEmpty(name)) {
            header = header.child(HtmlChunk.span().addText(this.name).attr("style", "padding-right: 10px;"));
        }
        if (StringUtil.isNotEmpty(type)) {
            header = header.child(DocumentationMarkup.GRAYED_ELEMENT.addText(this.type));
        }
        definition = definition.child(header);
        definition = definition.child(DocumentationMarkup.CONTENT_ELEMENT);
        for (HtmlChunk.Element section : getSections()) {
            definition = definition.child(section);
        }
        return definition;
    }

    protected List<HtmlChunk.Element> getSections() {
        return List.of();
    }

    protected HtmlChunk.Element buildStandardSection(String title, String value) {
        return DocumentationMarkup.SECTIONS_TABLE
                .child(HtmlChunk.tag("tr").child(DocumentationMarkup.SECTION_HEADER_CELL.addText(title)))
                .child(HtmlChunk.tag("tr").child(DocumentationMarkup.SECTION_CONTENT_CELL.child(DocumentationMarkup.PRE_ELEMENT.addText(value))));
    }

    protected @Nullable String getName(PsiElement element) {
        if (element != null) {
            if (element instanceof PsiElementBase base) {
                ItemPresentation presentation = base.getPresentation();
                if (presentation != null) {
                    return presentation.getPresentableText();
                }
            } else if (element instanceof PsiNamedElement named) {
                return named.getName();
            }
        }
        return null;
    }

    protected HtmlChunk.Element buildSyntaxSection(List<String> syntax) {
        return buildStandardSection(DQLBundle.message("documentation.statement.syntax"), DQLBundle.print(syntax));
    }

    protected HtmlChunk.Element buildParametersDescription(List<DQLParameterDefinition> parameters) {
        HtmlChunk.Element table = DocumentationMarkup.SECTIONS_TABLE
                .child(HtmlChunk.tag("tr")
                        .child(DocumentationMarkup.SECTION_HEADER_CELL.addText(DQLBundle.message("documentation.parameter.name")))
                        .child(DocumentationMarkup.SECTION_HEADER_CELL.addText(DQLBundle.message("documentation.parameter.returnValue")))
                        .child(DocumentationMarkup.SECTION_HEADER_CELL.addText(DQLBundle.message("documentation.parameter.isRequired")))
                );
        for (DQLParameterDefinition parameter : parameters) {
            table = table.child(HtmlChunk.tag("tr")
                    .child(DocumentationMarkup.SECTION_CONTENT_CELL.addText(parameter.name))
                    .child(DocumentationMarkup.SECTION_CONTENT_CELL.child(DocumentationMarkup.PRE_ELEMENT.addText(DQLBundle.print(parameter.type))))
                    .child(DocumentationMarkup.SECTION_CONTENT_CELL.addText((parameter.required ?
                            DQLBundle.message("documentation.statement.requiredParameter")
                            : DQLBundle.message("documentation.statement.optionalParameter"))))
            );
        }
        return table;
    }

    protected HtmlChunk.Element buildMoreInfoLink(String link) {
        return DocumentationMarkup.BOTTOM_ELEMENT
                .child(HtmlChunk.div().addText(DQLBundle.message("documentation.moreInformationLink")))
                .child(HtmlChunk.link(link, DocumentationMarkup.EXTERNAL_LINK_ICON.addText(link)));
    }
}
