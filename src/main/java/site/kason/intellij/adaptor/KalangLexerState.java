package site.kason.intellij.adaptor;

import kalang.compiler.antlr.KalangLexer;
import org.antlr.intellij.adaptor.lexer.ANTLRLexerState;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.misc.IntegerStack;
import org.antlr.v4.runtime.misc.MurmurHash;
import org.jetbrains.annotations.Nullable;

/**
 * @author KasonYang
 */
public class KalangLexerState extends ANTLRLexerState {

    private boolean inString;

    public KalangLexerState(int mode, @Nullable IntegerStack modeStack, boolean inString) {
        super(mode, modeStack);
        this.inString = inString;
    }

    @Override
    public void apply(Lexer lexer) {
        super.apply(lexer);
        if (lexer instanceof KalangLexer) {
            ((KalangLexer)lexer).inString  = inString;
        }
    }

    @Override
    protected int hashCodeImpl() {
        int hash = MurmurHash.initialize();
        hash = MurmurHash.update(hash, getMode());
        hash = MurmurHash.update(hash, getModeStack());
        hash = MurmurHash.update(hash, inString);
        return MurmurHash.finish(hash, 3);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof KalangLexerState)) {
            return false;
        }

        if (!super.equals(obj)) {
            return false;
        }

        KalangLexerState other = (KalangLexerState) obj;
        return this.inString == other.inString;
    }

}
