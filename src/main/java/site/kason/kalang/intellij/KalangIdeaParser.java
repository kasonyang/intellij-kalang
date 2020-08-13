package site.kason.kalang.intellij;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import kalang.compiler.antlr.KalangParser;
import org.antlr.intellij.adaptor.parser.ANTLRParserAdaptor;
import org.antlr.intellij.adaptor.parser.SyntaxError;
import org.antlr.intellij.adaptor.parser.SyntaxErrorListener;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * @author KasonYang
 */
public class KalangIdeaParser extends ANTLRParserAdaptor {

    private boolean script;

    public KalangIdeaParser(boolean script) {
        super(KalangLanguage.INSTANCE, new KalangParser(null));
        this.script = script;
    }

    @Override
    protected ParseTree parse(Parser parser, IElementType root) {
        //replace builtin error listener
        parser.removeErrorListeners();
        parser.addErrorListener(new SyntaxErrorListener() {

            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                Token offendingToken = (Token) offendingSymbol;
                if (e instanceof NoViableAltException) {
                    msg = "unexpected token '" + offendingToken.getText() + "'";
                }
                getSyntaxErrors().add(new SyntaxError(recognizer, offendingToken, line, charPositionInLine, msg, e){
                    @Override
                    public Token getOffendingSymbol() {
                        return offendingToken;
                    }
                });
            }

        });
        if (root instanceof IFileElementType) {
            KalangParser kp = (KalangParser) parser;
            return script ? kp.scriptCompilationUnit() : kp.standardCompilationUnit();
        }
        throw new UnsupportedOperationException("unknown rule");
    }
}
