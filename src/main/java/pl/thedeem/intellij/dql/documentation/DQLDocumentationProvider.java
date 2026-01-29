package pl.thedeem.intellij.dql.documentation;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.documentation.providers.*;
import pl.thedeem.intellij.dql.psi.*;
import pl.thedeem.intellij.dql.psi.elements.impl.ExpressionOperatorImpl;

import java.util.Objects;

public class DQLDocumentationProvider extends AbstractDocumentationProvider {
    @Override
    public @Nullable String generateDoc(@NotNull PsiElement element, @Nullable PsiElement originalElement) {
        BaseDocumentationProvider<?> docsProvider = switch (element) {
            case DQLCommandKeyword keyword when keyword.getParent() instanceof DQLCommand command ->
                    new DQLCommandDocumentationProvider(command);
            case DQLParameterName parameterName -> {
                if (parameterName.getParent() instanceof DQLParameterExpression parameter) {
                    if (parameter.getParent() instanceof DQLCommand command) {
                        yield new DQLParameterDocumentationProvider(Objects.requireNonNull(command.getParameter(parameter)));
                    } else if (parameter.getParent() instanceof DQLFunctionExpression function) {
                        yield new DQLParameterDocumentationProvider(Objects.requireNonNull(function.getParameter(parameter)));
                    }
                }
                yield new BaseDocumentationProvider<>(parameterName, DQLBundle.message("documentation.parameter.type"), "AllIcons.Nodes.Parameter");
            }
            case DQLFunctionExpression func -> new DQLFunctionDocumentationProvider(func);
            case DQLFunctionName func when func.getParent() instanceof DQLFunctionExpression function ->
                    new DQLFunctionDocumentationProvider(function);
            case DQLFieldExpression field -> new BaseTypedElementDocumentationProvider<>(
                    originalElement != null && originalElement.getParent() instanceof DQLFieldExpression originalField ? originalField : field,
                    DQLBundle.message("documentation.field.type"),
                    "AllIcons.Nodes.Method");
            case DQLVariableExpression variable -> new DQLVariableDocumentationProvider(variable);
            case ExpressionOperatorImpl operator -> {
                if (operator.getParent() instanceof DQLExpression expression) {
                    yield new DQLExpressionDocumentationProvider(expression);
                }
                yield new BaseDocumentationProvider<>(operator, DQLBundle.message("documentation.operator.type"), "AllIcons.Nodes.Lambda");
            }
            case DQLNumber number ->
                    new BaseDocumentationProvider<>(number, DQLBundle.message("documentation.number.type"), "AllIcons.Nodes.PropertyReadStatic");
            case DQLDuration duration ->
                    new BaseDocumentationProvider<>(duration, DQLBundle.message("documentation.duration.type"), "AllIcons.Nodes.Type");
            case DQLSortDirection sortKeyword ->
                    new BaseDocumentationProvider<>(sortKeyword, DQLBundle.message("documentation.sorting.type"), "AllIcons.Nodes.Target");
            case DQLBoolean bool ->
                    new BaseDocumentationProvider<>(bool, DQLBundle.message("documentation.boolean.type"), "AllIcons.Nodes.Static");
            case DQLNull nullElement ->
                    new BaseDocumentationProvider<>(nullElement, DQLBundle.message("documentation.null.type"), "AllIcons.Nodes.C_private");
            case DQLStringContentElement content when content.getParent() instanceof DQLString string ->
                    new DQLStringDocumentationProvider(string);
            case DQLString string -> new DQLStringDocumentationProvider(string);
            default -> {
                if (originalElement != null && originalElement.getParent() instanceof DQLVariableExpression variable) {
                    yield new DQLVariableDocumentationProvider(variable);
                }
                yield new BaseDocumentationProvider<>(element);
            }
        };
        return docsProvider.generateDocumentation();
    }

    @Override
    public @Nullable String generateHoverDoc(@NotNull PsiElement element, @Nullable PsiElement originalElement) {
        return generateDoc(element, originalElement);
    }

    @Override
    public @Nullable PsiElement getCustomDocumentationElement(
            @NotNull Editor editor,
            @NotNull PsiFile file,
            @Nullable PsiElement context,
            int targetOffset) {
        if (context == null) {
            return null;
        }
        IElementType type = context.getNode().getElementType();
        if (DQLTypes.STRING_CONTENT == type) {
            return context.getParent().getParent();
        } else if (DQLTypes.MULTILINE_STRING_QUOTE == type
                || DQLTypes.DOUBLE_QUOTE == type
                || DQLTypes.SINGLE_QUOTE == type
                || DQLTypes.TICK_QUOTE == type
        ) {
            return context.getParent();
        }
        if (context instanceof DQLFieldExpression) {
            return context;
        }
        return super.getCustomDocumentationElement(editor, file, context, targetOffset);
    }
}
