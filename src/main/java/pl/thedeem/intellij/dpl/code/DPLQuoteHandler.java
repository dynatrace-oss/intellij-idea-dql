package pl.thedeem.intellij.dpl.code;

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler;
import pl.thedeem.intellij.dpl.psi.DPLTokenSets;

public class DPLQuoteHandler extends SimpleTokenSetQuoteHandler {
    public DPLQuoteHandler() {
        super(DPLTokenSets.STRING_QUOTES);
    }
}
