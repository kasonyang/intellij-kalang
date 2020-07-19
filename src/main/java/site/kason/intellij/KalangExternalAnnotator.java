package site.kason.intellij;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import kalang.compiler.compile.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import site.kason.kalang.sdk.compiler.ExtendKalangCompiler;

import java.util.LinkedList;
import java.util.List;

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
        //TODO fix className
        String fileName = collectedInfo.getVirtualFile().getName();
        String className = collectedInfo.getName();
        ExtendKalangCompiler compiler = new ExtendKalangCompiler();
        List<Diagnosis> diagnosisList = new LinkedList<>();
        compiler.setDiagnosisHandler(diagnosisList::add);
        compiler.addSource(className, text, fileName);
        compiler.compile(CompilePhase.PHASE_SEMANTIC);
        return diagnosisList;
    }

    @Override
    public void apply(@NotNull PsiFile file, List<Diagnosis> annotationResult, @NotNull AnnotationHolder holder) {
        for (Diagnosis d : annotationResult) {
            Diagnosis.Kind kind = d.getKind();
            OffsetRange offset = d.getOffset();
            TextRange textRange = TextRange.EMPTY_RANGE;
            String msg = d.getDescription();
            if (offset != null) {
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
