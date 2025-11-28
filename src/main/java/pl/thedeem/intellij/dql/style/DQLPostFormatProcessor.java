package pl.thedeem.intellij.dql.style;

import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DQLPostFormatProcessor implements PostFormatProcessor {

    @Override
    public @NotNull PsiElement processElement(@NotNull PsiElement element,
                                              @NotNull CodeStyleSettings settings) {
        return element;
    }

    @Override
    public @NotNull TextRange processText(@NotNull PsiFile hostFile,
                                          @NotNull TextRange range,
                                          @NotNull CodeStyleSettings settings) {
        Project project = hostFile.getProject();
        InjectedLanguageManager injector = InjectedLanguageManager.getInstance(project);
        CodeStyleManager styleManager = CodeStyleManager.getInstance(project);

        Collection<PsiFile> injectedFiles = findInjectionHosts(hostFile, injector, range);
        final PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
        if (!injectedFiles.isEmpty()) {
            for (PsiFile host : injectedFiles) {
                PsiFile updated = PsiFileFactory.getInstance(project).createFileFromText(host.getLanguage(), host.getText());
                styleManager.reformat(updated);
                String text = updated.getText();

                Document d = documentManager.getDocument(host);
                if (d != null) {
                    d.setText(text);
                }
            }
        }
        return range;
    }

    private @NotNull Collection<PsiFile> findInjectionHosts(@NotNull PsiFile file, @NotNull InjectedLanguageManager injector, @NotNull TextRange range) {
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
                        result.add(iFile.getFirst().getContainingFile());
                    }
                }
            }
        }

        return result;
    }

    @Override
    public boolean isWhitespaceOnly() {
        return false;
    }
}
