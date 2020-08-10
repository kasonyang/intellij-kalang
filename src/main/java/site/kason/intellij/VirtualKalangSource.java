package site.kason.intellij;

import com.intellij.openapi.vfs.VirtualFile;
import kalang.compiler.compile.KalangSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author KasonYang
 */
public class VirtualKalangSource implements KalangSource {

    private final VirtualFile virtualFile;

    private final String className;

    private final boolean isScript;

    public VirtualKalangSource(String className, boolean script, VirtualFile virtualFile) {
        this.className = className;
        this.virtualFile = virtualFile;
        this.isScript = script;
    }


    public long getModificationStamp() {
        return virtualFile.getModificationStamp();
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public InputStream createInputStream() throws IOException {
        return new ByteArrayInputStream(virtualFile.contentsToByteArray());
    }

    @Override
    public String getFileName() {
        return virtualFile.getCanonicalPath();
    }

    @Override
    public boolean isScript() {
        return isScript;
    }

}
