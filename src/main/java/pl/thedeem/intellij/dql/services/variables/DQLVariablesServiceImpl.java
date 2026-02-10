package pl.thedeem.intellij.dql.services.variables;

import com.intellij.json.JsonFileType;
import com.intellij.json.psi.*;
import com.intellij.openapi.application.ReadAction;
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
import pl.thedeem.intellij.dql.psi.DQLQuery;
import pl.thedeem.intellij.dql.psi.elements.VariableElement;

import java.nio.file.Path;
import java.util.*;

public final class DQLVariablesServiceImpl implements DQLVariablesService {
    private final static String DQL_VARIABLES_FILE = "dql-variables.json";
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
    public @NotNull PsiElement findClosestDefinition(@NotNull String path, @NotNull List<PsiElement> definitions) {
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

    @Override
    public @Nullable String getVariableValue(@Nullable JsonValue value) {
        if (value == null) {
            return null;
        }
        return switch (value) {
            case JsonStringLiteral literal -> "\"" + literal.getValue() + "\"";
            case JsonNumberLiteral literal -> String.valueOf(literal.getValue());
            case JsonBooleanLiteral literal -> String.valueOf(literal.getValue());
            case JsonNullLiteral ignored -> "null";
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

    @Override
    public @NotNull List<VariableDefinition> getDefinedVariables(@NotNull PsiFile file) {
        return ReadAction.compute(() -> {
            DQLQuery query = PsiTreeUtil.getChildOfType(file, DQLQuery.class);
            if (query == null) {
                return List.of();
            }
            List<VariableDefinition> result = new ArrayList<>();
            for (Map.Entry<String, List<VariableElement>> variable : query.getDefinedVariables().entrySet()) {
                String name = variable.getKey();
                if (!variable.getValue().isEmpty()) {
                    VariableElement element = variable.getValue().getFirst();
                    result.add(new VariableDefinition(name, element.getValue(), element.getDataType()));
                }
            }
            return Collections.unmodifiableList(result);
        });
    }
}
