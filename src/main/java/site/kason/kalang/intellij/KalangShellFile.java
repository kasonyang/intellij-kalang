package site.kason.kalang.intellij;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

/**
 * @author KasonYang
 */
public class KalangShellFile extends PsiFileBase {

    public KalangShellFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, KalangLanguage.INSTANCE);
    }

    @Override
    public @NotNull FileType getFileType() {
        return KalangShellFileType.INSTANCE;
    }
    
}

