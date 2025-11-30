package pl.thedeem.intellij.dql.style;

import com.intellij.application.options.CodeStyle;
import com.intellij.lang.Language;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.style.DPLCodeStyleSettings;
import pl.thedeem.intellij.dql.psi.DQLMultilineString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DQLPostFormatProcessor implements PostFormatProcessor {
    private final static Pattern INDENT_PATTERN = Pattern.compile("^\\s*");

    @Override
    public @NotNull PsiElement processElement(@NotNull PsiElement element, @NotNull CodeStyleSettings settings) {
        return element;
    }

    @Override
    public @NotNull TextRange processText(@NotNull PsiFile hostFile, @NotNull TextRange range, @NotNull CodeStyleSettings settings) {
        Project project = hostFile.getProject();
        InjectedLanguageManager injector = InjectedLanguageManager.getInstance(project);
        CodeStyleManager styleManager = CodeStyleManager.getInstance(project);
        DQLCodeStyleSettings dqlSettings = settings.getCustomSettings(DQLCodeStyleSettings.class);
        Collection<PsiFile> injectedFiles = findInjectionHosts(hostFile, injector, range, settings);
        final PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);

        if (!injectedFiles.isEmpty()) {
            for (PsiFile host : injectedFiles) {
                PsiLanguageInjectionHost injectionHost = injector.getInjectionHost(host);
                if (injectionHost != null) {
                    PsiFile updated = PsiFileFactory.getInstance(project).createFileFromText(host.getLanguage(), host.getText());
                    CodeStyleSettings temporarySettings = createTemporarySettings(settings, injectionHost);
                    CodeStyle.runWithLocalSettings(project, temporarySettings, () -> {
                        styleManager.reformat(updated);
                        Document d = documentManager.getDocument(host);
                        if (d != null) {
                            d.setText(prepareFormattedText(updated, dqlSettings, injectionHost, documentManager));
                            documentManager.commitDocument(d);
                        }
                    });
                }
            }
        }
        return range;
    }

    private @NotNull Collection<PsiFile> findInjectionHosts(@NotNull PsiFile file, @NotNull InjectedLanguageManager injector, @NotNull TextRange range, @NotNull CodeStyleSettings settings) {
        PsiLanguageInjectionHost injectionHost = injector.getInjectionHost(file);
        List<PsiFile> result = new ArrayList<>();
        Collection<PsiLanguageInjectionHost> hosts;
        if (injectionHost == null) {
            hosts = PsiTreeUtil.findChildrenOfType(file, PsiLanguageInjectionHost.class);
        } else {
            hosts = List.of(injectionHost);
        }
        for (PsiLanguageInjectionHost host : hosts) {
            List<Pair<PsiElement, TextRange>> files = injector.getInjectedPsiFiles(host);
            if (files != null) {
                for (Pair<PsiElement, TextRange> iFile : files) {
                    if (iFile.getSecond().intersects(range)) {
                        PsiFile containingFile = iFile.getFirst().getContainingFile();
                        if (canReformat(containingFile, settings)) {
                            result.add(containingFile);
                        }
                    }
                }
            }
        }

        return result;
    }

    private boolean canReformat(@NotNull PsiFile containingFile, @NotNull CodeStyleSettings settings) {
        DQLCodeStyleSettings dqlSettings = settings.getCustomSettings(DQLCodeStyleSettings.class);
        return switch (containingFile.getLanguage().getID()) {
            case "DPL" -> dqlSettings.REFORMAT_DPL_FRAGMENTS;
            case "JSON" -> dqlSettings.REFORMAT_JSON_FRAGMENTS;
            default -> false;
        };
    }

    @SuppressWarnings("deprecation")
    private @NotNull CodeStyleSettings createTemporarySettings(@NotNull CodeStyleSettings base, @NotNull PsiLanguageInjectionHost host) {
        boolean isCodeBlock = host instanceof DQLMultilineString;
        CodeStyleSettings temp = base.clone(); // TODO: Find a non-deprecated way of cloning settings

        Language json = Language.findLanguageByID("JSON");
        Language dpl = Language.findLanguageByID("DPL");

        if (!isCodeBlock) {
            if (json != null) {
                com.intellij.json.formatter.JsonCodeStyleSettings jsonSettings = temp.getCustomSettings(com.intellij.json.formatter.JsonCodeStyleSettings.class);
                jsonSettings.ARRAY_WRAPPING = 0;
                jsonSettings.OBJECT_WRAPPING = 0;
                CommonCodeStyleSettings common = temp.getCommonSettings(json);
                common.WRAP_LONG_LINES = false;
                common.KEEP_LINE_BREAKS = false;
            }
            if (dpl != null) {
                DPLCodeStyleSettings dplSettings = temp.getCustomSettings(DPLCodeStyleSettings.class);
                dplSettings.WRAP_LONG_EXPRESSIONS = false;
                dplSettings.LB_BETWEEN_EXPRESSIONS = false;
                dplSettings.LB_AFTER_CONFIGURATION_PARAMETERS = false;
                dplSettings.LB_BETWEEN_EXPRESSIONS_IN_GROUPS = false;
                dplSettings.LB_AFTER_MACRO_DEFINITION = false;
                dplSettings.LB_AROUND_BRACE_IN_MATCHER = false;
                dplSettings.LB_AROUND_PARENTHESES_IN_GROUPS = false;

                var dplCommon = temp.getCommonSettings(dpl);
                dplCommon.WRAP_LONG_LINES = false;
                dplCommon.KEEP_LINE_BREAKS = false;
            }
        }
        return temp;
    }

    private String prepareFormattedText(PsiFile updated, DQLCodeStyleSettings dqlSettings, PsiLanguageInjectionHost injectionHost, PsiDocumentManager documentManager) {
        boolean codeBlock = injectionHost instanceof DQLMultilineString;
        String text = updated.getText().trim();
        int parentIndent = calculateParentIndent(injectionHost, documentManager);
        if (dqlSettings.KEEP_INDENT_FOR_INJECTED_FRAGMENTS && codeBlock) {
            text = text.indent(parentIndent).strip();
        }
        if (dqlSettings.SURROUND_INJECTED_FRAGMENTS_WITH_NEW_LINES && codeBlock) {
            String indent = " ".repeat(dqlSettings.KEEP_INDENT_FOR_INJECTED_FRAGMENTS ? parentIndent : 0);
            text = "\n" + indent + text + "\n" + indent;
        }
        return text;
    }

    @Override
    public boolean isWhitespaceOnly() {
        return false;
    }

    private int calculateParentIndent(@NotNull PsiLanguageInjectionHost host, @NotNull PsiDocumentManager manager) {
        final Document originalDocument = manager.getDocument(host.getContainingFile());
        if (originalDocument == null) {
            return 0;
        }
        int lineNumber = originalDocument.getLineNumber(host.getTextRange().getStartOffset());
        String lineText = originalDocument.getText(new TextRange(originalDocument.getLineStartOffset(lineNumber), originalDocument.getLineEndOffset(lineNumber)));
        Matcher matcher = INDENT_PATTERN.matcher(lineText);
        if (matcher.find()) {
            return matcher.group(0).length();
        }
        return 0;
    }
}
