package site.kason.intellij;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import kalang.compiler.antlr.KalangParser;
import org.antlr.intellij.adaptor.parser.ANTLRParserAdaptor;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.tree.ParseTree;
import site.kason.intellij.KalangLanguage;

/**
 * @author KasonYang
 */
public class KalangIdeaParser extends ANTLRParserAdaptor {

    public KalangIdeaParser() {
        super(KalangLanguage.INSTANCE, new KalangParser(null));
    }

    @Override
    protected ParseTree parse(Parser parser, IElementType root) {
        if (root instanceof IFileElementType) {
            return ((KalangParser) parser).compilationUnit();
        }
        throw new UnsupportedOperationException("unknown rule");
    }
}
