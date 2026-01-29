package pl.thedeem.intellij.dql;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;

/**
 * A wrapper for parsing DQL files that allows defining simple DQL expressions without the context
 * of a command.
 */
public class ExpressionDQLParser extends DQLParser {
    @Override
    protected boolean parse_root_(IElementType root_, PsiBuilder builder_) {
        command_parameters(builder_, 0);
        return true;
    }
}
