package pl.thedeem.intellij.dql.services.variables;

import com.intellij.json.JsonFileType;
import com.intellij.json.psi.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.util.SimpleModificationTracker;
import com.intellij.openapi.util.UserDataHolderEx;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.concurrency.annotations.RequiresReadLock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.IntelliJUtils;
import pl.thedeem.intellij.dql.DQLFileType;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.psi.DQLQuery;
import pl.thedeem.intellij.dql.psi.DQLVariableExpression;
import pl.thedeem.intellij.dql.psi.elements.VariableElement;

import java.nio.file.Path;
import java.util.*;

public final class DQLVariablesServiceImpl implements DQLVariablesService {
    public static final String NULL_VARIABLE_PLACEHOLDER = "null";

    private static final Key<SimpleModificationTracker> RUNTIME_VALUES_TRACKER_KEY = Key.create("DQL_RUNTIME_VARIABLE_VALUES_TRACKER");
    private static final Logger logger = Logger.getInstance(DQLVariablesServiceImpl.class);

    private final Project project;

    public DQLVariablesServiceImpl(Project project) {
        this.project = project;
    }

    @Override
    public @Nullable Path getDefaultVariablesFile(@NotNull PsiElement element) {
        VirtualFile virtualFile = element.getContainingFile().getVirtualFile();
        if (virtualFile == null) {
            return null;
        }
        String directory = Path.of(virtualFile.getPath()).getParent().toString();
        return Path.of(directory + "/" + DQL_VARIABLES_FILE).normalize();
    }

