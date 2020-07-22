package site.kason.intellij.editor;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static kalang.compiler.antlr.KalangLexer.*;
import static site.kason.intellij.KalangTokenTypes.getTokenElementType;

/**
 * @author KasonYang
 */
public class KalangBraceMatcher implements PairedBraceMatcher {
    @NotNull
    @Override
    public BracePair[] getPairs() {
        return new BracePair[] {
                new BracePair(getTokenElementType(LPAREN), getTokenElementType(RPAREN), false),
                new BracePair(getTokenElementType(LBRACE), getTokenElementType(RBRACE), false),
                new BracePair(getTokenElementType(LBRACK), getTokenElementType(RBRACK), true),
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
