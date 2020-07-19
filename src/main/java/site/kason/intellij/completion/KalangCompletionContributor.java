package site.kason.intellij.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ProcessingContext;
import kalang.compiler.compile.CompilationUnit;
import kalang.compiler.compile.CompilePhase;
import kalang.compiler.core.FieldDescriptor;
import kalang.compiler.core.MethodDescriptor;
import kalang.compiler.core.ParameterDescriptor;
import kalang.compiler.util.NameUtil;
import org.jetbrains.annotations.NotNull;
import site.kason.kalang.sdk.compiler.ExtendKalangCompiler;
import site.kason.kalang.sdk.compiler.complete.Completion;
import site.kason.kalang.sdk.compiler.complete.FieldCompletion;
import site.kason.kalang.sdk.compiler.complete.KalangCompleter;
import site.kason.kalang.sdk.compiler.complete.MethodCompletion;

import java.util.List;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 * @author KasonYang
 */
public class KalangCompletionContributor extends CompletionContributor {

    public KalangCompletionContributor() {
        extend(CompletionType.BASIC, psiElement().afterLeaf(psiElement().withText(".")), new MemberCompletionProvider());
    }

    private static class MemberCompletionProvider extends CompletionProvider<CompletionParameters> {

        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
            VirtualFile file = parameters.getOriginalFile().getVirtualFile();
            String text = parameters.getOriginalFile().getText();
            String fileName = parameters.getOriginalFile().getName();
            //TODO fix className
            String className = file.getNameWithoutExtension();
            ExtendKalangCompiler compiler = new ExtendKalangCompiler();
            compiler.addSource(className, text, fileName);
            compiler.compile(CompilePhase.PHASE_BUILDAST);
            CompilationUnit cu = compiler.getCompilationUnit(className);
            KalangCompleter completer = new KalangCompleter(compiler.parseTreeAstNodeMap, cu);
            List<Completion> list = completer.complete(parameters.getOffset());
            processCompletionResult(list, result);
        }

        private void processCompletionResult(List<Completion> list, CompletionResultSet resultSet) {
            for (Completion it : list) {
                if (it instanceof MethodCompletion) {
                    MethodDescriptor method = ((MethodCompletion) it).getMethod();
                    LookupElementBuilder ele = LookupElementBuilder.create(method.getName())
                            .withTailText(formatMethodParams(method));
                    resultSet.addElement(ele);
                } else if (it instanceof FieldCompletion) {
                    FieldDescriptor field = ((FieldCompletion) it).getField();
                    LookupElementBuilder ele = LookupElementBuilder.create(field.getName())
                            .withTailText(NameUtil.getSimpleClassName(field.getName()));
                    resultSet.addElement(ele);
                }
            }
        }

        private String formatMethodParams(MethodDescriptor methodNode) {
            StringBuilder sb = new StringBuilder();
            ParameterDescriptor[] parms = methodNode.getParameterDescriptors();
            sb.append('(');
            if (parms.length > 0) {
                for (ParameterDescriptor p : parms) {
                    sb.append(NameUtil.getSimpleClassName(p.getType().getName()))
                            .append(' ')
                            .append(p.getName())
                            .append(',');
                }
                sb.setLength(sb.length() - 1);
            }
            sb.append(')');
            return sb.toString();
        }

    }



}
