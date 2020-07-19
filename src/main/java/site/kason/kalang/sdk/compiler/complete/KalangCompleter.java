package site.kason.kalang.sdk.compiler.complete;

import kalang.compiler.antlr.KalangLexer;
import kalang.compiler.antlr.KalangParser.ExpressionContext;
import kalang.compiler.ast.AstNode;
import kalang.compiler.ast.ClassNode;
import kalang.compiler.ast.ClassReference;
import kalang.compiler.ast.ExprNode;
import kalang.compiler.compile.CompilationUnit;
import kalang.compiler.core.*;
import kalang.compiler.util.AstUtil;
import kalang.compiler.util.ModifierUtil;
import kalang.compiler.util.TokenNavigator;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import site.kason.kalang.sdk.compiler.ParseTreeNavigator;
import site.kason.kalang.sdk.compiler.util.NavigatorUtil;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @author KasonYang
 */
public class KalangCompleter {

    //TODO move to compilation unit
    private Map<ParseTree, AstNode> parseTreeAstNodeMap;
    private CompilationUnit compilationUnit;

    public KalangCompleter(Map<ParseTree, AstNode> parseTreeAstNodeMap, CompilationUnit compilationUnit) {
        this.parseTreeAstNodeMap = parseTreeAstNodeMap;
        this.compilationUnit = compilationUnit;
    }

    public List<Completion> complete(int caret) {
        TokenNavigator tokenNav = NavigatorUtil.createTokenNavigator(compilationUnit);
        try {
            tokenNav.move(caret - 1);
        } catch (IndexOutOfBoundsException ex) {
            return null;
        }
        Token currentToken = tokenNav.getCurrentToken();
        if (!tokenNav.hasPrevious()) {
            return null;
        }
        tokenNav.previous(0);
        Token prevToken = tokenNav.getCurrentToken();
        if (isDotToken(currentToken)) {
            return completeMember(prevToken, caret);
        } else if (isIdentifier(currentToken) && isDotToken(prevToken)) {
            if (!tokenNav.hasPrevious()) {
                return null;
            }
            tokenNav.previous(0);
            Token prevPrevToken = tokenNav.getCurrentToken();
            return completeMember(prevPrevToken, currentToken.getStartIndex());
        } else if (isDotDotToken(currentToken)) {
            return completeMixinMethod(prevToken, caret);
        }
        return null;
    }


    private List<Completion> completeMember(Token targetExprToken, int anchorOffset) {
        ParseTreeNavigator parseTreeNav = NavigatorUtil.createParseTreeNavigator(compilationUnit);
        ExpressionContext prevCtx = parseTreeNav.move(targetExprToken.getStopIndex(), ExpressionContext.class);
        AstNode node = parseTreeAstNodeMap.get(prevCtx);
        if (node instanceof ExprNode) {
            Type targetType = ((ExprNode) node).getType();
            if (!(targetType instanceof ObjectType)) {
                return null;
            }
            ObjectType targetObjType = (ObjectType) targetType;
            return completeMembersForType(targetObjType, anchorOffset, false);
        } else if (node instanceof ClassReference) {
            ClassReference cr = (ClassReference) node;
            ClassNode clsNode = cr.getReferencedClassNode();
            ClassType clsType = Types.getClassType(clsNode);
            return completeMembersForType(clsType, anchorOffset, true);
        }
        return Collections.emptyList();
    }

    private List<Completion> completeMembersForType(ObjectType objectType , int anchorOffset, @Nullable Boolean staticMember) {
        List<Completion> list = new LinkedList<Completion>();
        FieldDescriptor[] fs = objectType.getFieldDescriptors(compilationUnit.getAst());
        if (fs != null) {
            for (FieldDescriptor f : fs) {
                if (staticMember != null && staticMember != AstUtil.isStatic(f.getModifier())) {
                    continue;
                }
                list.add(new FieldCompletion(f, anchorOffset));
            }
        }
        MethodDescriptor[] ms = objectType.getMethodDescriptors(compilationUnit.getAst(), true, true);
        if (ms != null) {
            for (MethodDescriptor m : ms) {
                String name = m.getName();
                if (name.startsWith("<")) {
                    continue;
                }
                if (staticMember != null && staticMember != AstUtil.isStatic(m.getModifier())) {
                    continue;
                }
                list.add(new MethodCompletion(anchorOffset, m));
            }
        }
        return list;
    }

    private List<Completion> completeMixinMethod(Token targetExprToken, int anchorOffset) {
        ParseTreeNavigator parseTreeNav = NavigatorUtil.createParseTreeNavigator(compilationUnit);
        ExpressionContext prevCtx = parseTreeNav.move(targetExprToken.getStopIndex(), ExpressionContext.class);
        AstNode node = parseTreeAstNodeMap.get(prevCtx);
        if (node instanceof ExprNode) {
            Type targetType = ((ExprNode) node).getType();
            return completeMixinMethodForType(targetType, anchorOffset);
        } else if (node instanceof ClassReference) {
            ClassReference cr = (ClassReference) node;
            ClassNode clsNode = cr.getReferencedClassNode();
            ClassType clsType = Types.getClassType(clsNode);
            return completeMixinMethodForType(clsType, anchorOffset);
        }
        return null;
    }

    private List<Completion> completeMixinMethodForType(Type type, int anchorOffset) {
        Map<String, CompilationUnit.MemberImport> importedMethods = new HashMap<String, CompilationUnit.MemberImport>();
        List<ClassNode> importedPaths = compilationUnit.importedMixinPaths;
        for (ClassNode cn : importedPaths) {
            ClassType cnType = Types.getClassType(cn);
            MethodDescriptor[] methods = cnType.getMethodDescriptors(null, false, false);
            for (MethodDescriptor m : methods) {
                importedMethods.put(m.getName(), new CompilationUnit.MemberImport(cn, m.getName()));
            }
        }
        importedMethods.putAll(compilationUnit.importedMixinMethods);
        List<Completion> methods = new LinkedList<Completion>();
        for (CompilationUnit.MemberImport mi : importedMethods.values()) {
            ClassType miType = Types.getClassType(mi.classNode);
            MethodDescriptor[] miMethods = miType.getMethodDescriptors(null, mi.member, false, false);
            for (MethodDescriptor m : miMethods) {
                if (!ModifierUtil.isStatic(m.getModifier())) {
                    continue;
                }
                ParameterDescriptor[] params = m.getParameterDescriptors();
                if (params.length < 1) {
                    continue;
                }
                if (!params[0].getType().isAssignableFrom(type)) {
                    continue;
                }
                methods.add(new MethodCompletion(anchorOffset, m));
            }
        }
        return methods;
    }

    private boolean isDotToken(Token token) {
        return token.getType() == KalangLexer.DOT;
    }

    private boolean isDotDotToken(Token token) {
        return token.getType() == KalangLexer.DOTDOT;
    }

    private boolean isIdentifier(Token token) {
        return token.getType() == KalangLexer.Identifier;
    }

}
