package pl.thedeem.intellij.dpl.documentation.providers;

import com.intellij.openapi.util.text.HtmlChunk;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.documentation.GenericDocumentationProvider;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.definition.model.Configuration;
import pl.thedeem.intellij.dpl.definition.model.ExpressionDescription;
import pl.thedeem.intellij.dpl.psi.DPLConfigurationExpression;
import pl.thedeem.intellij.dpl.psi.DPLExpressionDefinition;
import pl.thedeem.intellij.dpl.psi.DPLParameterName;

import java.util.*;

public class DPLConfigurationParameterDocumentationProvider extends GenericDocumentationProvider<DPLParameterName> {
    public DPLConfigurationParameterDocumentationProvider(@NotNull DPLParameterName element) {
        super(element, DPLBundle.message("documentation.configurationParameter.type"), "AllIcons.Actions.Edit");
    }

    @Override
    protected @NotNull List<HtmlChunk> getSections() {
        DPLExpressionDefinition def = PsiTreeUtil.getParentOfType(element, DPLExpressionDefinition.class);
        DPLConfigurationExpression configurationBlock = def != null ? def.getConfiguration() : null;
        ExpressionDescription definition = def != null ? def.getDefinition() : null;
        if (configurationBlock == null || definition == null) {
            return List.of();
        }
        String parameterName = Objects.requireNonNullElse(element.getName(), "");
        Map<String, Configuration> configuration = Objects.requireNonNullElse(definition.configuration(), Map.of());
        Configuration parameterDefinition = configuration.values().stream().filter(c -> parameterName.equalsIgnoreCase(c.name())).findFirst().orElse(null);
        if (parameterDefinition == null) {
            return List.of(buildDescription(DPLBundle.message("documentation.unknownParameter")));
        }

        List<HtmlChunk> sections = new ArrayList<>();
        sections.add(HtmlChunk.p().child(tagElement().addText(parameterDefinition.type())));
        sections.add(buildDescription(Objects.requireNonNullElseGet(
                parameterDefinition.description(),
                () -> DPLBundle.message("documentation.configuration.unknownParameter"))
        ));

        Set<String> names = parameterDefinition.names();
        if (names.size() > 1) {
            sections.add(buildTitledSection(
                    DPLBundle.message("documentation.configuration.aliases"),
                    buildSeparatedElements(names, HtmlChunk.tag("tt"))
            ));
        }
        if (parameterDefinition.defaultValue() != null) {
            sections.add(buildTitledSection(
                    DPLBundle.message("documentation.configuration.defaultValue"),
                    buildCodeBlock(String.valueOf(parameterDefinition.defaultValue()))
            ));
        }

        return sections;
    }
}
