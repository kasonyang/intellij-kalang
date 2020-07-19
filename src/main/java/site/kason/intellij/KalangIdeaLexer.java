package site.kason.intellij;

import kalang.compiler.antlr.KalangLexer;
import org.antlr.intellij.adaptor.lexer.ANTLRLexerAdaptor;

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
}
