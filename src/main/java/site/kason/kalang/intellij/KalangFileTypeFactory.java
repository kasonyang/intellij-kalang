package site.kason.kalang.intellij;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

/**
 * @author KasonYang
 */
public class KalangFileTypeFactory extends FileTypeFactory {
    @Override
    public void createFileTypes(@NotNull FileTypeConsumer consumer) {
        consumer.consume(KalangFileType.INSTANCE);
        consumer.consume(KalangFileType.INSTANCE_KALANG);
        consumer.consume(KalangScriptFileType.INSTANCE);
    }
}
