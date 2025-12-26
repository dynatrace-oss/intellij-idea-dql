package pl.thedeem.intellij.dql.services.query;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.psi.elements.BaseTypedElement;

import java.util.Collection;
import java.util.List;

public class DQLFieldNamesServiceImpl implements DQLFieldNamesService {
    public @NotNull String calculateFieldName(@Nullable Object... parts) {
        StringBuilder nameParts = new StringBuilder();
        for (Object part : parts) {
            String fieldPart = switch (part) {
                case BaseTypedElement e -> e.getFieldName();
                case PsiElement e -> e.getText().trim();
                case Collection<?> children ->
                        String.join(",", children.stream().map(this::calculateFieldName).toList());
                case SeparatedChildren children -> {
                    List<String> calculated = children.children().stream().map(this::calculateFieldName).toList();
                    if (children.separator() != null) {
                        yield String.join(calculateFieldName(children.separator()), calculated);
                    }
                    yield String.join(",", calculated);
                }
                case null -> null;
                default -> part.toString().trim();
            };
            if (fieldPart != null) {
                nameParts.append(fieldPart);
            }
        }
        return nameParts.toString();
    }
}
