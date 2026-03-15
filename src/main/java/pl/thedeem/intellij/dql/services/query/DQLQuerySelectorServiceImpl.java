package pl.thedeem.intellij.dql.services.query;

import com.intellij.injected.editor.DocumentWindow;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
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
        String content = injector.isInjectedFragment(file) ? injector.getUnescapedText(file) : file.getText();
        return Objects.requireNonNullElse(content, "");
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
            createSelectionPopup(editor, contexts, range -> consumer.accept(getQueryText(file, editor, range)));
        }
    }

    private @NotNull String getQueryText(@NotNull PsiFile file, @NotNull Editor editor, @NotNull TextRange range) {
        InjectedLanguageManager injector = InjectedLanguageManager.getInstance(file.getProject());
        if (!injector.isInjectedFragment(file)) {
            return file.getFileDocument().getText(range);
        }

        int injectedStart = range.getStartOffset();
        int injectedEnd = range.getEndOffset();
        if (file.getViewProvider().getDocument() instanceof DocumentWindow window && !(editor.getDocument() instanceof DocumentWindow)) {
            injectedStart = window.hostToInjected(range.getStartOffset());
            injectedEnd = window.hostToInjected(range.getEndOffset());
        }
        int unescapedStart = injector.mapInjectedOffsetToUnescaped(file, injectedStart);
        int unescapedEnd = injector.mapInjectedOffsetToUnescaped(file, injectedEnd);

        String fullUnescaped = injector.getUnescapedText(file);
        return fullUnescaped.substring(unescapedStart, unescapedEnd);
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
                        getMappedTextRange(file, parent.getTextRange(), editor),
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
                getMappedTextRange(file, file.getTextRange(), editor),
                DQLBundle.message("services.executeDQL.selectQuery.wholeFile")
        ));

        return queries;
    }

    private @NotNull List<SelectionContext> getQueryFromSelection(@NotNull PsiFile file, @NotNull Editor editor, int start, int end) {
        List<SelectionContext> queries = new ArrayList<>(2);
        queries.add(new SelectionContext(
                new TextRange(start, end),
                DQLBundle.message("services.executeDQL.selectQuery.selectedText")
        ));
        queries.add(new SelectionContext(
                getMappedTextRange(file, file.getTextRange(), editor),
                DQLBundle.message("services.executeDQL.selectQuery.wholeFile")
        ));
        return queries;
    }

    private static void createSelectionPopup(@NotNull Editor editor, @NotNull List<SelectionContext> queries, @NotNull Consumer<TextRange> selectedCallback) {
        MarkupModel markupModel = editor.getMarkupModel();
        TextAttributes attributes = new TextAttributes();
        attributes.setEffectType(EffectType.BOXED);
        attributes.setEffectColor(JBColor.GREEN);

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
                        markupModel.removeAllHighlighters();
                        markupModel.addRangeHighlighter(
                                e.range().getStartOffset(),
                                e.range().getEndOffset(),
                                HighlighterLayer.SELECTION - 1,
                                attributes,
                                HighlighterTargetArea.EXACT_RANGE
                        );
                    }
                })
                .addListener(new JBPopupListener() {
                    @Override
                    public void beforeShown(@NotNull LightweightWindowEvent event) {
                        SelectionContext context = queries.getFirst();
                        markupModel.addRangeHighlighter(
                                context.range().getStartOffset(),
                                context.range().getEndOffset(),
                                HighlighterLayer.SELECTION - 1,
                                attributes,
                                HighlighterTargetArea.EXACT_RANGE
                        );
                    }

                    @Override
                    public void onClosed(@NotNull LightweightWindowEvent event) {
                        markupModel.removeAllHighlighters();
                    }
                })
                .createPopup()
                .showInBestPositionFor(editor);
    }

    private @NotNull TextRange getMappedTextRange(@NotNull PsiFile file, @NotNull TextRange range, @NotNull Editor editor) {
        InjectedLanguageManager injector = InjectedLanguageManager.getInstance(file.getProject());
        if (injector.isInjectedFragment(file) && file.getViewProvider().getDocument() instanceof DocumentWindow
                && !(editor.getDocument() instanceof DocumentWindow)) {
            return injector.injectedToHost(file, range);
        }
        return range;
    }

    protected record SelectionContext(@NotNull TextRange range, @NotNull String fragment) {
    }
}
