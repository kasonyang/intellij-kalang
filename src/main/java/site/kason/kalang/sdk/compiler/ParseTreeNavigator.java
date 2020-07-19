package site.kason.kalang.sdk.compiler;

import kalang.compiler.compile.OffsetRange;
import kalang.compiler.util.OffsetRangeHelper;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Predicate;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import javax.annotation.Nullable;

/**
 * @author KasonYang
 */
public class ParseTreeNavigator {

    private ParseTree parseTree;

    private ParseTree currentFound;

    public ParseTreeNavigator(ParseTree parseTree) {
        this.parseTree = parseTree;
    }

    @Nullable
    public ParseTree move(int offset, Predicate<ParseTree> predicate) {
        currentFound = null;
        visit(parseTree, offset, predicate);
        return currentFound;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T move(int offset, final Class<T> treeClass) {
        return (T) move(offset, new Predicate<ParseTree>() {
            @Override
            public boolean test(ParseTree tree) {
                return treeClass.isAssignableFrom(tree.getClass());
            }
        });
    }

    private boolean visit(ParseTree tree, int offset, Predicate<ParseTree> predicate) {
        OffsetRange offsetRange = getOffset(tree);
        if (offsetRange != null && (offset < offsetRange.startOffset || offset > offsetRange.stopOffset)) {
            return false;
        }
        if (predicate.test(tree)) {
            currentFound = tree;
        }
        int childCount = tree.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ParseTree child = tree.getChild(i);
            if (visit(child, offset, predicate)) {
                break;
            }
        }
        return true;
    }

    @Nullable
    private OffsetRange getOffset(ParseTree parseTree) {
        if (parseTree instanceof ParserRuleContext) {
            return OffsetRangeHelper.getOffsetRange((ParserRuleContext) parseTree);
        } else if (parseTree instanceof TerminalNode) {
            return OffsetRangeHelper.getOffsetRange(((TerminalNode) parseTree).getSymbol());
        }
        return null;
    }

}
