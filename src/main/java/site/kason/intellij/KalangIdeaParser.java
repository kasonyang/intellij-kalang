package site.kason.intellij;

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

    public KalangIdeaParser() {
        super(KalangLanguage.INSTANCE, new KalangParser(null));
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
            return ((KalangParser) parser).compilationUnit();
        }
        throw new UnsupportedOperationException("unknown rule");
    }
}
