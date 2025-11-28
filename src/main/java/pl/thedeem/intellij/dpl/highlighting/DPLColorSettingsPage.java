package pl.thedeem.intellij.dpl.highlighting;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.DynatracePatternLanguage;
import pl.thedeem.intellij.dpl.DPLIcon;

import javax.swing.*;
import java.util.Map;

public class DPLColorSettingsPage implements ColorSettingsPage {
    private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
            new AttributesDescriptor(DPLBundle.message("settings.color.badValue"), DPLColorScheme.BAD_CHARACTER),
            new AttributesDescriptor(DPLBundle.message("settings.color.strings"), DPLColorScheme.STRING),
            new AttributesDescriptor(DPLBundle.message("settings.color.numbers"), DPLColorScheme.NUMBER),
            new AttributesDescriptor(DPLBundle.message("settings.color.booleans"), DPLColorScheme.BOOLEAN),
            new AttributesDescriptor(DPLBundle.message("settings.color.macros"), DPLColorScheme.MACRO),
            new AttributesDescriptor(DPLBundle.message("settings.color.nulls"), DPLColorScheme.NULL),
            new AttributesDescriptor(DPLBundle.message("settings.color.dataFields"), DPLColorScheme.FIELD_NAME),
            new AttributesDescriptor(DPLBundle.message("settings.color.colons"), DPLColorScheme.COLON),
            new AttributesDescriptor(DPLBundle.message("settings.color.semicolons"), DPLColorScheme.SEMICOLON),
            new AttributesDescriptor(DPLBundle.message("settings.color.lookaround"), DPLColorScheme.LOOKAROUND),
            new AttributesDescriptor(DPLBundle.message("settings.color.quantifiers"), DPLColorScheme.QUANTIFIERS),
            new AttributesDescriptor(DPLBundle.message("settings.color.sets"), DPLColorScheme.SET),
            new AttributesDescriptor(DPLBundle.message("settings.color.commas"), DPLColorScheme.COMMA),
            new AttributesDescriptor(DPLBundle.message("settings.color.brackets"), DPLColorScheme.BRACKETS),
            new AttributesDescriptor(DPLBundle.message("settings.color.braces"), DPLColorScheme.BRACES),
            new AttributesDescriptor(DPLBundle.message("settings.color.parentheses"), DPLColorScheme.PARENTHESES),
            new AttributesDescriptor(DPLBundle.message("settings.color.keywords"), DPLColorScheme.KEYWORD),
            new AttributesDescriptor(DPLBundle.message("settings.color.negation"), DPLColorScheme.NEGATION),
            new AttributesDescriptor(DPLBundle.message("settings.color.regex"), DPLColorScheme.REGEX),
    };

    @Override
    public Icon getIcon() {
        return DPLIcon.DYNATRACE_LOGO;
    }

    @NotNull
    @Override
    public SyntaxHighlighter getHighlighter() {
        return new DPLSyntaxHighlighter();
    }

    @NotNull
    @Override
    public String getDemoText() {
        return """
                $syslog_hdr = <keyword>TIMESTAMP</keyword>('MMM d HH:mm:ss'):<fieldName>ts</fieldName> ' ' <keyword>LD</keyword>:<fieldName>host</fieldName>;
                
                <keyword>LD</keyword>:<fieldName>username</fieldName> ';'
                !<<<keyword>ENUM</keyword>{
                    ''=-3, 'success'=0, 'Wrong password'=1, 'tech error'=2
                }(<configurationName>cis</configurationName>=true):<fieldName>result</fieldName> ';'
                (<keyword>LD</keyword>*:<fieldName>comment</fieldName> "SOME_STRING")
                (<keyword>IPADDR</keyword>:<fieldName>ip</fieldName> | <keyword>LD</keyword>:<fieldName>host</fieldName>):<fieldName>alt_grp</fieldName>
                >><keyword>JSON_ARRAY</keyword>{ <keyword>DOUBLE</keyword> }(<configurationName>typed</configurationName>=true):<fieldName>double_arr</fieldName>
                [{*}0-9{*}a-z]{4,15}+:<fieldName>username</fieldName> ',' $syslog_hdr
                <keyword>KVP</keyword>{
                 [a-z]:<fieldName>key</fieldName>
                 '='
                 <keyword>INT</keyword>:<fieldName>value</fieldName>
                 ' '?
                }*:<fieldName>attr</fieldName>
                <keyword>EOL</keyword>;
                """;
    }

    @Nullable
    @Override
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return Map.of(
                "keyword", DPLColorScheme.KEYWORD,
                "fieldName", DPLColorScheme.FIELD_NAME,
                "configurationName", DPLColorScheme.CONFIGURATION_NAME
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
        return DynatracePatternLanguage.DPL_DISPLAY_NAME;
    }

}
