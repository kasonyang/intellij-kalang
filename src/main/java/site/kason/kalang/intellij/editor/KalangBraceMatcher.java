package site.kason.kalang.intellij.editor;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import site.kason.kalang.intellij.KalangTokenTypes;

import static kalang.compiler.antlr.KalangLexer.*;

/**
 * @author KasonYang
 */
public class KalangBraceMatcher implements PairedBraceMatcher {
    @NotNull
    @Override
    public BracePair[] getPairs() {
        return new BracePair[] {
                new BracePair(KalangTokenTypes.getTokenElementType(LPAREN), KalangTokenTypes.getTokenElementType(RPAREN), false),
                new BracePair(KalangTokenTypes.getTokenElementType(LBRACE), KalangTokenTypes.getTokenElementType(RBRACE), true),
                new BracePair(KalangTokenTypes.getTokenElementType(LBRACK), KalangTokenTypes.getTokenElementType(RBRACK), false),
                //new BracePair(getTokenElementType(LT), getTokenElementType(GT), false)
        };
    }

    @Override
    public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType lbraceType, @Nullable IElementType contextType) {
        return true;
    }

    @Override
    public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
        return openingBraceOffset;
    }
}
