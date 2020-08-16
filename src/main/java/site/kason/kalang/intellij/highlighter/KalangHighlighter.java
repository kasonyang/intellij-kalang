package site.kason.kalang.intellij.highlighter;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import kalang.compiler.antlr.KalangLexer;
import kalang.compiler.antlr.KalangParser;
import org.antlr.intellij.adaptor.lexer.PSIElementTypeFactory;
import org.antlr.intellij.adaptor.lexer.TokenIElementType;
import org.jetbrains.annotations.NotNull;
import site.kason.kalang.intellij.KalangLanguage;
import site.kason.kalang.intellij.KalangIdeaLexer;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

/**
 * @author KasonYang
 */
public class KalangHighlighter extends SyntaxHighlighterBase {

    private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];
    public static final TextAttributesKey ID =
            createTextAttributesKey("SAMPLE_ID", DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey KEYWORD =
            createTextAttributesKey("SAMPLE_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey STRING =
            createTextAttributesKey("SAMPLE_STRING", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey LINE_COMMENT =
            createTextAttributesKey("SAMPLE_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey BLOCK_COMMENT =
            createTextAttributesKey("SAMPLE_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT);
    public static final TextAttributesKey NUMBER =
            createTextAttributesKey("SAMPLE_NUMBER", DefaultLanguageHighlighterColors.NUMBER);

    static {
        PSIElementTypeFactory.defineLanguageIElementTypes(KalangLanguage.INSTANCE,
                KalangParser.tokenNames,
                KalangParser.ruleNames);
    }

    @Override
    public @NotNull Lexer getHighlightingLexer() {
        return new KalangIdeaLexer();
    }

    @NotNull
    @Override
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
        if (!(tokenType instanceof TokenIElementType)) {
            return EMPTY_KEYS;
        }
        TokenIElementType type = (TokenIElementType) tokenType;
        int typeId = type.getANTLRTokenType();
        TextAttributesKey attrKey = getAttrKey(type.getANTLRTokenType());
        if (attrKey == null) {
            return EMPTY_KEYS;
        }
        return new TextAttributesKey[]{attrKey};
    }

    private TextAttributesKey getAttrKey(int tokenTypeId) {
        switch (tokenTypeId) {
            case KalangLexer.Identifier:
                return ID;
            case KalangLexer.ABSTRACT:
            case KalangLexer.AS:
            case KalangLexer.ASSERT:
            case KalangLexer.BOOLEAN:
            case KalangLexer.BREAK:
            case KalangLexer.BYTE:
            case KalangLexer.BooleanLiteral:
            case KalangLexer.CASE:
            case KalangLexer.CATCH:
            case KalangLexer.CHAR:
            case KalangLexer.CLASS:
            case KalangLexer.CONST:
            case KalangLexer.CONSTRUCTOR:
            case KalangLexer.CONTINUE:
            case KalangLexer.DEFAULT:
            case KalangLexer.DO:
            case KalangLexer.DOUBLE:
            case KalangLexer.ELSE:
            case KalangLexer.ENUM:
            case KalangLexer.EXTENDS:
            case KalangLexer.FINAL:
            case KalangLexer.FINALLY:
            case KalangLexer.FLOAT:
            case KalangLexer.FOR:
            case KalangLexer.FOREACH:
            case KalangLexer.GOTO:
            case KalangLexer.IF:
            case KalangLexer.IMPLEMENTS:
            case KalangLexer.IMPORT:
            case KalangLexer.IN:
            case KalangLexer.INSTANCEOF:
            case KalangLexer.INT:
            case KalangLexer.INTERFACE:
            case KalangLexer.LONG:
            case KalangLexer.NATIVE:
            case KalangLexer.NEW:
            case KalangLexer.NullLiteral:
            case KalangLexer.OVERRIDE:
            case KalangLexer.PACKAGE:
            case KalangLexer.PRIVATE:
            case KalangLexer.PROTECTED:
            case KalangLexer.PUBLIC:
            case KalangLexer.RETURN:
            case KalangLexer.SHORT:
            case KalangLexer.STATIC:
            case KalangLexer.STRICTFP:
            case KalangLexer.SUPER:
            case KalangLexer.SWITCH:
            case KalangLexer.SYNCHRONIZED:
            case KalangLexer.THIS:
            case KalangLexer.THROW:
            case KalangLexer.THROWS:
            case KalangLexer.TRANSIENT:
            case KalangLexer.TRY:
            case KalangLexer.VAL:
            case KalangLexer.VAR:
            case KalangLexer.VOID:
            case KalangLexer.VOLATILE:
            case KalangLexer.WHILE:
            case KalangLexer.MIXIN:
                return KEYWORD;
            case KalangLexer.StringLiteral:
            case KalangLexer.MultiLineStringLiteral:
            case KalangLexer.InterpolationPreffixString:
            case KalangLexer.INTERPOLATION_INTERUPT:
            case KalangLexer.INTERPOLATION_END:
            case KalangLexer.INTERPOLATION_STRING:
                return STRING;
            case KalangLexer.COMMENT:
            case KalangLexer.LINE_COMMENT:
                return LINE_COMMENT;
            case KalangLexer.IntegerLiteral:
            case KalangLexer.FloatingPointLiteral:
                return NUMBER;
            default:
                return null;
        }
    }
}


