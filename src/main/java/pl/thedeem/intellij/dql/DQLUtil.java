package pl.thedeem.intellij.dql;

import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.json.JsonFileType;
import com.intellij.json.psi.JsonFile;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonValue;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.psi.*;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DQLUtil {
    private final static String DQL_VARIABLES_FILE = "dql-variables.json";
    private final static String PARTIAL_DQL_SUFFIX = ".partial.dql";
    private final static String CREDENTIALS_SUFFIX = "pl.thedeem.intellij.dql/";
    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    /**
     * The variable file must be defined either in the same directory or in one of the current file parents
     */
    public static List<PsiElement> findVariablesDefinitions(@NotNull Project project, @NotNull String variableName, @NotNull PsiFile file) {
        Collection<VirtualFile> virtualFiles = FileTypeIndex.getFiles(JsonFileType.INSTANCE, GlobalSearchScope.allScope(project));
        List<PsiElement> result = new ArrayList<>();
        Path currentPath = Path.of(file.getVirtualFile().getPath()).normalize();
        for (VirtualFile virtualFile : virtualFiles) {
            if (DQL_VARIABLES_FILE.equals(virtualFile.getName())) {
              Path variablePath = Path.of(virtualFile.getPath()).normalize();
              if (!currentPath.startsWith(variablePath.getParent())) {
                continue;
              }
              JsonFile jsonFile = (JsonFile) PsiManager.getInstance(project).findFile(virtualFile);
              if (jsonFile != null) {
                  JsonValue topLevelValue = jsonFile.getTopLevelValue();
                  if (topLevelValue != null) {
                      for (PsiElement child : topLevelValue.getChildren()) {
                        if (child instanceof JsonProperty property) {
                            if (variableName.equals(property.getName())) {
                                result.add(property);
                            }
                        }
                      }
                  }
              }
            }
        }
        return result;
    }

    public static Path getDefaultVariablesFile(PsiElement element) {
        VirtualFile virtualFile = element.getContainingFile().getVirtualFile();
        if (virtualFile == null) {
            return null;
        }
        String directory = Path.of(virtualFile.getPath()).getParent().toString();
        return Path.of(directory + "/" + DQL_VARIABLES_FILE).normalize();
    }

    public static @Nullable PsiFile openFile(@NotNull Project project, @NotNull String path) {
        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(path);
        if (file != null) {
            return PsiManager.getInstance(project).findFile(file);
        }
        return null;
    }

    public static boolean isPartialFile(final PsiFile file) {
        String name = file.getName();
        return name.endsWith(PARTIAL_DQL_SUFFIX);
    }

    public static List<DQLFieldExpression> findFieldsInFile(PsiFile dqlFile, String key) {
        List<DQLFieldExpression> result = new ArrayList<>();
        if (dqlFile != null) {
            Collection<DQLFieldExpression> fields = PsiTreeUtil.findChildrenOfType(dqlFile, DQLFieldExpression.class);
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

    public static List<DQLFieldExpression> findFieldsInFile(PsiFile dqlFile) {
        List<DQLFieldExpression> result = new ArrayList<>();
        if (dqlFile != null) {
            Collection<DQLFieldExpression> fields = PsiTreeUtil.findChildrenOfType(dqlFile, DQLFieldExpression.class);
            if (!fields.isEmpty()) {
                Collections.addAll(result, fields.toArray(new DQLFieldExpression[0]));
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

    public static Character getLastNonEmptyCharacterFromDocument(InsertionContext context) {
        int i = context.getStartOffset();
        String result;
        do {
            result = getStringFromDocument(context, i - 1, i);
            i--;
        } while (result.isBlank() && !result.isEmpty());
        return !result.isEmpty() ? result.charAt(0) : null;
    }

    public static String getStringFromDocument(InsertionContext context, int startOffset, int endOffset) {
        return context.getDocument().getText(new TextRange(Math.max(0, startOffset), Math.max(0, endOffset)));
    }

    public static PsiElement getPreviousElement(PsiElement element) {
        PsiElement prev = element.getPrevSibling();
        while (prev != null && PlatformPatterns.psiElement().whitespaceCommentEmptyOrError().accepts(prev)) {
            prev = prev.getPrevSibling();
        }
        return prev;
    }

    public static PsiElement getNextElement(PsiElement element) {
        PsiElement next = element.getNextSibling();
        while (next != null && PlatformPatterns.psiElement().whitespaceCommentEmptyOrError().accepts(next)) {
            next = next.getNextSibling();
        }
        return next;
    }

    public static PsiElement unpackParenthesis(PsiElement element) {
        PsiElement result = element;
        while (result instanceof DQLParenthesisedExpression parenthesisedExpression) {
            result = parenthesisedExpression.getExpression();
        }
        return result;
    }

    public static List<PsiElement> getElementsUntilParent(PsiElement element, Class<?>... types) {
        List<PsiElement> elements = new ArrayList<>();
        elements.add(element);
        PsiElement result = element.getParent();
        while (result != null) {
            elements.add(result);
            PsiElement finalResult = result;

            if (Arrays.stream(types).anyMatch(t -> t.isInstance(finalResult))) {
                break;
            }

            result = result.getParent();
        }
        return elements.reversed();
    }

    public static String getCurrentTimeTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        return formatter.format(now);
    }

    public static boolean containsAny(Collection<?> collection, Collection<?> other) {
        for (Object o : collection) {
            if (other.contains(o)) {
                return true;
            }
        }

        return false;
    }

    public static Set<DQLDataType> calculateFieldType(PsiElement[] children) {
        Set<DQLDataType> result = new HashSet<>();
        result.add(DQLDataType.LIST_OF_EXPRESSIONS);
        result.add(DQLDataType.ARRAY);

        boolean assign = false;
        boolean sort = false;
        boolean read = false;

        for (PsiElement child : children) {
            switch (child) {
                case DQLAssignExpression ignored -> assign = true;
                case DQLSortExpression ignored -> sort = true;
                case null, default -> read = true;
            }
        }

        if (assign) {
            if (!read && !sort) {
                result.add(DQLDataType.WRITE_ONLY_EXPRESSION);
            }
        }
        if (read && !assign && !sort) {
            result.add(DQLDataType.READ_ONLY_EXPRESSION);
        }
        if (!assign) {
            result.add(DQLDataType.SORTING_EXPRESSION);
        }
        return Collections.unmodifiableSet(result);
    }

    public static CredentialAttributes createCredentialAttributes(String credentialId) {
        return new CredentialAttributes(CREDENTIALS_SUFFIX + credentialId);
    }
}
