package site.kason.intellij.psi;

import com.intellij.lang.ASTNode;
import org.antlr.intellij.adaptor.psi.ANTLRPsiNode;
import org.jetbrains.annotations.NotNull;

/**
 * @author KasonYang
 */
public class RulePsiNode extends ANTLRPsiNode {
    public RulePsiNode(@NotNull ASTNode node) {
        super(node);
    }
}
