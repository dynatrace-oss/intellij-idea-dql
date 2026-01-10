package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.json.psi.*;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.psi.DQLElementFactory;
import pl.thedeem.intellij.dql.psi.DQLTypes;
import pl.thedeem.intellij.dql.psi.elements.VariableElement;
import pl.thedeem.intellij.dql.services.query.DQLFieldNamesService;
import pl.thedeem.intellij.dql.services.variables.DQLVariablesService;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public abstract class VariableElementImpl extends ASTWrapperPsiElement implements VariableElement {
    private CachedValue<PsiElement> reference;

    public VariableElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String getName() {
        PsiElement nameIdentifier = this.getNameIdentifier();
        String text = Objects.requireNonNullElse(nameIdentifier, this).getText();
        return StringUtil.isNotEmpty(text) ? text.substring(1) : null;
    }

    @Override
    public PsiElement getNameIdentifier() {
        ASTNode keyNode = this.getNode().findChildByType(DQLTypes.IDENTIFIER);
        if (keyNode != null) {
            return keyNode.getPsi();
        }
        return this;
    }

    @Override
    public PsiElement setName(@NotNull String newName) {
        VariableElement variable = DQLElementFactory.createVariableElement(newName, getProject());
        PsiElement keyNode = getNameIdentifier();
        if (keyNode == null) {
            replace(variable);
        } else {
            keyNode.replace(Objects.requireNonNull(variable.getNameIdentifier()));
        }
        return this;
    }

    @Override
    public String getFieldName() {
        return DQLFieldNamesService.getInstance(getProject()).calculateFieldName(getName());
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(this.getName(), this, DQLIcon.DQL_VARIABLE);
    }

    @Override
    public PsiReference @NotNull [] getReferences() {
        return ReferenceProvidersRegistry.getReferencesFromProviders(this);
    }

    @Override
    public boolean accessesData() {
        return false;
    }

    @Override
    public @NotNull Set<String> getDataType() {
        PsiElement definition = getDefinition();
        if (definition instanceof JsonProperty jsonProperty && jsonProperty.getValue() != null) {
            return switch (jsonProperty.getValue()) {
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
                default -> Set.of();
            };
        }
        return Set.of();
    }

    @Override
    public @Nullable PsiElement getDefinition() {
        if (reference == null) {
            reference = CachedValuesManager.getManager(getProject()).createCachedValue(
                    () -> {
                        PsiElement dqlVariableReference = recalculateReference();
                        if (dqlVariableReference != null) {
                            return new CachedValueProvider.Result<>(dqlVariableReference, this, dqlVariableReference);
                        }
                        return new CachedValueProvider.Result<>(null, this);
                    },
                    false
            );
        }
        return reference.getValue();
    }

    @Override
    public @Nullable String getValue() {
        PsiElement definition = getDefinition();
        if (definition instanceof JsonProperty property) {
            DQLVariablesService service = DQLVariablesService.getInstance(getProject());
            return service.getVariableValue(property.getValue());
        }
        return null;
    }

    private PsiElement recalculateReference() {
        String name = getName();
        if (name != null) {
            DQLVariablesService service = DQLVariablesService.getInstance(getProject());
            List<PsiElement> definitions = service.findVariableDefinitionFiles(name, getContainingFile());
            if (!definitions.isEmpty()) {
                return service.findClosestDefinition(getContainingFile().getVirtualFile().getPath(), definitions);
            }
        }
        return null;
    }
}
