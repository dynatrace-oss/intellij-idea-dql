package pl.thedeem.intellij.dql.inspections.fixes;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.EmptyNode;
import com.intellij.codeInsight.template.impl.TextExpression;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.json.psi.JsonFile;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.psi.DQLVariableExpression;
import pl.thedeem.intellij.dql.variables.DQLVariablesService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class AddMissingVariableDefinitionQuickFix implements LocalQuickFix {
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) throws IncorrectOperationException {
        PsiElement element = descriptor.getPsiElement();

        if (!(element instanceof DQLVariableExpression variable)) {
            return;
        }
        String name = variable.getName();
        Path filePath = DQLVariablesService.getInstance(project).getDefaultVariablesFile(variable);
        
        if (name == null || filePath == null) {
            return;
        }
        VirtualFile virtualFile = ensureFileExists(project, filePath);
        if (virtualFile == null) {
            return;
        }

        PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
        if (psiFile == null) {
            return;
        }

        WriteCommandAction.runWriteCommandAction(project, () -> {
            JsonObject rootObject = ((JsonFile) psiFile).getTopLevelValue() instanceof JsonObject obj ? obj : null;
            if (rootObject == null) {
                return;
            }

            List<JsonProperty> propertyList = rootObject.getPropertyList();

            Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
            if (document != null) {
                PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(document);
                PsiDocumentManager.getInstance(project).commitDocument(document);
                Editor editor = FileEditorManager.getInstance(project).openTextEditor(new OpenFileDescriptor(project, virtualFile, rootObject.getLastChild().getTextOffset()), true);
                if (editor != null) {
                    TemplateManager templateManager = TemplateManager.getInstance(project);
                    Template template = templateManager.createTemplate("", "");
                    if (!propertyList.isEmpty()) {
                        template.addTextSegment(", ");
                    }
                    template.addTextSegment("\"" + name + "\":");
                    template.addVariable("VALUE", new TextExpression("null"), new EmptyNode(), true);
                    templateManager.startTemplate(editor, template);
                    template.setToReformat(true);
                    AutoPopupController.getInstance(project).autoPopupParameterInfo(editor, psiFile);
                }
            }
        });
    }

    private @Nullable VirtualFile ensureFileExists(Project project, Path filePath) {
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(filePath.toString());
        if (virtualFile == null) {
            try {
                Files.createDirectories(filePath.getParent());
                virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(Files.createFile(filePath));

                VirtualFile finalVirtualFile = virtualFile;
                if (finalVirtualFile != null) {
                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        try {
                            finalVirtualFile.setBinaryContent("{}".getBytes());
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to write initial JSON to file: " + e.getMessage(), e);
                        }
                    });
                }
            } catch (IOException ex) {
                Messages.showErrorDialog(
                        DQLBundle.message("components.actions.saveAsFile.error.description", ex.getMessage()),
                        DQLBundle.message("components.actions.saveAsFile.error.title")
                );
            }
        }
        return virtualFile;
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return DQLBundle.message("inspection.fix.genericFamilyName");
    }

    @NotNull
    @Override
    public String getName() {
        return DQLBundle.message("inspection.fix.addMissingVariable");
    }
}