    @Override
    public @NotNull List<PsiElement> findVariableDefinitionFiles(@NotNull String variableName, @NotNull PsiFile file) {
        Collection<VirtualFile> virtualFiles = FileTypeIndex.getFiles(JsonFileType.INSTANCE, GlobalSearchScope.allScope(project));
        List<PsiElement> result = new ArrayList<>();
        if (file.getVirtualFile() == null) {
            return result;
        }
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

    @Override
    public @NotNull List<DQLVariableExpression> findVariableUsages(@NotNull JsonProperty definition) {
        PsiFile definitionFile = definition.getContainingFile();
        if (definitionFile == null || definitionFile.getVirtualFile() == null || !DQL_VARIABLES_FILE.equals(definitionFile.getVirtualFile().getName())) {
            return List.of();
        }
        String variableName = definition.getName();
        Path definitionPath = Path.of(definitionFile.getVirtualFile().getPath()).normalize();
        Path definitionDirectory = definitionPath.getParent();
        if (definitionDirectory == null) {
            return List.of();
        }

        List<DQLVariableExpression> result = new ArrayList<>();
        Collection<VirtualFile> dqlFiles = FileTypeIndex.getFiles(DQLFileType.INSTANCE, GlobalSearchScope.allScope(project));
        PsiManager psiManager = PsiManager.getInstance(project);
        for (VirtualFile dqlVirtualFile : dqlFiles) {
            Path dqlPath = Path.of(dqlVirtualFile.getPath()).normalize();
            if (!dqlPath.startsWith(definitionDirectory)) {
                continue;
            }
            PsiFile psiFile = psiManager.findFile(dqlVirtualFile);
            if (psiFile == null) {
                continue;
            }
            for (DQLVariableExpression variable : DQLUtil.findVariablesInFile(psiFile)) {
                if (!variableName.equals(variable.getName())) {
                    continue;
                }
                List<PsiElement> definitions = findVariableDefinitionFiles(variableName, psiFile);
                if (definitions.isEmpty()) {
                    continue;
                }
                PsiElement closest = findClosestDefinition(dqlVirtualFile.getPath(), definitions);
                PsiFile closestFile = closest.getContainingFile();
                if (closestFile != null && closestFile.getVirtualFile() != null && definitionPath.equals(Path.of(closestFile.getVirtualFile().getPath()).normalize())) {
                    result.add(variable);
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public @Nullable VariableDefinition loadVariable(@NotNull PsiFile file, @NotNull String variableName) {
        List<PsiElement> definitions = findVariableDefinitionFiles(variableName, file);
        if (!definitions.isEmpty()) {
            PsiElement definition = findClosestDefinition(file.getVirtualFile().getPath(), definitions);
            if (definition instanceof JsonProperty property) {
                return new VariableDefinition(property.getName(), getVariableValue(property.getValue()), getDataType(property.getValue()));
            }
        }
        for (VariableDefinition def : getUserDefinedVariables(file)) {
            if (variableName.equals(def.name())) {
                return def;
            }
        }
        return null;
    }

    @Override
    @RequiresReadLock
    public @NotNull Set<String> getUndefinedVariables(@NotNull PsiFile file) {
        Set<String> result = new LinkedHashSet<>();
        for (DQLVariableExpression variable : PsiTreeUtil.findChildrenOfType(file, DQLVariableExpression.class)) {
            if (variable.getValue() == null) {
                result.add(variable.getName());
            }
        }
        return result;
    }

    @Override
    @RequiresReadLock
    public @NotNull List<VariableDefinition> getDefinedVariables(@NotNull PsiFile file) {
        DQLQuery query = PsiTreeUtil.getChildOfType(file, DQLQuery.class);
        if (query == null) {
            return List.of();
        }
        List<VariableDefinition> result = new ArrayList<>();
        Set<String> names = new HashSet<>();
        for (Map.Entry<String, List<VariableElement>> variable : query.getDefinedVariables().entrySet()) {
            String name = variable.getKey();
            if (!variable.getValue().isEmpty()) {
                VariableElement element = variable.getValue().getFirst();
                String value = element.getValue();
                if (value != null) {
                    result.add(new VariableDefinition(name, value, element.getDataType()));
                    names.add(name);
                }
            }
        }
        for (VariableDefinition definition : getUserDefinedVariables(file)) {
            if (!names.contains(definition.name())) {
                result.add(definition);
            }
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    @RequiresReadLock
    public @NotNull Collection<VariableDefinition> getUserDefinedVariables(@NotNull PsiFile file) {
        Map<String, VariableDefinition> userData = file.getUserData(RUNTIME_VALUES_KEY);
        if (userData == null) {
            return List.of();
        }
        return userData.values();
    }

    @Override
    public void updateUserDefinedVariables(@NotNull PsiFile file, @NotNull Collection<VariableDefinition> definitions) {
        Map<String, VariableDefinition> userData = file.getUserData(RUNTIME_VALUES_KEY);
        if (userData == null) {
            userData = new HashMap<>();
            file.putUserData(RUNTIME_VALUES_KEY, userData);
        }
        for (VariableDefinition definition : definitions) {
            if (definition.value() == null) {
                userData.remove(definition.name());
            } else {
                userData.put(definition.name(), definition);
            }
        }
        runtimeValuesTracker(file).incModificationCount();
        try {
            IntelliJUtils.retriggerValidations(file);
        } catch (Exception error) {
            logger.warn("Could not retrigger code analysis after updating user defined variables", error);
        }
    }

    @Override
    public @NotNull ModificationTracker getUserDefinedVariablesTracker(@NotNull PsiFile file) {
        return runtimeValuesTracker(file);
    }

    private @NotNull SimpleModificationTracker runtimeValuesTracker(@NotNull PsiFile file) {
        SimpleModificationTracker tracker = file.getUserData(RUNTIME_VALUES_TRACKER_KEY);
        if (tracker == null) {
            tracker = ((UserDataHolderEx) file).putUserDataIfAbsent(RUNTIME_VALUES_TRACKER_KEY, new SimpleModificationTracker());
        }
        return tracker;
    }

    private @NotNull PsiElement findClosestDefinition(@NotNull String path, @NotNull List<PsiElement> definitions) {
        Path myFile = Path.of(path).normalize();
        PsiElement closestDefinition = definitions.getFirst();
        int commonSegments = -1;

        for (PsiElement definition : definitions) {
            Path itsFile = Path.of(definition.getContainingFile().getVirtualFile().getPath()).normalize();
            if (itsFile.getNameCount() <= myFile.getNameCount()) {
                int matchingSegments = 0;
                for (int i = 0; i < itsFile.getNameCount(); i++) {
                    if (itsFile.getName(i).equals(myFile.getName(i))) {
                        matchingSegments++;
                    }
                }
                if (matchingSegments > commonSegments) {
                    closestDefinition = definition;
                    commonSegments = matchingSegments;
                }
            }
        }

        return closestDefinition;
    }

    private @Nullable String getVariableValue(@Nullable JsonValue value) {
        if (value == null) {
            return null;
        }
        return switch (value) {
            case JsonStringLiteral literal -> "\"" + literal.getValue() + "\"";
            case JsonNumberLiteral literal -> String.valueOf(literal.getValue());
            case JsonBooleanLiteral literal -> String.valueOf(literal.getValue());
            case JsonNullLiteral ignored -> NULL_VARIABLE_PLACEHOLDER;
            case JsonObject object -> {
                JsonProperty $type = object.findProperty("$type");
                if ($type != null && $type.getValue() instanceof JsonStringLiteral literal && literal.getValue().equals("dql")) {
                    JsonProperty fragment = object.findProperty("dql");
                    if (fragment != null && fragment.getValue() instanceof JsonStringLiteral valueLiteral) {
                        yield valueLiteral.getValue();
                    }
                }
                StringBuilder builder = new StringBuilder("record(");
                boolean first = true;
                for (JsonProperty jsonProperty : object.getPropertyList()) {
                    if (!first) {
                        builder.append(", ");
                    }
                    first = false;
                    builder.append(jsonProperty.getName()).append(" = ").append(getVariableValue(jsonProperty.getValue()));
                }
                builder.append(")");
                yield builder.toString();
            }
            case JsonArray array -> {
                StringBuilder builder = new StringBuilder("array(");
                boolean first = true;
                for (JsonValue jsonValue : array.getValueList()) {
                    if (!first) {
                        builder.append(", ");
                    }
                    first = false;
                    builder.append(getVariableValue(jsonValue));
                }
                builder.append(")");
                yield builder.toString();
            }
            default -> null;
        };
    }

    private @NotNull Set<String> getDataType(@Nullable JsonValue value) {
        return switch (value) {
            case JsonStringLiteral ignored -> Set.of("dql.dataType.string");
            case JsonNumberLiteral ignored -> Set.of("dql.dataType.double", "dql.dataType.long");
            case JsonNullLiteral ignored -> Set.of("dql.dataType.null");
            case JsonBooleanLiteral ignored -> Set.of("dql.dataType.boolean");
            case JsonObject object -> {
                JsonProperty $type = object.findProperty("$type");
                // for injected DQL fragments we do not know what type it produces
                if ($type != null && $type.getValue() instanceof JsonStringLiteral literal && literal.getValue().equals("dql")) {
                    yield Set.of();
                }
                yield Set.of("dql.dataType.record");
            }
            case JsonArray ignored -> Set.of("dql.dataType.array");
            case null, default -> Set.of();
        };
    }
}
