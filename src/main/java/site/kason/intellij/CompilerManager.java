package site.kason.intellij;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.vfs.VirtualFile;
import kalang.compiler.compile.Configuration;
import kalang.compiler.compile.jvm.JvmAstLoader;
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

    public static ExtendKalangCompiler create(Project project, VirtualFile virtualFile) {
        Module module = ModuleUtil.findModuleForFile(virtualFile, project);
        Objects.requireNonNull(module);
        List<String> libUrls = new LinkedList<>();
        ModuleRootManager.getInstance(module).orderEntries().forEachLibrary(lib -> {
            libUrls.addAll(Arrays.asList(lib.getUrls(OrderRootType.CLASSES)));
            return true;
        });
        URLClassLoader urlClassLoader = new URLClassLoader(string2url(libUrls).toArray(new URL[0]));
        Configuration config = new Configuration();
        config.setAstLoader(new JvmAstLoader(null, urlClassLoader));
        return new ExtendKalangCompiler(config);
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
