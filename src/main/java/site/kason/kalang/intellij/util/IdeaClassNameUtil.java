package site.kason.kalang.intellij.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import kalang.compiler.util.ClassNameUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author KasonYang
 */
public class IdeaClassNameUtil {

    public static String getClassName(Project project, VirtualFile vFile) {
        VirtualFile srcRoot = ProjectRootManager.getInstance(project).getFileIndex().getSourceRootForFile(vFile);
        if (srcRoot == null) {
            srcRoot = vFile.getParent();
        }
        @NotNull File dir = VfsUtil.virtualToIoFile(srcRoot);
        @NotNull File file = VfsUtil.virtualToIoFile(vFile);
        return ClassNameUtil.getClassName(dir, file);
    }

}
