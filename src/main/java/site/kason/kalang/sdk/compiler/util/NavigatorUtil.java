package site.kason.kalang.sdk.compiler.util;

import kalang.compiler.compile.CompilationUnit;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import site.kason.kalang.sdk.compiler.ParseTreeNavigator;
import site.kason.kalang.sdk.compiler.TokenNavigator;

/**
 * @author KasonYang
 */
public class NavigatorUtil {

    public static TokenNavigator createTokenNavigator(CompilationUnit compilationUnit) {
        CommonTokenStream ts = (CommonTokenStream) compilationUnit.getParser().getTokenStream();
        return new TokenNavigator(ts.getTokens().toArray(new Token[0]));
    }

    public static ParseTreeNavigator createParseTreeNavigator(CompilationUnit compilationUnit) {
        ParserRuleContext root = compilationUnit.getAstBuilder().getParseTree();
        return new ParseTreeNavigator(root);
    }

}
