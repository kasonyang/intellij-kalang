package site.kason.intellij.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.vfs.VirtualFile;
import kalang.compiler.shell.KalangOption;
import kalang.mixin.CollectionMixin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.Arrays;

/**
 * @author KasonYang
 */
public class SyncKalangOptionsAction extends AnAction {


    private final static String OPTIONS_FILE = "kalang.options";

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        @Nullable Project project = e.getProject();
        if (project == null) {
            return;
        }
        updateProject(project);
    }


    private void updateProject(Project project) {
        VirtualFile[] roots = ProjectRootManager.getInstance(project).getContentRoots();
        System.out.println("project roots:" + Arrays.toString(roots));
        for (VirtualFile root : roots) {
            VirtualFile optionsFile = root.findChild(OPTIONS_FILE);
            if (optionsFile != null) {
                loadOptions(project, optionsFile);
                return;
            }
        }
    }

    private void loadOptions(Project project, VirtualFile optionsFile) {
        try {
            InputStream is = optionsFile.getInputStream();
            InputStreamReader reader = new InputStreamReader(is);
            KalangOption option = new KalangOption();
            option.parse(new StringReader(""), reader, true);
            String[] classPaths = CollectionMixin.map(option.getClassPaths(), String.class, this::formatJarUrl);
            ApplicationManager.getApplication().runWriteAction(() -> updateDependencies(project, classPaths));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void updateDependencies(Project project, String[] classPaths) {
        //update app libraries
        @NotNull LibraryTable appLibTable = LibraryTablesRegistrar.getInstance().getLibraryTable(project);
        for (Library lib : appLibTable.getLibraries()) {
            appLibTable.removeLibrary(lib);
        }
        @NotNull Library lib = appLibTable.createLibrary("UserLibrary");
        Library.ModifiableModel libModel = lib.getModifiableModel();
        for (String cp : classPaths) {
            libModel.addRoot(cp, OrderRootType.CLASSES);
        }
        libModel.commit();

        //update module dependencies
        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module m : modules) {
            @NotNull ModifiableRootModel moduleModel = ModuleRootManager.getInstance(m).getModifiableModel();
            for (OrderEntry oe : moduleModel.getOrderEntries()) {
                if (oe instanceof LibraryOrderEntry) {
                    moduleModel.removeOrderEntry(oe);
                }
            }
            @NotNull LibraryOrderEntry libEntry = moduleModel.addLibraryEntry(lib);
            libEntry.setExported(false);
            libEntry.setScope(DependencyScope.COMPILE);
            moduleModel.commit();
        }
    }

    private String formatJarUrl(URL url) {
        String cpStr = url.toString();
        if (cpStr.endsWith(".jar")) {
            String prefix = "file:/";
            if (cpStr.startsWith(prefix)) {
                cpStr = cpStr.substring(prefix.length());
            }
            cpStr = "jar://" + cpStr + "!/";
        }
        return cpStr;
    }

}
