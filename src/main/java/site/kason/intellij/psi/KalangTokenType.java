package site.kason.intellij.psi;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import site.kason.intellij.KalangLanguage;

/**
 * @author KasonYang
 */
public class KalangTokenType extends IElementType {
    public KalangTokenType(@NotNull String debugName) {
        super(debugName, KalangLanguage.INSTANCE);
    }
}
