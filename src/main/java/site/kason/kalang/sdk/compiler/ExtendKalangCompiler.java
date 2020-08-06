package site.kason.kalang.sdk.compiler;

import kalang.compiler.antlr.KalangParser;
import kalang.compiler.ast.AstNode;
import kalang.compiler.ast.ErrorousExpr;
import kalang.compiler.ast.ExprStmt;
import kalang.compiler.compile.*;
import kalang.compiler.compile.codegen.Ast2JavaStub;
import kalang.compiler.compile.semantic.AstBuilder;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author KasonYang
 */
public class ExtendKalangCompiler extends KalangCompiler {

    public Map<ParseTree, AstNode> parseTreeAstNodeMap = new HashMap<>();

    private final CacheHolder<String, Pair<KalangSource,CompilationUnit>> compilationCacheHolder;

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
        addSource(className, source, fileName, script);
        compile();
    }

    @Override
    public AstBuilder createAstBuilder(CompilationUnit compilationUnit, KalangParser parser) {
        return new AstBuilder(compilationUnit, parser) {

            @Override
            public Object visit(ParseTree tree) {
                Object result = super.visit(tree);
                if (result instanceof AstNode) {
                    parseTreeAstNodeMap.put(tree, (AstNode)result);
                }
                return result;
            }

            @Override
            public Object visitErrorousMemberExpr(KalangParser.ErrorousMemberExprContext emec) {
                visit(emec.expression());
                return super.visitErrorousMemberExpr(emec);
            }

            @Override
            public Object visitErrorousStat(KalangParser.ErrorousStatContext esc) {
                super.visitErrorousStat(esc);
                Object ast = visit(esc.expression());
                if (ast instanceof AstNode) {
                    return new ExprStmt(new ErrorousExpr((AstNode) ast));
                } else {
                    return null;
                }
            }

        };
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

}
