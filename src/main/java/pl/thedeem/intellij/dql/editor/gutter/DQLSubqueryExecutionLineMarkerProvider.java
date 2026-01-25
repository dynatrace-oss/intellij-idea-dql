package pl.thedeem.intellij.dql.editor.gutter;

import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLFile;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.psi.DQLQuery;

import javax.swing.*;

public class DQLSubqueryExecutionLineMarkerProvider extends DQLFileQueryExecutionLineMarkerProvider {
    @Override
    public String getName() {
        return DQLBundle.message("gutter.executeDQL.subquery.name");
    }

    @Override
    public @NotNull Icon getIcon() {
        return DQLIcon.GUTTER_EXECUTE_DQL;
    }

    protected boolean isEnabled(@NotNull DQLQuery query) {
        return !(query.getParent() instanceof DQLFile);
    }
}
