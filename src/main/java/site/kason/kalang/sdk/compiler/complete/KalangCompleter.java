package site.kason.kalang.sdk.compiler.complete;

import kalang.compiler.antlr.KalangLexer;
import kalang.compiler.antlr.KalangParser.ExpressionContext;
import kalang.compiler.ast.AstNode;
import kalang.compiler.ast.ClassNode;
import kalang.compiler.ast.ClassReference;
import kalang.compiler.ast.ExprNode;
import kalang.compiler.compile.CompilationUnit;
import kalang.compiler.compile.StandardCompilePhases;
import kalang.compiler.core.*;
import kalang.compiler.util.AstUtil;
import kalang.compiler.util.LexerFactory;
import kalang.compiler.util.ModifierUtil;
import kalang.compiler.util.TokenNavigator;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import site.kason.kalang.sdk.compiler.ExtendKalangCompiler;
import site.kason.kalang.sdk.compiler.ParseTreeNavigator;
import site.kason.kalang.sdk.compiler.util.NavigatorUtil;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @author KasonYang
 */
public class KalangCompleter {

    private ExtendKalangCompiler compiler;

    public KalangCompleter(ExtendKalangCompiler compiler) {
        this.compiler = compiler;
    }

    public List<Completion> complete(String className, String source, boolean script, int caret) {
        KalangLexer lexer = LexerFactory.createLexer(source);
        CommonTokenStream inputStream = new CommonTokenStream(lexer);
        while (inputStream.LA(1) != -1) {
            inputStream.consume();
        }
        TokenNavigator tokenNav = new TokenNavigator(inputStream.getTokens());
        try {
            tokenNav.move(caret - 1);
        } catch (IndexOutOfBoundsException ex) {
            return Collections.emptyList();
        }
        Token currentToken = tokenNav.getCurrentToken();
        if (!tokenNav.hasPrevious()) {
            return Collections.emptyList();
        }
        tokenNav.previous(0);
        Token prevToken = tokenNav.getCurrentToken();
        if (isDotToken(currentToken)) {
            CompilationUnit cu = compile(className, source, script, currentToken.getStartIndex(), currentToken.getStopIndex());
            return completeMember(cu, prevToken.getStopIndex(), caret);
        } else if (isIdentifier(currentToken) && isDotToken(prevToken)) {
            if (!tokenNav.hasPrevious()) {
                return Collections.emptyList();
            }
            tokenNav.previous(0);
            Token prevPrevToken = tokenNav.getCurrentToken();
            CompilationUnit cu = compile(className, source, script, -1, -1);
            return completeMember(cu, prevPrevToken.getStopIndex(), currentToken.getStartIndex());
        } else if (isDotDotToken(currentToken)) {
            CompilationUnit cu = compile(className, source, script, currentToken.getStartIndex(), currentToken.getStopIndex());
            return completeMixinMethod(cu, prevToken.getStopIndex(), caret);
        } else if (isDoubleColon(currentToken)) {
            CompilationUnit cu = compile(className, source, script, currentToken.getStartIndex(), currentToken.getStopIndex());
            return completeMethodRef(cu, prevToken.getStopIndex(), caret);
        }
        return Collections.emptyList();
    }

    private CompilationUnit compile(String className, String source,  boolean script, int deleteBegin, int deleteStop) {
        if (deleteBegin >= 0 && deleteStop >= 0) {
            source = source.substring(0, deleteBegin) + " " + source.substring(deleteStop + 1);
        }
        compiler.setCompileTargetPhase(StandardCompilePhases.PARSE_BODY);
        compiler.setDiagnosisHandler(dh -> {});
        try {
            compiler.forceCompile(className, source, null, script);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return compiler.getCompilationUnit(className);
    }


    private List<Completion> completeMember(CompilationUnit compilationUnit, int exprOffset, int anchorOffset) {
        ParseTreeNavigator parseTreeNav = NavigatorUtil.createParseTreeNavigator(compilationUnit);
        ExpressionContext prevCtx = parseTreeNav.move(exprOffset, ExpressionContext.class);
        AstNode node = compiler.parseTreeAstNodeMap.get(prevCtx);
        if (node instanceof ExprNode) {
            Type targetType = ((ExprNode) node).getType();
            if (!(targetType instanceof ObjectType)) {
                return Collections.emptyList();
            }
            ObjectType targetObjType = (ObjectType) targetType;
            return completeMembersForType(compilationUnit, targetObjType, anchorOffset, false);
        } else if (node instanceof ClassReference) {
            ClassReference cr = (ClassReference) node;
            ClassNode clsNode = cr.getReferencedClassNode();
            ClassType clsType = Types.getClassType(clsNode);
            return completeMembersForType(compilationUnit, clsType, anchorOffset, true);
        }
        return Collections.emptyList();
    }

    private List<Completion> completeMethodRef(CompilationUnit compilationUnit, int exprOffset, int anchorOffset) {
        ParseTreeNavigator parseTreeNav = NavigatorUtil.createParseTreeNavigator(compilationUnit);
        ExpressionContext prevCtx = parseTreeNav.move(exprOffset, ExpressionContext.class);
        AstNode node = compiler.parseTreeAstNodeMap.get(prevCtx);
        if (node instanceof ExprNode) {
            Type targetType = ((ExprNode) node).getType();
            if (!(targetType instanceof ObjectType)) {
                return Collections.emptyList();
            }
            ObjectType targetObjType = (ObjectType) targetType;
            return completeMethodRefForType(compilationUnit, targetObjType, anchorOffset);
        } else if (node instanceof ClassReference) {
            ClassReference cr = (ClassReference) node;
            ClassNode clsNode = cr.getReferencedClassNode();
            ClassType clsType = Types.getClassType(clsNode);
            return completeMethodRefForType(compilationUnit, clsType, anchorOffset);
        }
        return Collections.emptyList();
    }

    private List<Completion> completeMethodRefForType(CompilationUnit compilationUnit, ObjectType objectType , int anchorOffset) {
        Set<MethodRefCompletion> results = new HashSet<>();
        MethodDescriptor[] methods = objectType.getMethodDescriptors(compilationUnit.getAst(), true, true);
        for (MethodDescriptor m : methods) {
            results.add(new MethodRefCompletion(anchorOffset, m.getName()));
        }
        return new ArrayList<>(results);
    }

    private List<Completion> completeMembersForType(CompilationUnit compilationUnit, ObjectType objectType , int anchorOffset, @Nullable Boolean staticMember) {
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

    private List<Completion> completeMixinMethod(CompilationUnit compilationUnit, int exprOffset, int anchorOffset) {
        ParseTreeNavigator parseTreeNav = NavigatorUtil.createParseTreeNavigator(compilationUnit);
        ExpressionContext prevCtx = parseTreeNav.move(exprOffset, ExpressionContext.class);
        AstNode node = compiler.parseTreeAstNodeMap.get(prevCtx);
        if (node instanceof ExprNode) {
            Type targetType = ((ExprNode) node).getType();
            return completeMixinMethodForType(compilationUnit, targetType, anchorOffset);
        } else if (node instanceof ClassReference) {
            ClassReference cr = (ClassReference) node;
            ClassNode clsNode = cr.getReferencedClassNode();
            ClassType clsType = Types.getClassType(clsNode);
            return completeMixinMethodForType(compilationUnit, clsType, anchorOffset);
        }
        return Collections.emptyList();
    }

    private List<Completion> completeMixinMethodForType(CompilationUnit compilationUnit, Type type, int anchorOffset) {
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

    private boolean isDoubleColon(Token token) {
        return token.getType() == KalangLexer.DOUBLE_COLON;
    }

}
