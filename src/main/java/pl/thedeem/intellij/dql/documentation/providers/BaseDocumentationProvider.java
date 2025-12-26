package pl.thedeem.intellij.dql.documentation.providers;

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
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.definition.model.ParameterValueType;
import pl.thedeem.intellij.dql.services.definition.DQLDefinitionService;

import java.util.List;

public class BaseDocumentationProvider {
    private final String name;
    private final String type;
    private final PsiElement element;

    public BaseDocumentationProvider(@NotNull PsiElement element, @Nullable String type) {
        this.element = element;
        this.name = getName(element);
        this.type = type;
    }

    public BaseDocumentationProvider(@NotNull PsiElement element) {
        this.element = element;
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

    protected HtmlChunk.Element buildStandardSection(String title, HtmlChunk value) {
        return DocumentationMarkup.SECTIONS_TABLE
                .child(HtmlChunk.tag("tr").child(DocumentationMarkup.SECTION_HEADER_CELL.addText(title)))
                .child(HtmlChunk.tag("tr").child(DocumentationMarkup.SECTION_CONTENT_CELL.child(value)));
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

    protected HtmlChunk.Element buildSyntaxSection(String syntax) {
        return buildStandardSection(DQLBundle.message("documentation.statement.syntax"), syntax);
    }

    protected HtmlChunk.Element buildParametersDescription(List<Parameter> parameters) {
        HtmlChunk.Element table = DocumentationMarkup.SECTIONS_TABLE.style("width: 100%")
                .child(HtmlChunk.tag("tr")
                        .child(DocumentationMarkup.SECTION_HEADER_CELL.addText(DQLBundle.message("documentation.parameter.name")))
                        .child(DocumentationMarkup.SECTION_HEADER_CELL.addText(DQLBundle.message("documentation.parameter.returnValue")))
                        .child(DocumentationMarkup.SECTION_HEADER_CELL.addText(DQLBundle.message("documentation.parameter.attributes")))
                );
        for (Parameter parameter : parameters) {
            if (!parameter.hidden()) {
                table = table.child(HtmlChunk.tag("tr")
                        .child(DocumentationMarkup.SECTION_CONTENT_CELL.addText(parameter.name()))
                        .child(DocumentationMarkup.SECTION_CONTENT_CELL.child(describeParameterTypes(parameter, element.getProject())))
                        .child(DocumentationMarkup.SECTION_CONTENT_CELL.child(describeParameterAttributes(parameter)))
                );
            }
        }
        return table;
    }

    protected HtmlChunk.Element describeParameterTypes(@NotNull Parameter parameter, @NotNull Project project) {
        HtmlChunk.Element result = HtmlChunk.span();
        DQLDefinitionService service = DQLDefinitionService.getInstance(project);
        boolean added = false;
        if (parameter.parameterValueTypes() != null) {
            for (String parameterValueType : parameter.parameterValueTypes()) {
                ParameterValueType type = service.findParameterValueType(parameterValueType);
                if (type != null) {
                    if (added) {
                        result = result.child(HtmlChunk.br());
                    }
                    result = result.child(HtmlChunk.span().addText(type.name()));
                    added = true;
                }
            }
        }
        if (parameter.valueTypes() != null) {
            if (added) {
                result = result.child(HtmlChunk.br());
            }
            result = result.child(HtmlChunk.span().addText(DQLBundle.types(parameter.valueTypes(), element.getProject())));
        }
        return result;
    }


    protected HtmlChunk.Element describeParameterAttributes(@NotNull Parameter parameter) {
        HtmlChunk.Element result = HtmlChunk.span();
        result = result.child(HtmlChunk.span().addText(parameter.required() ? DQLBundle.message("documentation.parameter.required")
                : DQLBundle.message("documentation.parameter.optional")));
        if (parameter.experimental()) {
            result = result.child(HtmlChunk.br()).child(HtmlChunk.span().addText(DQLBundle.message("documentation.parameter.experimental")));
        }
        if (parameter.variadic()) {
            result = result.child(HtmlChunk.br()).child(HtmlChunk.span().addText(DQLBundle.message("documentation.parameter.variadic")));
        }
        return result;
    }

    protected HtmlChunk.Element buildMoreInfoLink(String link) {
        return DocumentationMarkup.BOTTOM_ELEMENT
                .child(HtmlChunk.div().addText(DQLBundle.message("documentation.moreInformationLink")))
                .child(HtmlChunk.link(link, DocumentationMarkup.EXTERNAL_LINK_ICON.addText(link)));
    }
}
