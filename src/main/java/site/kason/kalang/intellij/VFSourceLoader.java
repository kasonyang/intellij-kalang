package site.kason.kalang.intellij;

import com.intellij.openapi.vfs.VirtualFile;
import kalang.compiler.compile.KalangSource;
import kalang.compiler.compile.SourceLoader;
import kalang.compiler.util.KalangSourceUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author KasonYang
 */
public class VFSourceLoader implements SourceLoader {
    private final List<VirtualFile> srcDirs = new ArrayList<>();

    private final List<String> extensions = new ArrayList<>();

    public VFSourceLoader(Collection<VirtualFile> srcDirs, String[] extensions) {
        this.srcDirs.addAll(srcDirs);
        this.extensions.addAll(Arrays.asList(extensions));
    }

    @Override
    public KalangSource loadSource(@NotNull String className) {
        for (String e : extensions) {
            String fn = className.replace(".", "/") + "." + e;
            for (VirtualFile s : srcDirs) {
                @Nullable VirtualFile vf = s.findFileByRelativePath(fn);
                if (vf != null) {
                    boolean script = KalangSourceUtil.isScriptFile(vf.getName());
                    return new VirtualKalangSource(className, script, vf);
                }
            }
        }
        return null;
    }
}
