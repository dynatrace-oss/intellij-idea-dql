package pl.thedeem.intellij.dpl;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.psi.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DPLUtil {
    public static @NotNull List<DPLFieldName> findFields(@NotNull Project project) {
        List<DPLFieldName> result = new ArrayList<>();
        Collection<VirtualFile> virtualFiles = FileTypeIndex.getFiles(DPLFileType.INSTANCE, GlobalSearchScope.allScope(project));
        for (VirtualFile virtualFile : virtualFiles) {
            PsiFile file = PsiManager.getInstance(project).findFile(virtualFile);
            if (file != null) {
                Collection<DPLFieldName> properties = PsiTreeUtil.findChildrenOfType(file, DPLFieldName.class);
                result.addAll(properties);
            }
        }
        return result;
    }

    public static @NotNull List<DPLFieldName> findFields(@NotNull Project project, @NotNull String name) {
        List<DPLFieldName> result = new ArrayList<>();
        Collection<VirtualFile> virtualFiles = FileTypeIndex.getFiles(DPLFileType.INSTANCE, GlobalSearchScope.allScope(project));
        for (VirtualFile virtualFile : virtualFiles) {
            PsiFile file = PsiManager.getInstance(project).findFile(virtualFile);
            if (file != null) {
                result.addAll(findFields(file, name));
            }
        }
        return result;
    }

    public static @NotNull List<DPLFieldName> findFields(@NotNull PsiFile file, @NotNull String name) {
        List<DPLFieldName> result = new ArrayList<>();
        Collection<DPLFieldName> properties = PsiTreeUtil.findChildrenOfType(file, DPLFieldName.class);
        for (DPLFieldName property : properties) {
            if (name.equals(property.getName())) {
                result.add(property);
            }
        }
        return result;
    }

    public static @NotNull List<DPLVariable> findVariables(@NotNull PsiFile file, @NotNull String name) {
        List<DPLVariable> result = new ArrayList<>();
        Collection<DPLVariable> properties = PsiTreeUtil.findChildrenOfType(file, DPLVariable.class);
        for (DPLVariable property : properties) {
            if (name.equals(property.getName())) {
                result.add(property);
            }
        }
        return result;
    }

    public static @NotNull List<DPLVariable> findVariables(@NotNull PsiFile file) {
        Collection<DPLVariable> properties = PsiTreeUtil.findChildrenOfType(file, DPLVariable.class);
        return new ArrayList<>(properties);
    }
    
    public static  @NotNull String getExpressionName(@NotNull DPLExpression expression) {
        return switch (expression) {
            case DPLSequenceGroupExpression ignored -> "sequence";
            case DPLAlternativeGroupExpression ignored -> "alternative_group";
            default -> "unknown";
        };
    }

    public static @NotNull MinMaxValues getMinMaxValues(@NotNull DPLQuantifier quantifier) {
        long min = 0L;
        Long max = null;

        if (quantifier instanceof DPLSimpleQuantifier q) {
            if (DPLTypes.ADD.equals(q.getFirstChild().getNode().getElementType())) {
                min = 1L;
            }
        }
        else if (quantifier instanceof DPLLimitedQuantifier q) {
            DPLLimitedQuantifierRanges ranges = q.getLimitedQuantifierRanges();
            switch (ranges) {
                case DPLMinMaxQuantifier range -> {
                    min = range.getQuantifierLimitList().getFirst().getLongValue();
                    max = range.getQuantifierLimitList().getLast().getLongValue();
                }
                case DPLMinQuantifier range -> {
                    min = range.getQuantifierLimit().getLongValue();
                }
                case DPLMaxQuantifier range -> {
                    max = range.getQuantifierLimit().getLongValue();
                }
                case DPLExactQuantifier range -> {
                    min = max = range.getQuantifierLimit().getLongValue();
                }
                case null, default -> {}
            }
        }
        return new MinMaxValues(min, max);
    }

    public record MinMaxValues(Long min, Long max) {}
}
