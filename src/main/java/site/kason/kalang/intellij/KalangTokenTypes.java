package site.kason.kalang.intellij;

import org.antlr.intellij.adaptor.lexer.PSIElementTypeFactory;
import org.antlr.intellij.adaptor.lexer.RuleIElementType;
import org.antlr.intellij.adaptor.lexer.TokenIElementType;

import java.util.List;

/**
 * @author KasonYang
 */
public class KalangTokenTypes {

    public static final List<TokenIElementType> TOKEN_ELEMENT_TYPES =
            PSIElementTypeFactory.getTokenIElementTypes(KalangLanguage.INSTANCE);
    public static final List<RuleIElementType> RULE_ELEMENT_TYPES =
            PSIElementTypeFactory.getRuleIElementTypes(KalangLanguage.INSTANCE);


    public static RuleIElementType getRuleElementType(int ruleIndex) {
        return RULE_ELEMENT_TYPES.get(ruleIndex);
    }

    public static TokenIElementType getTokenElementType(int ruleIndex) {
        return TOKEN_ELEMENT_TYPES.get(ruleIndex);
    }
    
}
