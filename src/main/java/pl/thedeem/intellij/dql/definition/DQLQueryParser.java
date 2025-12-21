package pl.thedeem.intellij.dql.definition;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.psi.DQLVariableExpression;

import java.util.List;

public interface DQLQueryParser {
    static @NotNull DQLQueryParser getInstance(@NotNull Project project) {
        return project.getService(DQLQueryParser.class);
    }

    @NotNull ParseResult getSubstitutedQuery(@NotNull PsiFile psiFile);

    record OffsetMapping(
            int originalStartOffset,
            int substitutedStartOffset,
            int originalLength,
            int substitutedLength) {
    }

    record Replacement(DQLVariableExpression variable, String value) {
    }

    record ParseResult(
            @NotNull List<OffsetMapping> offsetMappings,
            @NotNull String parsed) {
        public int getOriginalOffset(int substitutedOffset) {
            if (substitutedOffset < 0 || substitutedOffset > parsed.length()) {
                return -1;
            }

            int originalOffset = substitutedOffset;
            for (OffsetMapping mapping : offsetMappings) {
                // if it's inside the variable, let's just return the original "$" position
                if (substitutedOffset >= mapping.substitutedStartOffset()
                        && substitutedOffset < (mapping.substitutedStartOffset() + mapping.substitutedLength())) {
                    return mapping.originalStartOffset();
                }

                if (substitutedOffset >= mapping.substitutedStartOffset()) {
                    originalOffset -= (mapping.substitutedLength() - mapping.originalLength());
                } else {
                    break;
                }
            }
            return Math.max(originalOffset, 0);
        }
    }
}
