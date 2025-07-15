package pl.thedeem.intellij.dql.highlighting;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.DynatraceQueryLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

public class DQLColorSettingsPage implements ColorSettingsPage {
    private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
            new AttributesDescriptor(DQLBundle.message("settings.color.badValue"), DQLColorScheme.BAD_CHARACTER),
            new AttributesDescriptor(DQLBundle.message("settings.color.keywords"), DQLColorScheme.KEYWORD),
            new AttributesDescriptor(DQLBundle.message("settings.color.comments"), DQLColorScheme.COMMENT),
            new AttributesDescriptor(DQLBundle.message("settings.color.strings"), DQLColorScheme.STRING),
            new AttributesDescriptor(DQLBundle.message("settings.color.numbers"), DQLColorScheme.NUMBER),
            new AttributesDescriptor(DQLBundle.message("settings.color.booleans"), DQLColorScheme.BOOLEAN),
            new AttributesDescriptor(DQLBundle.message("settings.color.nulls"), DQLColorScheme.NULL),

            new AttributesDescriptor(DQLBundle.message("settings.color.dataFields"), DQLColorScheme.DATA_FIELD),
            new AttributesDescriptor(DQLBundle.message("settings.color.assignedDataFields"), DQLColorScheme.DATA_ASSIGNED_FIELD),
            new AttributesDescriptor(DQLBundle.message("settings.color.variables"), DQLColorScheme.VARIABLE),
            new AttributesDescriptor(DQLBundle.message("settings.color.names"), DQLColorScheme.STATEMENT_KEYWORD),
            new AttributesDescriptor(DQLBundle.message("settings.color.parameters"), DQLColorScheme.PARAMETER),
            new AttributesDescriptor(DQLBundle.message("settings.color.enumValues"), DQLColorScheme.ENUM_VALUE),
            new AttributesDescriptor(DQLBundle.message("settings.color.duration"), DQLColorScheme.DURATION),

            new AttributesDescriptor(DQLBundle.message("settings.color.functionNames"), DQLColorScheme.FUNCTION),
            new AttributesDescriptor(DQLBundle.message("settings.color.functionParameters"), DQLColorScheme.FUNCTION_PARAMETER),

            new AttributesDescriptor(DQLBundle.message("settings.color.symbols"), DQLColorScheme.OPERATOR),
            new AttributesDescriptor(DQLBundle.message("settings.color.semicolons"), DQLColorScheme.COLON),
            new AttributesDescriptor(DQLBundle.message("settings.color.signs"), DQLColorScheme.SET),
            new AttributesDescriptor(DQLBundle.message("settings.color.commas"), DQLColorScheme.COMMA),
            new AttributesDescriptor(DQLBundle.message("settings.color.brackets"), DQLColorScheme.BRACKETS),
            new AttributesDescriptor(DQLBundle.message("settings.color.braces"), DQLColorScheme.BRACES),
            new AttributesDescriptor(DQLBundle.message("settings.color.parentheses"), DQLColorScheme.PARENTHESES),
    };

    @Override
    public Icon getIcon() {
        return DQLIcon.DYNATRACE_LOGO;
    }

    @NotNull
    @Override
    public SyntaxHighlighter getHighlighter() {
        return new DQLSyntaxHighlighter();
    }

    @NotNull
    @Override
    public String getDemoText() {
        return """
                <keyword>data</keyword> <function>record</function>(<dataField>my.favourite.language</dataField> = "Dynatrace Query Language")
                | <keyword>fieldsAdd</keyword> {
                    <dataField>size</dataField> = <function>stringLength</function>(<dataField>my.favourite.language</dataField>),
                    <dataField>other_example</dataField> = 10 * <function>power</function>(10, 100),
                    <dataField>`Can you have named fields?`</dataField> = true,
                    <dataField>variable_value</dataField> = $someVariable,
                    <dataField>duration_value</dataField> = <duration>12w</duration>,
                    <dataField>empty_value</dataField> = null
                }
                // and this is a comment
                | <keyword>filter</keyword> <function>matchesValue</function>(<dataField>my.favourite.language</dataField>, "*Dynatrace*", <functionParameter>caseSensitive</functionParameter>: false)
                                            and (55 * <dataField>other_example</dataField>) >= <dataField>size</dataField>
                | <keyword>dedup</keyword> <dataField>my.favourite.language</dataField>, sort: <dataField>size</dataField> <sortKeyword>desc</sortKeyword>
                
                | <keyword>lookup</keyword> [
                   <keyword>describe</keyword> <dataField>some.field</dataField>
                ], sourceField: <globalDataField>some.field</globalDataField>, lookupField: <globalDataField>some.field2</globalDataField>, executionOrder: <enumValue>rightFirst</enumValue>
                """;
    }

    @Nullable
    @Override
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return Map.of(
                "keyword", DQLColorScheme.STATEMENT_KEYWORD,
                "function", DQLColorScheme.FUNCTION,
                "dataField", DQLColorScheme.DATA_ASSIGNED_FIELD,
                "globalDataField", DQLColorScheme.DATA_FIELD,
                "functionParameter", DQLColorScheme.FUNCTION_PARAMETER,
                "sortKeyword", DQLColorScheme.KEYWORD,
                "enumValue", DQLColorScheme.ENUM_VALUE,
                "duration", DQLColorScheme.DURATION
        );
    }

    @Override
    public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
        return DESCRIPTORS;
    }

    @Override
    public ColorDescriptor @NotNull [] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return DynatraceQueryLanguage.DQL_DISPLAY_NAME;
    }

}
