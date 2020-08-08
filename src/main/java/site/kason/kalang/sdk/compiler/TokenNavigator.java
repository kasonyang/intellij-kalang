package site.kason.kalang.sdk.compiler;

import org.antlr.v4.runtime.Token;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

/**
 * @author KasonYang
 */
public class TokenNavigator {

    private Token[] tokens;

    private int caretOffset;

    private int maxCaretOffset=-1;

    private final int[] startOffset;

    private int currentTokenIndex = 0;

    public TokenNavigator(@Nonnull List<Token> tokens){
        this(tokens.toArray(new Token[0]));
    }

    public TokenNavigator(@Nonnull Token[] tokens) {
        this.tokens = tokens;
        startOffset = new int[tokens.length];
        if(tokens.length>0){
            for (int i=0;i<tokens.length;i++) {
                startOffset[i] = tokens[i].getStartIndex();
            }
            maxCaretOffset = tokens[tokens.length-1].getStopIndex();
        }
    }

    /**
     *  move offset to specified position
     * @param caretOffset new caret offset
     * @return old caret offset
     */
    public int moveCaret(int caretOffset){
        if(caretOffset<0 || caretOffset > maxCaretOffset){
            throw new IndexOutOfBoundsException(String.valueOf(caretOffset));
        }
        currentTokenIndex = getTokenIndexByCaretOffset(caretOffset);
        int oldCaret = this.caretOffset;
        this.caretOffset = caretOffset;
        return oldCaret;
    }

    /**
     * move token index to specified position
     * @param tokenIndex new token index
     * @return old token index
     */
    public int moveIndex(int tokenIndex) {
        if (tokenIndex < 0 || tokenIndex >= tokens.length) {
            throw new IndexOutOfBoundsException(String.valueOf(tokenIndex));
        }
        int oldIndex = currentTokenIndex;
        this.currentTokenIndex = tokenIndex;
        return oldIndex;
    }

    public int advance(int tokenCount, int channel) {
        int newIdx = lookIndex(1, tokenCount, channel);
        if (newIdx == -1) {
            throw new IndexOutOfBoundsException();
        }
        return moveIndex(newIdx);
    }

    public int back(int tokenCount, int channel) {
        int newIdx = lookIndex(-1, tokenCount, channel);
        if (newIdx == -1) {
            throw new IndexOutOfBoundsException();
        }
        return moveIndex(newIdx);
    }

    public Token currentToken() {
        return tokens[currentTokenIndex];
    }

    @Nullable
    public Token loadAhead(int tokenCount, int channel) {
        if (tokenCount <= 0) {
            throw new IllegalArgumentException("invalid token count");
        }
        int laIdx = lookIndex(1, tokenCount, channel);
        if (laIdx == -1) {
            return  null;
        }
        return tokens[laIdx];
    }

    @Nullable
    public Token lookBack(int tokenCount, int channel) {
        if (tokenCount <= 0) {
            throw new IllegalArgumentException("invalid token count");
        }
        int laIdx = lookIndex(-1, tokenCount, channel);
        if (laIdx == -1) {
            return  null;
        }
        return tokens[laIdx];
    }

    public boolean hasNext(int channel){
        return lookIndex(1, 1, channel) >= 0;
    }

    public boolean hasPrevious(int channel){
        return lookIndex(-1, 1, channel) >= 0;
    }

    private int lookIndex(int step, int tokenCount, int channel) {
        int newIdx = currentTokenIndex;
        int movedCount = 0 ;
        while(movedCount < tokenCount){
            newIdx += step;
            if(newIdx < 0 || newIdx >= tokens.length){
                return -1;
            }
            if(channel >= 0 && channel != tokens[newIdx].getChannel()){
                continue;
            }
            movedCount++;
        }
        return newIdx;
    }

    private int getTokenIndexByCaretOffset(int caretOffset) {
        int idx = Arrays.binarySearch(startOffset, caretOffset);
        if (idx < 0) {
            idx = -idx - 2;
            if (caretOffset > tokens[idx].getStopIndex()) {
                return -1;
            }
        }
        return idx;
    }

}
