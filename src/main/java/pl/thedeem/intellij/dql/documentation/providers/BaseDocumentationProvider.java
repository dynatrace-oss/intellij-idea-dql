package pl.thedeem.intellij.dql.documentation.providers;

import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.HtmlChunk;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.documentation.GenericDocumentationProvider;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.DataType;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.definition.model.ParameterValueType;
import pl.thedeem.intellij.dql.services.definition.DQLDefinitionService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class BaseDocumentationProvider<T extends PsiElement> extends GenericDocumentationProvider<T> {
    public BaseDocumentationProvider(@NotNull T element, @Nullable String type, @Nullable String icon) {
        super(element, type, icon);
    }

    public BaseDocumentationProvider(@NotNull T element, @Nullable String name, @Nullable String type, @Nullable String icon) {
        super(element, name, type, icon);
    }

    public BaseDocumentationProvider(@NotNull T element) {
        this(element, null, null);
    }

    public @Nullable String generateDocumentation() {
        return Objects.requireNonNullElse(super.generateDocumentation(), DQLBundle.message("documentation.missingDocs"));
    }

    protected @NotNull HtmlChunk buildSyntaxSection(@NotNull String syntax) {
        return buildTitledSection(
                DQLBundle.message("documentation.statement.syntax"),
                buildCodeBlock(syntax)
        );
    }

    protected @NotNull HtmlChunk buildParametersDescription(@NotNull List<Parameter> parameters) {
        HtmlChunk.Element table = DocumentationMarkup.SECTIONS_TABLE.style("width: 100%");
        for (Parameter parameter : parameters) {
            if (!parameter.hidden()) {
                table = table.child(HtmlChunk.tag("tr")
                        .child(DocumentationMarkup.SECTION_CONTENT_CELL
                                .child(DocumentationMarkup.GRAYED_ELEMENT.addText(parameter.name()).child(HtmlChunk.span().addText(": ")))
                                .child(prepareParameterAttributesDescription(parameter))
                        )
                        .child(DocumentationMarkup.SECTION_CONTENT_CELL.child(describeParameter(parameter, getProject(), false)))
                );
            }
        }
        return HtmlChunk.div()
                .child(DocumentationMarkup.GRAYED_ELEMENT.addText(DQLBundle.message("documentation.parameters")))
                .child(HtmlChunk.hr())
                .child(table);
    }

    protected @NotNull HtmlChunk describeParameter(@Nullable Parameter parameter, @NotNull Project project, boolean main) {
        if (parameter == null) {
            return HtmlChunk.span().addText(DQLBundle.message("documentation.parameter.unknown"));
        }
        List<HtmlChunk> sections = new ArrayList<>();
        sections.add(HtmlChunk.span().addText(Objects.requireNonNullElse(parameter.description(), DQLBundle.message("documentation.parameter.noDescription"))));
        HtmlChunk values = prepareValuesDescription(parameter.valueTypes(), project);
        HtmlChunk types = prepareParameterTypesDescription(parameter, project);
        sections.add(main ?
                buildTitledSection(DQLBundle.message("documentation.parameter.allowedValues"), values)
                : buildEmbeddedSection(DQLBundle.message("documentation.parameter.allowedValues"), values));
        sections.add(main ?
                buildTitledSection(DQLBundle.message("documentation.parameter.types"), types)
                : buildEmbeddedSection(DQLBundle.message("documentation.parameter.types"), types));
        return HtmlChunk.span().children(sections);
    }

    protected @NotNull HtmlChunk prepareValuesDescription(@Nullable Collection<String> valueTypes, @NotNull Project project) {
        DQLDefinitionService service = DQLDefinitionService.getInstance(project);
        if (valueTypes == null) {
            return HtmlChunk.empty();
        }
        if (valueTypes.isEmpty()) {
            return HtmlChunk.span().addText(DQLBundle.message("documentation.parameter.unknownValue"));
        }
        return buildSeparatedElements(
                valueTypes.stream().map(service::findDataType).filter(Objects::nonNull).map(DataType::name).toList(),
                HtmlChunk.tag("tt")
        );
    }

    protected @NotNull HtmlChunk prepareParameterTypesDescription(@NotNull Parameter parameter, @NotNull Project project) {
        DQLDefinitionService service = DQLDefinitionService.getInstance(project);
        if (parameter.valueTypes() != null) {
            return buildSeparatedElements(
                    parameter.parameterValueTypes().stream()
                            .map(service::findParameterValueType)
                            .filter(Objects::nonNull).map(ParameterValueType::name).toList(),
                    HtmlChunk.tag("em")
            );
        }
        return HtmlChunk.empty();
    }

    protected @NotNull HtmlChunk prepareParameterAttributesDescription(@Nullable Parameter parameter) {
        if (parameter == null) {
            return HtmlChunk.empty();
        }
        List<String> tags = new ArrayList<>();
        tags.add(parameter.required() ? DQLBundle.message("documentation.parameter.required")
                : DQLBundle.message("documentation.parameter.optional"));
        if (parameter.experimental()) {
            tags.add(DQLBundle.message("documentation.parameter.experimental"));
        }
        if (parameter.variadic()) {
            tags.add(DQLBundle.message("documentation.parameter.variadic"));
        }
        return HtmlChunk.p().style("margin: 3px 0 5px 0").child(buildSeparatedElements(tags, tagElement()));
    }

    protected @NotNull HtmlChunk buildMoreInfoLink(@NotNull String link) {
        return DocumentationMarkup.BOTTOM_ELEMENT
                .child(HtmlChunk.div().addText(DQLBundle.message("documentation.moreInformationLink")))
                .child(HtmlChunk.link(link, DocumentationMarkup.EXTERNAL_LINK_ICON.addText(link)));
    }
}
