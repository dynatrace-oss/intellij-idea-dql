package pl.thedeem.intellij.dql.code;

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler;
import pl.thedeem.intellij.dql.psi.DQLTokenSets;

public class DQLQuoteHandler extends SimpleTokenSetQuoteHandler {
    public DQLQuoteHandler() {
        super(DQLTokenSets.STRING_QUOTES);
    }
}
