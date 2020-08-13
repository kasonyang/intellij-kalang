package site.kason.kalang.intellij;

import kalang.compiler.antlr.KalangLexer;
import org.antlr.intellij.adaptor.lexer.ANTLRLexerAdaptor;
import org.antlr.intellij.adaptor.lexer.ANTLRLexerState;
import org.antlr.v4.runtime.Lexer;
import site.kason.kalang.intellij.adaptor.KalangLexerState;

/**
 * @author KasonYang
 */
public class KalangIdeaLexer extends ANTLRLexerAdaptor {

    public KalangIdeaLexer(KalangLexer lexer) {
        super(KalangLanguage.INSTANCE, lexer);
    }

    public KalangIdeaLexer() {
        super(KalangLanguage.INSTANCE, new KalangLexer(null));
    }

    @Override
    protected ANTLRLexerState getLexerState(Lexer lexer) {
        if (lexer._modeStack.isEmpty()) {
            return new KalangLexerState(lexer._mode, null, ((KalangLexer) lexer).inString);
        }

        return new KalangLexerState(lexer._mode, lexer._modeStack, ((KalangLexer) lexer).inString);
    }
}
