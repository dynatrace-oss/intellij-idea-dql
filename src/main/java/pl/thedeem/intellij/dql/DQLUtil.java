package pl.thedeem.intellij.dql;

import com.intellij.codeInsight.completion.CompletionUtilCore;
import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.internal.StringUtil;
import pl.thedeem.intellij.dql.definition.model.DQLDurationType;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.psi.DQLFieldExpression;
import pl.thedeem.intellij.dql.psi.DQLParenthesisedExpression;
import pl.thedeem.intellij.dql.psi.DQLVariableExpression;
import pl.thedeem.intellij.dql.settings.DQLSettings;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DQLUtil {
    public final static DateTimeFormatter DQL_FLEXIBLE_DATE_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd['T'HH:mm:ss]")
            .appendFraction(ChronoField.MILLI_OF_SECOND, 0, 9, true)
            .optionalStart()
            .appendPattern("X")
            .optionalEnd()
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
            .parseDefaulting(ChronoField.MILLI_OF_SECOND, 0)
            .toFormatter()
            .withZone(ZoneOffset.UTC);
    public final static DateTimeFormatter DQL_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
    public final static DateTimeFormatter USER_FRIENDLY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSXXX");
    public final static Pattern DURATION_PATTERN = Pattern.compile(
            "^\\s*(-?)\\s*(\\d+)\\s*("
                    + String.join("|", DQLDurationType.getTypes()) +
                    ")$");

    private final static String PARTIAL_DQL_NAME = ".partial.";
    private final static String CREDENTIALS_PREFIX = "pl.thedeem.intellij.dql/";

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

    public static @Nullable PsiElement unpackParenthesis(@Nullable PsiElement element) {
        if (element == null) {
            return null;
        }
        PsiElement result = element;
        while (result instanceof DQLParenthesisedExpression parenthesisedExpression) {
            result = parenthesisedExpression.getExpression();
        }
        return result;
    }

    public static String getCurrentTimeTimestamp() {
        ZonedDateTime moment = Instant.now()
                .atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneOffset.UTC);
        return DQL_DATE_FORMATTER.format(moment);
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
                .map(p -> Objects.requireNonNullElse(p.name(), "").toLowerCase())
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

    public static @Nullable String parseUserTime(@NotNull String text) throws IllegalArgumentException {
        if (StringUtil.isBlank(text)) {
            return null;
        }
        Matcher durationMatcher = DURATION_PATTERN.matcher(text);
        if (durationMatcher.matches()) {
            int negative = durationMatcher.group(1).trim().isBlank() ? 0 : -1;
            int amount = Integer.parseInt(durationMatcher.group(2)) * negative;
            String unit = durationMatcher.group(3);
            DQLDurationType type = DQLDurationType.getByType(unit);
            if (type == null) {
                throw new IllegalArgumentException("Invalid duration type: " + unit);
            }
            Instant instant = type.getInstant(amount, Instant.now());
            return DQL_DATE_FORMATTER.format(instant.atZone(ZoneId.systemDefault()));
        }

        try {
            TemporalAccessor parsed = DQL_FLEXIBLE_DATE_FORMATTER.parse(text);
            return DQL_DATE_FORMATTER.format(ZonedDateTime.from(parsed));
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date format", ex);
        }
    }
}
