package site.kason.kalang.sdk.compiler;

import kalang.compiler.antlr.KalangParser;
import kalang.compiler.ast.AstNode;
import kalang.compiler.ast.ErrorousExpr;
import kalang.compiler.ast.ExprStmt;
import kalang.compiler.compile.CodeGenerator;
import kalang.compiler.compile.CompilationUnit;
import kalang.compiler.compile.Configuration;
import kalang.compiler.compile.KalangCompiler;
import kalang.compiler.compile.codegen.Ast2JavaStub;
import kalang.compiler.compile.semantic.AstBuilder;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.HashMap;
import java.util.Map;

/**
 * @author KasonYang
 */
public class ExtendKalangCompiler extends KalangCompiler {

    public Map<ParseTree, AstNode> parseTreeAstNodeMap = new HashMap<ParseTree, AstNode>();

    public ExtendKalangCompiler() {
    }

    public ExtendKalangCompiler(Configuration configuration) {
        super(configuration);
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
    public CodeGenerator createCodeGenerator(CompilationUnit compilationUnit) {
        return new Ast2JavaStub(compilationUnit);
    }

}
