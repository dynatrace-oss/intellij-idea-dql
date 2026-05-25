package pl.thedeem.intellij.dql.services.query;

import com.intellij.injected.editor.DocumentWindow;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.LiteralTextEscaper;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.psi.DQLQuery;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class DQLQuerySelectorServiceImpl implements DQLQuerySelectorService {
    @Override
    public @NotNull String getQueryText(@NotNull PsiFile file) {
        InjectedLanguageManager injector = InjectedLanguageManager.getInstance(file.getProject());
        if (!injector.isInjectedFragment(file)) {
            return Objects.requireNonNullElse(file.getText(), "");
        }
        return decodeShreds(file, null);
    }

    @Override
    public @NotNull String getQueryText(@NotNull DQLQuery query, @NotNull Project project) {
        PsiFile file = query.getContainingFile();
        InjectedLanguageManager injector = InjectedLanguageManager.getInstance(project);
        if (!injector.isInjectedFragment(file)) {
            return Objects.requireNonNullElse(query.getText(), "");
        }
        TextRange hostRange = injector.injectedToHost(file, query.getTextRange());
        return decodeShreds(file, hostRange);
    }

    @Override
    public void getQueryFromEditorContext(@NotNull PsiFile file, @Nullable Editor editor, @NotNull Consumer<@NotNull String> consumer) {
        if (editor == null) {
            consumer.accept(getQueryText(file));
            return;
        }
        int start = editor.getSelectionModel().getSelectionStart();
        int end = editor.getSelectionModel().getSelectionEnd();
        List<SelectionContext> contexts = start != end ? getQueryFromSelection(file, editor, start, end) : getQueryFromSubqueries(file, editor);
        if (contexts.isEmpty()) {
            consumer.accept(getQueryText(file));
        } else {
            createSelectionPopup(editor, contexts, hostRange -> consumer.accept(getQueryText(file, hostRange)));
        }
    }

    private @NotNull String getQueryText(@NotNull PsiFile file, @NotNull TextRange hostRange) {
        InjectedLanguageManager injector = InjectedLanguageManager.getInstance(file.getProject());
        if (!injector.isInjectedFragment(file)) {
            return file.getFileDocument().getText(hostRange);
        }
        return decodeShreds(file, hostRange);
    }

    private @NotNull String decodeShreds(@NotNull PsiFile injectedFile, @Nullable TextRange hostRange) {
        InjectedLanguageManager injector = InjectedLanguageManager.getInstance(injectedFile.getProject());
        PsiLanguageInjectionHost rootHost = injector.getInjectionHost(injectedFile);
        if (rootHost == null) {
            return Objects.requireNonNullElse(injector.getUnescapedText(injectedFile), "");
        }
        StringBuilder out = new StringBuilder();
        injector.enumerate(rootHost, (injectedPsi, places) -> {
            if (!injectedFile.equals(injectedPsi)) {
                return;
            }
            for (PsiLanguageInjectionHost.Shred shred : places) {
                appendDecodedShred(out, shred, hostRange);
            }
        });
        return out.toString();
    }

    private static void appendDecodedShred(@NotNull StringBuilder out, @NotNull PsiLanguageInjectionHost.Shred shred, @Nullable TextRange hostRange) {
        PsiLanguageInjectionHost host = shred.getHost();
        if (host == null) {
            return;
        }
        TextRange rangeInHost = shred.getRangeInsideHost();
        int hostStartInFile = host.getTextRange().getStartOffset();

        TextRange effectiveRangeInHost;
        if (hostRange != null) {
            TextRange shredInFile = rangeInHost.shiftRight(hostStartInFile);
            TextRange clipped = shredInFile.intersection(hostRange);
            if (clipped == null) {
                return;
            }
            effectiveRangeInHost = new TextRange(
                    clipped.getStartOffset() - hostStartInFile,
                    clipped.getEndOffset() - hostStartInFile
            );
        } else {
            effectiveRangeInHost = rangeInHost;
            out.append(shred.getPrefix());
        }

        if (!effectiveRangeInHost.isEmpty()) {
            LiteralTextEscaper<? extends PsiLanguageInjectionHost> escaper = host.createLiteralTextEscaper();
            if (!escaper.decode(effectiveRangeInHost, out)) {
                out.append(effectiveRangeInHost.substring(host.getText()));
            }
        }

        if (hostRange == null) {
            out.append(shred.getSuffix());
        }
    }

    private @NotNull List<SelectionContext> getQueryFromSubqueries(@NotNull PsiFile file, @NotNull Editor editor) {
        List<SelectionContext> queries = new ArrayList<>();
        int offset = editor.getCaretModel().getOffset();
        PsiElement element = file.findElementAt(offset);
        InjectedLanguageManager injector = InjectedLanguageManager.getInstance(file.getProject());
        if (injector.isInjectedFragment(file)
                && file.getViewProvider().getDocument() instanceof DocumentWindow documentWindow
                && !(editor.getDocument() instanceof DocumentWindow)) {
            int injectedOffset = documentWindow.hostToInjected(offset);
            element = file.findElementAt(injectedOffset);
        }

        while (element != null) {
            DQLQuery parent = PsiTreeUtil.getParentOfType(element, DQLQuery.class);
            if (parent != null) {
                queries.add(new SelectionContext(
                        toHostRange(file, parent.getTextRange()),
                        DQLBundle.message("services.executeDQL.selectQuery.subquery", DQLBundle.shorten(parent.getText()))
                ));
            }
            element = parent;
        }
        if (queries.size() < 2) {
            return List.of();
        }
        queries.removeLast();
        queries.add(new SelectionContext(
                toHostRange(file, file.getTextRange()),
                DQLBundle.message("services.executeDQL.selectQuery.wholeFile")
        ));

        return queries;
    }

    private @NotNull List<SelectionContext> getQueryFromSelection(@NotNull PsiFile file, @NotNull Editor editor, int start, int end) {
        TextRange editorRange = new TextRange(start, end);
        TextRange selectionHostRange = editor.getDocument() instanceof DocumentWindow documentWindow
                ? documentWindow.injectedToHost(editorRange)
                : editorRange;
        List<SelectionContext> queries = new ArrayList<>(2);
        queries.add(new SelectionContext(
                selectionHostRange,
                DQLBundle.message("services.executeDQL.selectQuery.selectedText")
        ));
        queries.add(new SelectionContext(
                toHostRange(file, file.getTextRange()),
                DQLBundle.message("services.executeDQL.selectQuery.wholeFile")
        ));
        return queries;
    }

    private static void createSelectionPopup(@NotNull Editor editor, @NotNull List<SelectionContext> queries, @NotNull Consumer<TextRange> selectedCallback) {
        MarkupModel markupModel = editor.getMarkupModel();
        TextAttributes attributes = new TextAttributes();
        attributes.setEffectType(EffectType.BOXED);
        attributes.setEffectColor(JBColor.GREEN);

        RangeHighlighter[] activeHighlighter = new RangeHighlighter[1];
        Runnable clearHighlighter = () -> {
            RangeHighlighter current = activeHighlighter[0];
            if (current != null) {
                if (current.isValid()) {
                    markupModel.removeHighlighter(current);
                }
                activeHighlighter[0] = null;
            }
        };
        Consumer<TextRange> showHighlighter = hostRange -> {
            clearHighlighter.run();
            TextRange editorRange = toEditorRange(editor, hostRange);
            if (editorRange == null) {
                return;
            }
            activeHighlighter[0] = markupModel.addRangeHighlighter(
                    editorRange.getStartOffset(),
                    editorRange.getEndOffset(),
                    HighlighterLayer.SELECTION - 1,
                    attributes,
                    HighlighterTargetArea.EXACT_RANGE
            );
        };

        JBPopupFactory.getInstance()
                .createPopupChooserBuilder(queries)
                .setTitle(DQLBundle.message("services.executeDQL.selectQuery.title"))
                .setItemChosenCallback(e -> selectedCallback.accept(e.range()))
                .setRenderer(new ColoredListCellRenderer<>() {
                    @Override
                    protected void customizeCellRenderer(@NotNull JList<? extends SelectionContext> jList, SelectionContext context, int index, boolean selected, boolean hasFocus) {
                        append(context.fragment());
                    }
                })
                .setItemSelectedCallback(e -> {
                    if (e != null) {
                        showHighlighter.accept(e.range());
                    } else {
                        clearHighlighter.run();
                    }
                })
                .addListener(new JBPopupListener() {
                    @Override
                    public void beforeShown(@NotNull LightweightWindowEvent event) {
                        showHighlighter.accept(queries.getFirst().range());
                    }

                    @Override
                    public void onClosed(@NotNull LightweightWindowEvent event) {
                        clearHighlighter.run();
                    }
                })
                .createPopup()
                .showInBestPositionFor(editor);
    }

    private @NotNull TextRange toHostRange(@NotNull PsiFile file, @NotNull TextRange range) {
        InjectedLanguageManager injector = InjectedLanguageManager.getInstance(file.getProject());
        if (injector.isInjectedFragment(file) && file.getViewProvider().getDocument() instanceof DocumentWindow) {
            return injector.injectedToHost(file, range);
        }
        return range;
    }

    private static @Nullable TextRange toEditorRange(@NotNull Editor editor, @NotNull TextRange hostRange) {
        if (!(editor.getDocument() instanceof DocumentWindow documentWindow)) {
            return hostRange;
        }
        int start = documentWindow.hostToInjected(hostRange.getStartOffset());
        int end = documentWindow.hostToInjected(hostRange.getEndOffset());
        if (start < 0 || end < 0) {
            return null;
        }
        return new TextRange(start, end);
    }

    protected record SelectionContext(@NotNull TextRange range, @NotNull String fragment) {
    }
}
