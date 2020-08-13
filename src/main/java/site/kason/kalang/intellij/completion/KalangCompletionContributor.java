package site.kason.kalang.intellij.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ProcessingContext;
import kalang.compiler.antlr.KalangLexer;
import kalang.compiler.ast.VarObject;
import kalang.compiler.core.FieldDescriptor;
import kalang.compiler.core.MethodDescriptor;
import kalang.compiler.core.ParameterDescriptor;
import kalang.mixin.CollectionMixin;
import org.antlr.intellij.adaptor.lexer.TokenIElementType;
import org.jetbrains.annotations.NotNull;
import site.kason.kalang.intellij.CompilerManager;
import site.kason.kalang.intellij.KalangTokenTypes;
import site.kason.kalang.intellij.util.IdeaClassNameUtil;
import site.kason.kalang.sdk.compiler.ExtendKalangCompiler;
import site.kason.kalang.sdk.compiler.complete.*;

import java.util.List;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 * @author KasonYang
 */
public class KalangCompletionContributor extends CompletionContributor {

    private final static InsertHandler<LookupElement> METHOD_INSERT_HANDLER = new MethodInsertHandler();

    public KalangCompletionContributor() {
        TokenIElementType idToken = KalangTokenTypes.TOKEN_ELEMENT_TYPES.get(KalangLexer.Identifier);
        extend(
                CompletionType.BASIC,
                StandardPatterns.or(
                        psiElement(idToken),
                        psiElement().afterLeaf(".","..","::")
                ),
                new BasicCompletionProvider()
        );
        extend(
                CompletionType.BASIC,
                psiElement().afterLeaf("new"),
                new ClassNameCompletionProvider(new ClassNameInsertHandler())
        );
        extend(CompletionType.BASIC, psiElement().afterLeaf("import"), new ClassNameCompletionProvider(
                new ImportClassInsertHandler()
        ));
    }

    private static class ClassNameCompletionProvider extends CompletionProvider<CompletionParameters> {

        private final InsertHandler<JavaPsiClassReferenceElement> insertHandler;

        private ClassNameCompletionProvider(InsertHandler<JavaPsiClassReferenceElement> insertHandler) {
            this.insertHandler = insertHandler;
        }


        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
            @NotNull Project project = parameters.getOriginalFile().getProject();
            AllClassesGetter.processJavaClasses(result.getPrefixMatcher(), project, GlobalSearchScope.allScope(project), psiClass -> {
                result.addElement(AllClassesGetter.createLookupItem(psiClass, insertHandler));
                return true;
            });
        }

    }

    private static class BasicCompletionProvider extends CompletionProvider<CompletionParameters> {

        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
            @NotNull Project project = parameters.getOriginalFile().getProject();
            VirtualFile file = parameters.getOriginalFile().getVirtualFile();
            String text = parameters.getOriginalFile().getText();
            String className = IdeaClassNameUtil.getClassName(project, file);
            boolean script = file.getName().endsWith(".kls");
            ExtendKalangCompiler compiler = CompilerManager.create(project, file);
            KalangCompleter completer = new KalangCompleter(compiler);
            List<Completion> list = completer.complete(className, text, script, parameters.getOffset());
            processCompletionResult(list, result);
        }

        protected void processCompletionResult(List<Completion> list, CompletionResultSet resultSet) {
            for (Completion it : list) {
                if (it instanceof MethodCompletion) {
                    MethodDescriptor method = ((MethodCompletion) it).getMethod();
                    LookupElementBuilder ele = LookupElementBuilder.create(method, method.getName())
                            .withTypeText(method.getReturnType().getName(true))
                            .withTailText(formatMethodParams(method))
                            .withInsertHandler(METHOD_INSERT_HANDLER)
                            ;
                    resultSet.addElement(ele);
                } else if (it instanceof FieldCompletion) {
                    FieldDescriptor field = ((FieldCompletion) it).getField();
                    LookupElementBuilder ele = LookupElementBuilder.create(field.getName())
                            .withTypeText(field.getType().getName(true));
                    resultSet.addElement(ele);
                } else if (it instanceof VarCompletion) {
                    VarObject varObj = ((VarCompletion) it).getVarObject();
                    LookupElementBuilder ele = LookupElementBuilder.create(varObj.getName())
                            .withTypeText(varObj.getType().getName(true));
                    resultSet.addElement(ele);
                } else {
                    LookupElementBuilder ele = LookupElementBuilder.create(it.getCompleteString());
                    resultSet.addElement(ele);
                }
            }
        }

        private String formatMethodParams(MethodDescriptor methodNode) {
            StringBuilder sb = new StringBuilder();
            ParameterDescriptor[] params = methodNode.getParameterDescriptors();
            sb.append('(');
            if (params.length > 0) {
                for (ParameterDescriptor p : params) {
                    sb.append(p.getType().getName(true))
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

    private static class ImportClassInsertHandler implements InsertHandler<JavaPsiClassReferenceElement> {

        @Override
        public void handleInsert(@NotNull InsertionContext context, @NotNull JavaPsiClassReferenceElement item) {
            @NotNull Editor editor = context.getEditor();
            @NotNull Document doc = editor.getDocument();
            @NotNull CaretModel cm = editor.getCaretModel();
            String qName = item.getQualifiedName();
            int startOffset = context.getStartOffset();
            int tailOffset = context.getTailOffset();
            doc.replaceString(startOffset, tailOffset, qName);
        }

    }

    private static class ClassNameInsertHandler implements InsertHandler<JavaPsiClassReferenceElement> {

        @Override
        public void handleInsert(@NotNull InsertionContext ctx, @NotNull JavaPsiClassReferenceElement item) {
            @NotNull Editor editor = ctx.getEditor();
            @NotNull Document doc = editor.getDocument();
            @NotNull CaretModel cm = editor.getCaretModel();
            PsiMethod[] constructors = item.getObject().getConstructors();
            boolean hasParams = CollectionMixin.find(
                    constructors,
                    c -> !c.getParameterList().isEmpty()
            ) != null;
            doc.insertString(ctx.getTailOffset(),"()");
            cm.moveCaretRelatively(hasParams ? 1 : 2, 0, false, false,true);
        }
    }

    private static class MethodInsertHandler implements InsertHandler<LookupElement> {

        @Override
        public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement item) {
            @NotNull Editor editor = context.getEditor();
            @NotNull Document doc = editor.getDocument();
            @NotNull CaretModel cm = editor.getCaretModel();
            MethodDescriptor md = (MethodDescriptor) item.getObject();
            int paramsCount = md.getParameterDescriptors().length;
            doc.insertString(context.getTailOffset(),"()");
            cm.moveCaretRelatively(paramsCount > 0 ? 1 : 2, 0, false, false,true);
        }

    }

}
