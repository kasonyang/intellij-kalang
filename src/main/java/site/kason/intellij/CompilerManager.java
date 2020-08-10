package site.kason.intellij;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.vfs.VirtualFile;
import kalang.compiler.compile.CompilationUnit;
import kalang.compiler.compile.Configuration;
import kalang.compiler.compile.KalangSource;
import kalang.compiler.compile.jvm.JvmClassNodeLoader;
import kalang.compiler.tool.CachedClassNodeLoader;
import kalang.compiler.tool.CachedSourceLoader;
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

    private final static CacheHolder<String, Pair<KalangSource, CompilationUnit>> COMPILATION_UNIT_CACHE_HOLDER = new CacheHolder<>(100);

    private final static Map<Module, Pair<String, ExtendKalangCompiler>> cachedCompilers = new WeakHashMap<>();

    public static ExtendKalangCompiler create(Project project, VirtualFile virtualFile) {
        Module moduleOfFile = ModuleUtil.findModuleForFile(virtualFile, project);
        Objects.requireNonNull(moduleOfFile);
        Set<Module> modules = getModulesWithDeps(moduleOfFile);
        List<String> libUrls = new LinkedList<>();
        List<VirtualFile> srcDirs = new LinkedList<>();
        for (Module m : modules) {
            ModuleRootManager.getInstance(m).orderEntries().forEachLibrary(lib -> {
                libUrls.addAll(Arrays.asList(lib.getUrls(OrderRootType.CLASSES)));
                return true;
            });
            VirtualFile[] srcRoots = ModuleRootManager.getInstance(m).getSourceRoots();
            srcDirs.addAll(Arrays.asList(srcRoots));
        }
        Pair<String, ExtendKalangCompiler> compilerPair = cachedCompilers.get(moduleOfFile);
        String libUrlsStr = libUrls.toString();
        if (compilerPair == null) {
            System.out.println("compiler cache not found for module:" + moduleOfFile);
        }
        if (compilerPair == null || !Objects.equals(compilerPair.getLeft(), libUrlsStr)) {
            URLClassLoader urlClassLoader = new URLClassLoader(string2url(libUrls).toArray(new URL[0]));
            JvmClassNodeLoader classNodeLoader = new JvmClassNodeLoader(null, urlClassLoader);
            Configuration config = new Configuration();
            config.setClassNodeLoader(new CachedClassNodeLoader(classNodeLoader));
            ExtendKalangCompiler compiler = new ExtendKalangCompiler(config, COMPILATION_UNIT_CACHE_HOLDER);
            compiler.setSourceLoader(
                new CachedSourceLoader(
                    new VFSourceLoader(srcDirs, KalangSource.EXTENSION_STANDARD)
                )
            );
            compilerPair = Pair.of(libUrlsStr, compiler);
            cachedCompilers.put(moduleOfFile, compilerPair);
        }
        return compilerPair.getRight();
    }

    private static Set<Module> getModulesWithDeps(Module module) {
        Set<Module> modules = new HashSet<>();
        collectModuleWithDeps(module, modules);
        return modules;
    }

    private static void collectModuleWithDeps(Module module, Set<Module> moduleHolder) {
        boolean added = moduleHolder.add(module);
        if (added) {
            Module[] dependencies = ModuleRootManager.getInstance(module).getDependencies();
            moduleHolder.addAll(Arrays.asList(dependencies));
        }
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
