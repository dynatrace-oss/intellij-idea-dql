package pl.thedeem.intellij.dql;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import pl.thedeem.intellij.dql.psi.DQLTokenSets;
import pl.thedeem.intellij.dql.psi.DQLTypes;
import org.jetbrains.annotations.NotNull;

final class DQLParserDefinition implements ParserDefinition {
    public static final IFileElementType FILE = new IFileElementType(DynatraceQueryLanguage.INSTANCE);

    @NotNull
    @Override
    public Lexer createLexer(Project project) {
        return new DQLLexerAdapter();
    }

    @NotNull
    @Override
    public TokenSet getCommentTokens() {
        return DQLTokenSets.COMMENTS;
    }

    @NotNull
    @Override
    public TokenSet getStringLiteralElements() {
        return DQLTokenSets.STRING_LITERALS;
    }

    @NotNull
    @Override
    public PsiParser createParser(final Project project) {
        return new DQLParser();
    }

    @NotNull
    @Override
    public IFileElementType getFileNodeType() {
        return FILE;
    }

    @NotNull
    @Override
    public PsiFile createFile(@NotNull FileViewProvider viewProvider) {
        return new DQLFile(viewProvider);
    }

    @NotNull
    @Override
    public PsiElement createElement(ASTNode node) {
        return DQLTypes.Factory.createElement(node);
    }
}
