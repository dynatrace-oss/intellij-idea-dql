package pl.thedeem.intellij.dql.indexing;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.psi.DQLAssignExpression;
import pl.thedeem.intellij.dql.psi.DQLFieldExpression;
import pl.thedeem.intellij.dql.psi.DQLQuery;

import java.util.*;

public record ReferenceVariantsCalculator(DQLFieldExpression field) {

    public @Nullable DQLAssignExpression getAssignExpression() {
        if (field.getParent() instanceof DQLAssignExpression assignExpression && field == assignExpression.getLeftExpression()) {
            return assignExpression;
        }

        List<DQLFieldExpression> fields = DQLUtil.findFieldsInFile(this.field.getContainingFile());
        DQLAssignExpression result = null;
        int maxOffset = calculateMaxOffset(field);
        DQLQuery parentQuery = field.getParentQuery();
        for (DQLFieldExpression otherField : fields) {
            if (parentQuery == otherField.getParentQuery()
                    && Objects.equals(field.getName(), otherField.getName())
                    && otherField.getParent() instanceof DQLAssignExpression assignExpression
                    && otherField == assignExpression.getLeftExpression()) {
                int fieldOffset = assignExpression.getTextRange().getEndOffset();
                if (fieldOffset > maxOffset) {
                    break;
                }
                result = assignExpression;
            }
        }
        return result;
    }

    public Collection<VariantNode> calculateVariants() {
        List<DQLFieldExpression> fields = DQLUtil.findFieldsInFile(this.field.getContainingFile());
        Map<String, VariantNode> variants = new HashMap<>();
        int maxOffset = calculateMaxOffset(field);
        DQLQuery parentQuery = field.getParentQuery();
        for (DQLFieldExpression otherField : fields) {
            String fValue = null;
            if (parentQuery != otherField.getParentQuery()) {
                continue;
            }
            if (otherField.getParent() instanceof DQLAssignExpression assignExpression && assignExpression.getLeftExpression() == otherField) {
                PsiElement value = assignExpression.getRightExpression();
                if (value != null) {
                    fValue = value.getText();
                }
            }
            handleVariant(maxOffset, otherField, fValue, variants);
        }
        return variants.values();
    }

    private void handleVariant(int maxOffset, DQLFieldExpression otherField, String value, Map<String, VariantNode> variants) {
        int fieldOffset = otherField.getTextRange().getEndOffset();
        if (fieldOffset < maxOffset) {
            String fieldName = otherField.getName();
            if (value != null) {
                variants.put(fieldName, new VariantNode(otherField, fieldName, value));
            } else {
                variants.putIfAbsent(fieldName, new VariantNode(otherField, fieldName, null));
            }
        }
    }

    private int calculateMaxOffset(DQLFieldExpression field) {
        DQLAssignExpression assign = PsiTreeUtil.getParentOfType(field, DQLAssignExpression.class);
        if (assign != null) {
            return assign.getTextRange().getStartOffset();
        }
        return field.getTextRange().getEndOffset();
    }

    public static class VariantNode {
        public final String name;
        public final DQLFieldExpression field;
        public final String value;
        public final String fullValue;

        public VariantNode(DQLFieldExpression field, String name, String value) {
            this.field = field;
            this.name = name;
            this.value = value != null ? sanitizeNodeValue(value) : null;
            this.fullValue = value;
        }

        private String sanitizeNodeValue(String value) {
            return StringUtil.first(
                    value
                            .replaceAll("\n", "")
                            .replaceAll("\\s+", " "),
                    15, true
            );
        }
    }
}