package site.kason.kalang.intellij;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author KasonYang
 */
public class KalangFileType extends LanguageFileType {

    public static final KalangFileType INSTANCE = new KalangFileType("kl");

    public static final KalangFileType INSTANCE_KALANG = new KalangFileType("kalang");

    private final String extension;

    public KalangFileType(String extension) {
        super(KalangLanguage.INSTANCE);
        this.extension = extension;
    }

    @Override
    public @NotNull String getName() {
        return "Kalang File";
    }

    @Override
    public @NotNull String getDescription() {
        return "Kalang file";
    }

    @Override
    public @NotNull String getDefaultExtension() {
        return extension;
    }

    @Override
    public @Nullable Icon getIcon() {
        return KalangIcons.FILE;
    }
}
