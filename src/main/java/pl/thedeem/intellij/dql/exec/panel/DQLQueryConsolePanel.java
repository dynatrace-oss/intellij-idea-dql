package pl.thedeem.intellij.dql.exec.panel;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.SoftWrapsEditorCustomization;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.IntelliJUtils;
import pl.thedeem.intellij.dql.DynatraceQueryLanguage;
import pl.thedeem.intellij.dql.actions.ExecuteDQLQueryAction;
import pl.thedeem.intellij.dql.definition.model.QueryConfiguration;
import pl.thedeem.intellij.dql.editor.DQLToolbarProvider;
import pl.thedeem.intellij.dql.editor.actions.QueryConfigurationAction;
import pl.thedeem.intellij.dql.services.query.DQLQueryConfigurationService;

import java.util.List;
import java.util.Objects;

public class DQLQueryConsolePanel extends BorderLayoutPanel implements UiDataProvider {
    public static final Key<DQLQueryConsolePanel> CONSOLE_PANEL = Key.create("DQL_QUERY_CONSOLE_PANEL");
    private final QueryConfiguration configuration;
    private final EditorTextField editorField;
    private final Project project;
    private final VirtualFile virtualFile;

    public DQLQueryConsolePanel(
            @NotNull Project project,
            @NotNull String content,
            @NotNull VirtualFile virtualFile,
            @Nullable QueryConfiguration initialConfiguration
    ) {
        super();
        this.virtualFile = virtualFile;
        setBorder(JBUI.Borders.empty());

        virtualFile.putUserData(DQLToolbarProvider.TOOLBAR_HIDDEN, true);
        this.project = project;
        editorField = IntelliJUtils.createEditorPanel(project, DynatraceQueryLanguage.INSTANCE, false, List.of(
                new IntelliJUtils.StandardEditorCustomization(),
                new IntelliJUtils.EmptyBorderEditorCustomization(),
                SoftWrapsEditorCustomization.ENABLED,
                editorEx -> {
                    editorEx.getSettings().setLineMarkerAreaShown(true);
                    editorEx.getSettings().setShowIntentionBulb(true);
                }
        ));
        editorField.setText(content);
        editorField.putClientProperty(CONSOLE_PANEL, this);

        DefaultActionGroup group = new DefaultActionGroup();
        group.setInjectedContext(true);
        group.add(new QueryConfigurationAction() {
            @Override
            protected void saveCurrentQueryConfiguration(@NotNull AnActionEvent e) {
                // do nothing - console is in-memory only, no save needed
            }
        });

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("DQL.QueryConsoleToolbar", group, true);
        toolbar.setTargetComponent(this);
        toolbar.getComponent().setOpaque(false);
        toolbar.getComponent().setBorder(JBUI.Borders.empty());
        addToTop(toolbar.getComponent());
        addToCenter(editorField);

        this.configuration = Objects.requireNonNullElse(initialConfiguration, getQueryConfiguration(project, content, virtualFile));
        DQLQueryConfigurationService.getInstance().updateConfiguration(virtualFile, this.configuration);
    }

    @Override
    public void uiDataSnapshot(@NotNull DataSink dataSink) {
        dataSink.lazy(CommonDataKeys.EDITOR, editorField::getEditor);
        dataSink.lazy(CommonDataKeys.PSI_FILE, () -> psiFileFromEditorField(project, editorField));
        dataSink.set(DQLQueryConfigurationService.DATA_QUERY_CONFIGURATION, configuration);
        dataSink.set(ExecuteDQLQueryAction.PREFERRED_EXECUTION_NAME, virtualFile.getName());
    }

    protected @Nullable PsiFile psiFileFromEditorField(@NotNull Project project, @NotNull EditorTextField editorField) {
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        return psiDocumentManager.getPsiFile(editorField.getDocument());
    }

    protected @NotNull QueryConfiguration getQueryConfiguration(@NotNull Project project, @NotNull String content, @NotNull VirtualFile virtualFile) {
        QueryConfiguration result = DQLQueryConfigurationService.getInstance().createDefaultConfiguration(project, virtualFile);
        result.setQuery(content);
        return result;
    }
}
