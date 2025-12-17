package pl.thedeem.intellij.dql.intentions;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.psi.PsiUtils;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.DynatraceQueryLanguage;
import pl.thedeem.intellij.dql.definition.model.Command;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.psi.DQLFieldExpression;
import pl.thedeem.intellij.dql.psi.DQLParenthesisedExpression;
import pl.thedeem.intellij.dql.psi.DQLQueryStatement;
import pl.thedeem.intellij.dql.psi.DQLSimpleExpression;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DQLJoinFilterCommands extends PsiElementBaseIntentionAction implements Iconable {
    private final static Set<String> FILTERABLE_COMMANDS = Set.of("filter", "filterOut");

    public DQLJoinFilterCommands() {
        setText(DQLBundle.message("intention.joinFilterCommands"));
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) {
        DQLQueryStatement command = PsiTreeUtil.getParentOfType(psiElement, DQLQueryStatement.class);
        if (command == null || !isFilter(command)) {
            return false;
        }
        return ((PsiUtils.getNextElement(command) instanceof DQLQueryStatement next)
                && isFilter(next) && intersects(next, editor.getSelectionModel())) ||
                (PsiUtils.getPreviousElement(command) instanceof DQLQueryStatement previous
                        && isFilter(previous) && intersects(previous, editor.getSelectionModel()));
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws IncorrectOperationException {
        Document document = editor.getDocument();
        DQLQueryStatement command = PsiTreeUtil.getParentOfType(psiElement, DQLQueryStatement.class);
        if (command == null || !isFilter(command)) {
            return;
        }
        List<DQLQueryStatement> includedFilters = findSiblingFilters(command, editor.getSelectionModel());
        String newFilter = calculateNewFilterCommand(includedFilters);
        document.replaceString(
                includedFilters.getFirst().getTextRange().getStartOffset(),
                includedFilters.getLast().getTextRange().getEndOffset(),
                newFilter
        );
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return DynatraceQueryLanguage.DQL_DISPLAY_NAME;
    }

    @Override
    public Icon getIcon(int i) {
        return DQLIcon.INTENTION;
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }

    private boolean isFilter(@NotNull DQLQueryStatement command) {
        Command definition = command.getDefinition();
        if (definition == null) {
            return false;
        }
        return FILTERABLE_COMMANDS.contains(definition.name());
    }

    private @NotNull List<DQLQueryStatement> findSiblingFilters(@NotNull DQLQueryStatement command, @NotNull SelectionModel selectionModel) {
        List<DQLQueryStatement> result = new ArrayList<>();
        DQLQueryStatement current = command;
        while (PsiUtils.getPreviousElement(current) instanceof DQLQueryStatement previous && intersects(previous, selectionModel) && isFilter(previous)) {
            result.add(previous);
            current = previous;
        }
        result = result.reversed();
        result.add(command);
        current = command;
        while (PsiUtils.getNextElement(current) instanceof DQLQueryStatement next && intersects(next, selectionModel) && isFilter(next)) {
            result.add(next);
            current = next;
        }
        return result;
    }

    private @NotNull String calculateNewFilterCommand(@NotNull List<DQLQueryStatement> filters) {
        return "| filter " + String.join(" and ", filters.stream()
                .map(c -> {
                    Command definition = c.getDefinition();
                    MappedParameter condition = c.findParameter("condition");
                    if (definition == null || condition == null) {
                        return "";
                    }
                    List<String> parts = condition.getExpressions().stream().map(p -> {
                        if (p instanceof DQLParenthesisedExpression || p instanceof DQLSimpleExpression || p instanceof DQLFieldExpression) {
                            return p.getText();
                        }
                        return "(" + p.getText() + ")";
                    }).toList();
                    boolean isNegated = definition.name().equalsIgnoreCase("filterOut");
                    return isNegated ? "(not " + String.join(", ", parts) + ")" : String.join(", ", parts);
                })
                .toList());
    }

    private boolean intersects(@NotNull PsiElement element, @NotNull SelectionModel selectionModel) {
        if (selectionModel.getSelectionStart() == selectionModel.getSelectionEnd()) {
            return true;
        }
        return element.getTextRange().intersects(selectionModel.getSelectionStart(), selectionModel.getSelectionEnd());
    }
}
