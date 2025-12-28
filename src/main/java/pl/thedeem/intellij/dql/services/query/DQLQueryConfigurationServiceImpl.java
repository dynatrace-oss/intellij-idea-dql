package pl.thedeem.intellij.dql.services.query;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.injected.editor.VirtualFileWindow;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.ColoredListCellRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.IntelliJUtils;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.QueryConfiguration;
import pl.thedeem.intellij.dql.executing.runConfiguration.ExecuteDQLRunConfiguration;
import pl.thedeem.intellij.dql.psi.DQLCommand;
import pl.thedeem.intellij.dql.psi.DQLQuery;
import pl.thedeem.intellij.dql.psi.DQLSubqueryExpression;
import pl.thedeem.intellij.dql.services.variables.DQLVariablesService;
import pl.thedeem.intellij.dql.settings.DQLSettings;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class DQLQueryConfigurationServiceImpl implements DQLQueryConfigurationService {
    @Override
    public @NotNull QueryConfiguration getQueryConfiguration(@NotNull PsiFile file) {
        QueryConfiguration configuration = file.getUserData(QUERY_CONFIGURATION);
        if (configuration == null) {
            configuration = createConfigurationFromRunManager(file);
        }
        if (configuration == null) {
            configuration = createDefaultConfiguration(file);
        }
        DQLVariablesService variablesService = DQLVariablesService.getInstance(file.getProject());
        configuration.setDefinedVariables(variablesService.getDefinedVariables(file));
        return configuration;
    }

    @Override
    public void getQueryConfigurationWithEditorContext(@NotNull PsiFile file, @Nullable Editor editor, @NotNull Consumer<QueryConfiguration> consumer) {
        QueryConfiguration configuration = getQueryConfiguration(file);
        if (editor != null) {
            getQueryFromEditorContext(file, editor, (query) -> {
                configuration.setQuery(query);
                consumer.accept(configuration);
            });
        } else {
            consumer.accept(configuration);
        }
    }

    @Override
    public @NotNull QueryConfiguration createDefaultConfiguration(@NotNull PsiFile file) {
        QueryConfiguration result = new QueryConfiguration();
        result.setQuery(file.getText());
        result.setTenant(DQLSettings.getInstance().getDefaultDynatraceTenant());
        result.setOriginalFile(IntelliJUtils.getRelativeProjectPath(file.getVirtualFile(), file.getProject()));
        return result;
    }

    @Override
    public void updateQueryConfiguration(@NotNull QueryConfiguration configuration, @NotNull PsiFile file) {
        file.putUserData(QUERY_CONFIGURATION, configuration);
    }

    private @Nullable QueryConfiguration createConfigurationFromRunManager(@NotNull PsiFile file) {
        if (file instanceof VirtualFileWindow) {
            return null;
        }
        RunManager runManager = RunManager.getInstance(file.getProject());
        String filePath = IntelliJUtils.getRelativeProjectPath(file.getVirtualFile(), file.getProject());
        for (RunnerAndConfigurationSettings settings : runManager.getAllSettings()) {
            if (settings.getConfiguration() instanceof ExecuteDQLRunConfiguration dqlRunConfiguration && Objects.equals(filePath, dqlRunConfiguration.getDQLFile())) {
                return dqlRunConfiguration.getConfiguration();
            }
        }
        return null;
    }

    private void getQueryFromEditorContext(@NotNull PsiFile file, @NotNull Editor editor, @NotNull Consumer<@NotNull String> consumer) {
        int start = editor.getSelectionModel().getSelectionStart();
        int end = editor.getSelectionModel().getSelectionEnd();
        InjectedLanguageManager injector = InjectedLanguageManager.getInstance(file.getProject());
        if (injector.isInjectedFragment(file)) {
            consumer.accept(injector.getUnescapedText(file));
        } else if (start != end) {
            getQueryFromSelection(file, editor, start, end, consumer);
        } else {
            getQueryFromSubqueries(file, editor, consumer);
        }
    }

    private static void getQueryFromSubqueries(@NotNull PsiFile file, @NotNull Editor editor, @NotNull Consumer<@NotNull String> consumer) {
        List<SelectionContext> queries = new ArrayList<>();
        PsiElement element = file.findElementAt(editor.getSelectionModel().getSelectionStart());
        while (element != null) {
            DQLQuery parent = PsiTreeUtil.getParentOfType(element, DQLQuery.class);
            if (parent != null) {
                queries.add(new SelectionContext(parent.getTextRange(), DQLBundle.message("services.executeDQL.selectQuery.subquery", DQLBundle.shorten(parent.getText()))));
            }
            element = parent;
        }
        if (queries.size() < 2) {
            consumer.accept(file.getText());
            return;
        }
        queries.removeLast();
        queries.add(new SelectionContext(file.getTextRange(), DQLBundle.message("services.executeDQL.selectQuery.wholeFile")));

        createSelectionPopup(editor, queries.reversed(), textRange -> consumer.accept(textRange != null ? file.getFileDocument().getText(textRange) : file.getText()));
    }

    private void getQueryFromSelection(@NotNull PsiFile file, @NotNull Editor editor, int start, int end, @NotNull Consumer<@NotNull String> consumer) {
        PsiElement startingCommand = findMatchingElement(file.findElementAt(start));
        PsiElement endCommand = findMatchingElement(file.findElementAt(end));
        List<SelectionContext> queries = new ArrayList<>(2);
        queries.add(new SelectionContext(file.getTextRange(), DQLBundle.message("services.executeDQL.selectQuery.wholeFile")));
        queries.add(new SelectionContext(new TextRange(
                startingCommand != null ? startingCommand.getTextRange().getStartOffset() : start,
                endCommand != null ? endCommand.getTextRange().getEndOffset() : end
        ), DQLBundle.message("services.executeDQL.selectQuery.selectedText")));
        createSelectionPopup(editor, queries, textRange -> consumer.accept(textRange != null ? file.getFileDocument().getText(textRange) : file.getText()));
    }

    private @Nullable PsiElement findMatchingElement(@Nullable PsiElement psiElement) {
        PsiElement element = PsiTreeUtil.getParentOfType(psiElement, DQLCommand.class, DQLQuery.class, DQLSubqueryExpression.class);
        if (element instanceof DQLSubqueryExpression subqueryExpression) {
            return subqueryExpression.getQuery();
        }
        return psiElement;
    }

    private static void createSelectionPopup(@NotNull Editor editor, @NotNull List<SelectionContext> queries, @NotNull Consumer<TextRange> selectedCallback) {
        MarkupModel markupModel = editor.getMarkupModel();
        TextAttributes attributes = new TextAttributes();
        attributes.setBackgroundColor(editor.getColorsScheme().getColor(EditorColors.SELECTION_BACKGROUND_COLOR));
        JBPopupFactory.getInstance()
                .createPopupChooserBuilder(queries)
                .setTitle(DQLBundle.message("services.executeDQL.selectQuery.title"))
                .setItemChosenCallback(e -> selectedCallback.accept(e == queries.getFirst() ? null : e.range()))
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
                        SelectionContext context = queries.getLast();
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

    private record SelectionContext(@NotNull TextRange range, @NotNull String fragment) {
    }
}
