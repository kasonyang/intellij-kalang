package site.kason.kalang.intellij;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author KasonYang
 */
public class KalangFileType extends LanguageFileType {

    public static final KalangFileType INSTANCE = new KalangFileType();

    public KalangFileType() {
        super(KalangLanguage.INSTANCE);
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
        return "kl";
    }

    @Override
    public @Nullable Icon getIcon() {
        return AllIcons.Nodes.Class;
    }
}
