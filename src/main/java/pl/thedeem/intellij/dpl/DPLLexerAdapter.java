package pl.thedeem.intellij.dpl;

import com.intellij.lexer.FlexAdapter;
import pl.thedeem.intellij.dpl.psi._DPLLexer;

public class DPLLexerAdapter extends FlexAdapter {
    public DPLLexerAdapter() {
        super(new _DPLLexer(null));
    }
}
