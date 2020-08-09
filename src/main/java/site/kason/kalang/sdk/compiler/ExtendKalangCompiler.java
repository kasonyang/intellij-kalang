package site.kason.kalang.sdk.compiler;

import kalang.compiler.antlr.KalangParser;
import kalang.compiler.ast.AstNode;
import kalang.compiler.ast.ExprNode;
import kalang.compiler.ast.LocalVarNode;
import kalang.compiler.ast.Statement;
import kalang.compiler.compile.*;
import kalang.compiler.compile.codegen.Ast2JavaStub;
import kalang.compiler.compile.semantic.AstBuilder;
import kalang.compiler.core.VarTable;
import kalang.compiler.profile.Profiler;
import kalang.compiler.profile.Span;
import kalang.compiler.profile.SpanFormatter;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.PrintStream;
import java.util.*;

/**
 * @author KasonYang
 */
public class ExtendKalangCompiler extends KalangCompiler {

    private final CacheHolder<String, Pair<KalangSource,CompilationUnit>> compilationCacheHolder;

    private final Set<String> validCompilationUnits = new HashSet<>();

    private boolean enableProfileOutput = false;

    public CompletionInfo completionInfo = new CompletionInfo();

    public ExtendKalangCompiler(
            Configuration configuration,
            CacheHolder<String, Pair<KalangSource,CompilationUnit>> compilationCacheHolder
    ) {
        super(configuration);
        this.compilationCacheHolder = compilationCacheHolder;
    }

    public void forceCompile(String className, String source, String fileName) {
        forceCompile(className, source, fileName, fileName != null && fileName.endsWith(".kls"));
    }

    public void forceCompile(String className, String source, String fileName, boolean script) {
        compilationCacheHolder.remove(className);
        validCompilationUnits.clear();
        completionInfo = new CompletionInfo();
        Profiler.getInstance().startProfile();
        addSource(className, source, fileName, script);
        compile();
        Profiler.getInstance().stopProfile();
        outputProfileInfo();
    }

    @Override
    public AstBuilder createAstBuilder(CompilationUnit compilationUnit, KalangParser parser) {
        return new AstBuilder(compilationUnit, parser) {

            @Override
            public Object visit(ParseTree tree) {
                Object result = super.visit(tree);
                if (result instanceof AstNode) {
                    completionInfo.tree2astMap.put(tree, (AstNode)result);
                }
                return result;
            }

            @Override
            public ExprNode visitMethodRefExpr(KalangParser.MethodRefExprContext ctx) {
                //visit for method reference completion
                visit(ctx.expression());
                return super.visitMethodRefExpr(ctx);
            }

            @Nonnull
            @Override
            public Statement visitStat(KalangParser.StatContext ctx) {
                completionInfo.stat2thisTypeMap.put(ctx, getThisType());
                Map<String, LocalVarNode> varsMap = new HashMap<>();
                collectVars(varsMap, methodCtx.varTables);
                completionInfo.stat2VarsMap.put(ctx, varsMap.values());
                completionInfo.stat2methodMap.put(ctx, methodCtx.method);
                return super.visitStat(ctx);
            }

            private void collectVars(Map<String, LocalVarNode> result, VarTable<String, LocalVarNode> varTable) {
                Collection<LocalVarNode> vars = varTable.values();
                for (LocalVarNode v : vars) {
                    String name = v.getName();
                    if (result.containsKey(name)) {
                        continue;
                    }
                    result.put(name, v);
                }
                VarTable<String, LocalVarNode> parent = varTable.getParent();
                if (parent != null) {
                    collectVars(result, parent);
                }
            }

        };
    }

    @Nullable
    @Override
    protected CompilationUnitController loadCompilationUnitController(String className) {
        CompilationUnitController cuc = super.loadCompilationUnitController(className);
        if (cuc == null || validCompilationUnits.contains(className)) {
            return cuc;
        }
        KalangSource src = getSourceLoader().loadSource(className);
        if (src == null) {
            //source is deleted
            removeCompilationUnitController(className);
            return null;
        }
        KalangSource compiledSrc = cuc.getCompilationUnit().getSource();
        if (!Objects.equals(compiledSrc.getText(), src.getText())) {
            //source is changed
            removeCompilationUnitController(className);
            validCompilationUnits.add(className);
            return super.loadCompilationUnitController(className);
        }
        validCompilationUnits.add(className);
        return cuc;
    }

    @Override
    protected CompilationUnit newCompilationUnit(KalangSource source) {
        String fileName = source.getFileName();
        //only cache sources with file name
        if (fileName == null || fileName.isEmpty()) {
            return super.newCompilationUnit(source);
        }
        String key = source.getClassName();
        Pair<KalangSource, CompilationUnit> cache = compilationCacheHolder.get(key);
        if (cache == null || !Objects.equals(source.getText(), cache.getKey().getText())) {
            CompilationUnit cu = super.newCompilationUnit(source);
            cache = Pair.of(source, cu);
            compilationCacheHolder.put(key, cache);
        }
        return cache.getRight();
    }

    @Override
    public CodeGenerator createCodeGenerator(CompilationUnit compilationUnit) {
        return new Ast2JavaStub(compilationUnit);
    }

    private void outputProfileInfo() {
        Span rootSpan = Profiler.getInstance().getRootSpan();
        if (rootSpan == null) {
            return;
        }
        if (enableProfileOutput) {
            PrintStream os = new PrintStream(System.err);
            new SpanFormatter().format(rootSpan,os);
        }
        long time = rootSpan.getStopTime() - rootSpan.getStartTime();
        System.err.println("compiled in " + time + "ms");
    }

}
