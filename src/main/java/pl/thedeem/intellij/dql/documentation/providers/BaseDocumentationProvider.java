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
        return Objects.requireNonNullElseGet(super.generateDocumentation(), () -> DQLBundle.message("documentation.missingDocs"));
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
        sections.add(HtmlChunk.span().addText(Objects.requireNonNullElseGet(
                parameter.description(), () -> DQLBundle.message("documentation.parameter.noDescription")
        )));
        HtmlChunk values = prepareValuesDescription(parameter.valueTypes(), project);
        if (!values.isEmpty()) {
            sections.add(main ?
                    buildTitledSection(DQLBundle.message("documentation.parameter.allowedValues"), values)
                    : buildEmbeddedSection(DQLBundle.message("documentation.parameter.allowedValues"), values));
        }
        HtmlChunk enumValues = prepareEnumValuesDescription(parameter);
        if (!enumValues.isEmpty()) {
            sections.add(main ?
                    buildTitledSection(DQLBundle.message("documentation.parameter.allowedEnumValues"), enumValues)
                    : buildEmbeddedSection(DQLBundle.message("documentation.parameter.allowedEnumValues"), enumValues));
        }
        HtmlChunk types = prepareParameterTypesDescription(parameter, project);
        if (!types.isEmpty()) {
            sections.add(main ?
                    buildTitledSection(DQLBundle.message("documentation.parameter.types"), types)
                    : buildEmbeddedSection(DQLBundle.message("documentation.parameter.types"), types));
        }
        if (parameter.minValue() != null || parameter.maxValue() != null) {
            List<String> limits = new ArrayList<>();
            if (parameter.minValue() != null) {
                limits.add(DQLBundle.message("documentation.parameter.minValue", parameter.minValue()));
            }
            if (parameter.maxValue() != null) {
                limits.add(DQLBundle.message("documentation.parameter.maxValue", parameter.maxValue()));
            }
            HtmlChunk limitsElement = buildSeparatedElements(limits, HtmlChunk.tag("span"));
            sections.add(main ?
                    buildTitledSection(DQLBundle.message("documentation.parameter.rangeLimits"), limitsElement)
                    : buildEmbeddedSection(DQLBundle.message("documentation.parameter.rangeLimits"), limitsElement));
        }
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

    protected @NotNull HtmlChunk prepareEnumValuesDescription(@NotNull Parameter parameter) {
        if (parameter.allowedEnumValues() != null) {
            return buildSeparatedElements(parameter.allowedEnumValues(), HtmlChunk.tag("tt"));
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
