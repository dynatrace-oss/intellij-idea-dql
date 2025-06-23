package pl.thedeem.intellij.dql;

import com.intellij.lexer.FlexAdapter;
import pl.thedeem.intellij.dql.psi._DQLLexer;

public class DQLLexerAdapter extends FlexAdapter {
    public DQLLexerAdapter() {
        super(new _DQLLexer(null));
    }
}
