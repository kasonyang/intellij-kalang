package site.kason.intellij;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author KasonYang
 */
public class KalangShellFileType extends LanguageFileType {

    public static final KalangShellFileType INSTANCE = new KalangShellFileType();

    public KalangShellFileType() {
        super(KalangLanguage.INSTANCE);
    }

    @Override
    public @NotNull String getName() {
        return "Kalang shell File";
    }

    @Override
    public @NotNull String getDescription() {
        return "Kalang shell file";
    }

    @Override
    public @NotNull String getDefaultExtension() {
        return "kls";
    }

    @Override
    public @Nullable Icon getIcon() {
        return KalangIcons.FILE;
    }
}
