package site.kason.intellij;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import kalang.compiler.antlr.KalangLexer;
import kalang.compiler.antlr.KalangParser;
import org.antlr.intellij.adaptor.lexer.PSIElementTypeFactory;
import org.antlr.intellij.adaptor.lexer.TokenIElementType;
import org.antlr.intellij.adaptor.psi.ANTLRPsiNode;
import org.jetbrains.annotations.NotNull;
import site.kason.intellij.psi.RulePsiNode;

/**
 * @author KasonYang
 */
public class KalangIdeaParserDefinition implements ParserDefinition {

    public static final IFileElementType FILE = new IFileElementType(KalangLanguage.INSTANCE);

    static {
        PSIElementTypeFactory.defineLanguageIElementTypes(KalangLanguage.INSTANCE,
                KalangParser.tokenNames,
                KalangParser.ruleNames);
        //List<TokenIElementType> tokenIElementTypes =
        //        PSIElementTypeFactory.getTokenIElementTypes(KalangLanguage.INSTANCE);
        //ID = tokenIElementTypes.get(KalangLexer.Identifier);
    }

    public static final TokenSet COMMENTS =
            PSIElementTypeFactory.createTokenSet(
                    KalangLanguage.INSTANCE,
                    KalangLexer.COMMENT,
                    KalangLexer.LINE_COMMENT);

    public static final TokenSet WHITESPACE =
            PSIElementTypeFactory.createTokenSet(
                    KalangLanguage.INSTANCE,
                    KalangLexer.WS);

    public static final TokenSet STRING =
            PSIElementTypeFactory.createTokenSet(
                    KalangLanguage.INSTANCE,
                    KalangLexer.STRING);

    @Override
    public @NotNull Lexer createLexer(Project project) {
        KalangLexer lexer = new KalangLexer(null);
        return new KalangIdeaLexer(lexer);
    }

    @Override
    public PsiParser createParser(Project project) {
        return new KalangIdeaParser();
    }

    @Override
    public IFileElementType getFileNodeType() {
        return FILE;
    }

    @Override
    public @NotNull TokenSet getCommentTokens() {
        return COMMENTS;
    }

    @Override
    public @NotNull TokenSet getWhitespaceTokens() {
        return WHITESPACE;
    }

    @Override
    public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
        return SpaceRequirements.MAY;
    }

    @Override
    public @NotNull TokenSet getStringLiteralElements() {
        return STRING;
    }

    @Override
    public @NotNull PsiElement createElement(ASTNode node) {
        IElementType elType = node.getElementType();
        if (!(elType instanceof TokenIElementType)) {
            return new ANTLRPsiNode(node);
        }
        return new RulePsiNode(node);
    }

    @Override
    public PsiFile createFile(FileViewProvider viewProvider) {
        return new KalangFile(viewProvider);
    }

}
