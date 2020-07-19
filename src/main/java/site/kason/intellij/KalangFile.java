package site.kason.intellij;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

/**
 * @author KasonYang
 */
public class KalangFile extends PsiFileBase {

    public KalangFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, KalangLanguage.INSTANCE);
    }

    @Override
    public @NotNull FileType getFileType() {
        return KalangFileType.INSTANCE;
    }
}
