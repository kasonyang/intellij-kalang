package site.kason.intellij;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import kalang.compiler.compile.CompilationUnit;
import kalang.compiler.compile.Configuration;
import kalang.compiler.compile.KalangSource;
import kalang.compiler.compile.jvm.JvmAstLoader;
import kalang.compiler.tool.FileSystemSourceLoader;
import kalang.mixin.CollectionMixin;
import org.apache.commons.lang3.tuple.Pair;
import site.kason.kalang.sdk.compiler.CacheHolder;
import site.kason.kalang.sdk.compiler.ExtendKalangCompiler;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * @author KasonYang
 */
public class CompilerManager {

    private final static String[] EXTENSIONS = new String[] {"kl","kalang"};

    private final static CacheHolder<String, Pair<KalangSource, CompilationUnit>> COMPILATION_UNIT_CACHE_HOLDER = new CacheHolder<>(100);

    private final static Map<Project, Pair<String, JvmAstLoader>> astLoaderMap = new WeakHashMap<>();

    public static ExtendKalangCompiler create(Project project, VirtualFile virtualFile) {
        Module module = ModuleUtil.findModuleForFile(virtualFile, project);
        Objects.requireNonNull(module);
        List<String> libUrls = new LinkedList<>();
        ModuleRootManager.getInstance(module).orderEntries().forEachLibrary(lib -> {
            libUrls.addAll(Arrays.asList(lib.getUrls(OrderRootType.CLASSES)));
            return true;
        });
        VirtualFile[] srcRoots = ModuleRootManager.getInstance(module).getSourceRoots();
        File[] srcDirs = CollectionMixin.map(srcRoots, File.class, VfsUtil::virtualToIoFile);
        Pair<String, JvmAstLoader> astLoaderPair = astLoaderMap.get(project);
        String libUrlsStr = libUrls.toString();
        if (astLoaderPair == null || !Objects.equals(astLoaderPair.getLeft(), libUrlsStr)) {
            URLClassLoader urlClassLoader = new URLClassLoader(string2url(libUrls).toArray(new URL[0]));
            JvmAstLoader astLoader = new JvmAstLoader(null, urlClassLoader);
            astLoaderPair = Pair.of(libUrlsStr, astLoader);
            astLoaderMap.put(project, astLoaderPair);
        }
        Configuration config = new Configuration();
        config.setAstLoader(astLoaderPair.getRight());
        ExtendKalangCompiler compiler = new ExtendKalangCompiler(config, COMPILATION_UNIT_CACHE_HOLDER);
        compiler.setSourceLoader(new FileSystemSourceLoader(srcDirs, EXTENSIONS, "utf8"));
        return compiler;
    }

    private static List<URL> string2url(List<String> path) {
        List<URL> result = new ArrayList<>(path.size());
        for (String p : path) {
            String prefix = "jar://";
            String suffix = "!/";
            int start = p.startsWith(prefix) ? prefix.length() : 0;
            int end = p.endsWith(suffix) ? p.length() - suffix.length() : p.length();
            try {
                result.add(new File(p.substring(start, end)).toURI().toURL());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

}
