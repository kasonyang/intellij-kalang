package site.kason.kalang.sdk.compiler;

import kalang.compiler.antlr.KalangParser;
import kalang.compiler.ast.AstNode;
import kalang.compiler.ast.LocalVarNode;
import kalang.compiler.ast.MethodNode;
import kalang.compiler.core.ObjectType;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author KasonYang
 */
public class CompletionInfo {

    public Map<ParseTree, AstNode> tree2astMap = new HashMap<>();

    public Map<KalangParser.StatContext, ObjectType> stat2thisTypeMap = new HashMap<>();

    public Map<KalangParser.StatContext, Collection<LocalVarNode>> stat2VarsMap = new HashMap<>();

    public Map<KalangParser.StatContext, MethodNode> stat2methodMap = new HashMap<>();


}
