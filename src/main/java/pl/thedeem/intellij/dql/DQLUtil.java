package pl.thedeem.intellij.dql;

import com.intellij.codeInsight.completion.CompletionUtilCore;
import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.psi.DQLFieldExpression;
import pl.thedeem.intellij.dql.psi.DQLParenthesisedExpression;
import pl.thedeem.intellij.dql.psi.DQLVariableExpression;
import pl.thedeem.intellij.dql.settings.DQLSettings;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class DQLUtil {
    public final static DateTimeFormatter DQL_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
    public final static DateTimeFormatter USER_FRIENDLY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final static String PARTIAL_DQL_NAME = ".partial.";
    private final static String CREDENTIALS_PREFIX = "pl.thedeem.intellij.dql/";

    public static @Nullable PsiFile openFile(@NotNull Project project, @NotNull String path) {
        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(path);
        if (file != null) {
            return PsiManager.getInstance(project).findFile(file);
        }
        return null;
    }

    public static boolean isPartialFile(final PsiFile file) {
        String name = file.getName();
        return name.contains(PARTIAL_DQL_NAME);
    }

    public static List<DQLFieldExpression> findFieldsInFile(PsiFile file, String key) {
        List<DQLFieldExpression> result = new ArrayList<>();
        if (file != null) {
            Collection<DQLFieldExpression> fields = PsiTreeUtil.findChildrenOfType(file, DQLFieldExpression.class);
            if (!fields.isEmpty()) {
                for (DQLFieldExpression field : fields) {
                    if (key.equals(field.getName())) {
                        result.add(field);
                    }
                }
            }
        }
        return result;
    }

    public static List<DQLFieldExpression> findFieldsInFile(PsiFile file) {
        List<DQLFieldExpression> result = new ArrayList<>();
        if (file != null) {
            Collection<DQLFieldExpression> fields = PsiTreeUtil.findChildrenOfType(file, DQLFieldExpression.class);
            if (!fields.isEmpty()) {
                Collections.addAll(result, fields.toArray(new DQLFieldExpression[0]));
            }
        }
        return result;
    }

    public static List<DQLFieldExpression> findFieldsInProject(@NotNull Project project) {
        List<DQLFieldExpression> result = new ArrayList<>();
        Collection<VirtualFile> virtualFiles = FileTypeIndex.getFiles(DQLFileType.INSTANCE, GlobalSearchScope.allScope(project));
        for (VirtualFile virtualFile : virtualFiles) {
            PsiFile file = PsiManager.getInstance(project).findFile(virtualFile);
            if (file != null) {
                Collection<DQLFieldExpression> properties = PsiTreeUtil.findChildrenOfType(file, DQLFieldExpression.class);
                result.addAll(properties);
            }
        }
        return result;
    }

    public static List<DQLFieldExpression> findFieldsInProject(@NotNull Project project, @NotNull String name) {
        List<DQLFieldExpression> result = new ArrayList<>();
        Collection<VirtualFile> virtualFiles = FileTypeIndex.getFiles(DQLFileType.INSTANCE, GlobalSearchScope.allScope(project));
        for (VirtualFile virtualFile : virtualFiles) {
            PsiFile file = PsiManager.getInstance(project).findFile(virtualFile);
            if (file != null) {
                Collection<DQLFieldExpression> properties = PsiTreeUtil.findChildrenOfType(file, DQLFieldExpression.class);
                for (DQLFieldExpression property : properties) {
                    if (name.equals(property.getName())) {
                        result.add(property);
                    }
                }
            }
        }
        return result;
    }

    public static List<DQLVariableExpression> findVariablesInFile(PsiFile file) {
        List<DQLVariableExpression> result = new ArrayList<>();
        if (file != null) {
            Collection<DQLVariableExpression> fields = PsiTreeUtil.findChildrenOfType(file, DQLVariableExpression.class);
            if (!fields.isEmpty()) {
                Collections.addAll(result, fields.toArray(new DQLVariableExpression[0]));
            }
        }
        return result;
    }

    public static PsiElement unpackParenthesis(@NotNull PsiElement element) {
        PsiElement result = element;
        while (result instanceof DQLParenthesisedExpression parenthesisedExpression) {
            result = parenthesisedExpression.getExpression();
        }
        return result;
    }

    public static String getCurrentTimeTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        return DQL_DATE_FORMATTER.format(now);
    }

    public static ZonedDateTime getDateFromTimestamp(@NotNull String timestamp) {
        Instant instant = Instant.parse(timestamp);
        return instant.atZone(ZoneId.systemDefault());
    }

    public static CredentialAttributes createCredentialAttributes(@NotNull String credentialId) {
        return new CredentialAttributes(CREDENTIALS_PREFIX + credentialId);
    }

    public static @NotNull Collection<Parameter> getMissingParameters(@NotNull List<MappedParameter> definedParameters, @NotNull Collection<Parameter> parameters) {
        Set<String> names = definedParameters.stream()
                .filter(p -> p.name() != null && !CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED.equals(p.holder().getText()))
                .map(p -> Objects.requireNonNull(p.name()).toLowerCase())
                .collect(Collectors.toSet());

        return parameters.stream()
                .filter(p -> {
                    if (names.contains(p.name().toLowerCase())) {
                        return false;
                    }
                    if (p.excludes() != null) {
                        for (String exclude : p.excludes()) {
                            if (names.contains(exclude.toLowerCase())) {
                                return false;
                            }
                        }
                    }
                    if (p.aliases() != null) {
                        for (String exclude : p.aliases()) {
                            if (names.contains(exclude.toLowerCase())) {
                                return false;
                            }
                        }
                    }
                    return !p.experimental() || DQLSettings.getInstance().isAllowingExperimentalFeatures();
                })
                .collect(Collectors.toList());
    }
}
