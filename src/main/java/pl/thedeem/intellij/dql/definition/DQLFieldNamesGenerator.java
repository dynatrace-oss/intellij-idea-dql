package pl.thedeem.intellij.dql.definition;

import com.intellij.psi.PsiElement;
import pl.thedeem.intellij.dql.psi.elements.BaseTypedElement;

import java.util.Collection;

public class DQLFieldNamesGenerator {
    private final StringBuilder nameParts;

    public DQLFieldNamesGenerator() {
        nameParts = new StringBuilder();
    }

    public DQLFieldNamesGenerator addPart(Object part) {
        if (part == null) {
            return this;
        }
        nameParts.append(calculateName(part));
        return this;
    }

    public DQLFieldNamesGenerator addPart(Collection<?> children, Object separator) {
        if (children == null || children.isEmpty()) {
            return this;
        }

        int i = 0;
        for (Object child : children) {
            addPart(child);
            if (separator != null && i < children.size() - 1) {
                addPart(calculateName(separator));
            }
            i++;
        }

        return this;
    }

    public String getFieldName() {
        return nameParts.toString();
    }

    private String calculateName(Object obj) {
        return switch (obj) {
            case BaseTypedElement e -> e.getFieldName();
            case PsiElement e -> e.getText().trim();
            case null -> null;
            default -> obj.toString().trim();
        };
    }
}
