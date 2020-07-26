package site.kason.intellij;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import kalang.compiler.compile.CompilePhase;
import kalang.compiler.compile.Diagnosis;
import kalang.compiler.compile.OffsetRange;
import kalang.compiler.compile.StandardCompilePhases;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import site.kason.intellij.util.IdeaClassNameUtil;
import site.kason.kalang.sdk.compiler.ExtendKalangCompiler;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author KasonYang
 */
public class KalangExternalAnnotator extends ExternalAnnotator<PsiFile, List<Diagnosis>> {

    @Nullable
    @Override
    public PsiFile collectInformation(@NotNull PsiFile file) {
        return file;
    }

    @Nullable
    @Override
    public List<Diagnosis> doAnnotate(PsiFile collectedInfo) {
        String text = collectedInfo.getText();
        VirtualFile vFile = collectedInfo.getVirtualFile();
        @NotNull Project project = collectedInfo.getProject();
        String className = IdeaClassNameUtil.getClassName(project, vFile);
        ExtendKalangCompiler compiler = CompilerManager.create(project, vFile);
        List<Diagnosis> diagnosisList = new LinkedList<>();
        compiler.setDiagnosisHandler(d -> {
            if (Objects.equals(d.getSource().getClassName(), className)) {
                diagnosisList.add(d);
            }
        });
        compiler.setCompileTargetPhase(StandardCompilePhases.ANALYZE_SEMANTIC);
        compiler.forceCompile(className, text, vFile.getPath());
        return diagnosisList;
    }

    @Override
    public void apply(@NotNull PsiFile file, List<Diagnosis> annotationResult, @NotNull AnnotationHolder holder) {
        for (Diagnosis d : annotationResult) {
            Diagnosis.Kind kind = d.getKind();
            OffsetRange offset = d.getOffset();
            TextRange textRange = TextRange.EMPTY_RANGE;
            String msg = d.getDescription();
            if (offset != null && offset.startOffset >= 0) {
                textRange = new TextRange(offset.startOffset, offset.stopOffset + 1);
            }
            switch (kind) {
                case NOTE:
                    holder.createInfoAnnotation(textRange, msg);
                    break;
                case WARNING:
                    holder.createWarningAnnotation(textRange, msg);
                    break;
                case ERROR:
                case FATAL:
                    holder.createErrorAnnotation(textRange, msg);
                    break;
            }
        }
    }



}
