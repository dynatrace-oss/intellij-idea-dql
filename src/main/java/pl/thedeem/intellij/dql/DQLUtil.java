package pl.thedeem.intellij.dql;

import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.ElementPattern;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DQLUtil {
   public final static DateTimeFormatter DQL_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
   public final static DateTimeFormatter USER_FRIENDLY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

   private final static String PARTIAL_DQL_SUFFIX = ".partial.dql";
   private final static String CREDENTIALS_SUFFIX = "pl.thedeem.intellij.dql/";

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

   public static PsiElement getDeepNeighbourElement(PsiElement element, ElementPattern<PsiElement> pattern) {
      PsiElement neighbour = DQLUtil.getPreviousElement(element);
      while (neighbour != null && !pattern.accepts(neighbour)) {
         neighbour = neighbour.getLastChild();
      }
      return neighbour;
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
      return DQL_DATE_FORMATTER.format(now);
   }

   public static ZonedDateTime getDateFromTimestamp(String timestamp) {
      Instant instant = Instant.parse(timestamp);
      return instant.atZone(ZoneId.systemDefault());
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
