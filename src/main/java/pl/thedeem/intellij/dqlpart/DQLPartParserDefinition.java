package pl.thedeem.intellij.dqlpart;

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
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLLexerAdapter;
import pl.thedeem.intellij.dql.DQLParser;
import pl.thedeem.intellij.dql.psi.DQLTokenSets;
import pl.thedeem.intellij.dql.psi.DQLTypes;

public class DQLPartParserDefinition implements ParserDefinition {
    public static final IFileElementType FILE = new IFileElementType(DQLPartLanguage.INSTANCE);

    @Override
    public @NotNull Lexer createLexer(Project project) {
        return new DQLLexerAdapter();
    }

    @Override
    public @NotNull TokenSet getCommentTokens() {
        return DQLTokenSets.COMMENTS;
    }

    @Override
    public @NotNull TokenSet getStringLiteralElements() {
        return DQLTokenSets.STRING_LITERALS;
    }

    @Override
    public @NotNull PsiParser createParser(Project project) {
        return new DQLParser();
    }

    @Override
    public @NotNull IFileElementType getFileNodeType() {
        return FILE;
    }

    @Override
    public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
        return new DQLPartFile(viewProvider);
    }

    @Override
    public @NotNull PsiElement createElement(ASTNode node) {
        return DQLTypes.Factory.createElement(node);
    }
}
