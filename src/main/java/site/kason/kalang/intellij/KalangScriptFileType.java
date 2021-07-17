package site.kason.kalang.intellij;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author KasonYang
 */
public class KalangScriptFileType extends LanguageFileType {

    public static final KalangScriptFileType INSTANCE = new KalangScriptFileType();

    public KalangScriptFileType() {
        super(KalangLanguage.INSTANCE);
    }

    @Override
    public @NotNull String getName() {
        return "Kalang Script File";
    }

    @Override
    public @NotNull String getDescription() {
        return "Kalang Script file";
    }

    @Override
    public @NotNull String getDefaultExtension() {
        return "kls";
    }

    @Override
    public @Nullable Icon getIcon() {
        return AllIcons.Nodes.Console;
    }
}
