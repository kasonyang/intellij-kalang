package site.kason.kalang.intellij.psi;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import site.kason.kalang.intellij.KalangLanguage;

/**
 * @author KasonYang
 */
public class KalangElementType extends IElementType {
    public KalangElementType(@NotNull String debugName) {
        super(debugName, KalangLanguage.INSTANCE);
    }
}
