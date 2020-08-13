package site.kason.kalang.intellij;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import kalang.compiler.antlr.KalangParser;
import org.antlr.intellij.adaptor.lexer.RuleIElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author KasonYang
 */
public class KalangFoldingBuilder extends FoldingBuilderEx {

    RuleIElementType BLOCK_STMT = KalangTokenTypes.getRuleElementType(KalangParser.RULE_blockStmt);

    RuleIElementType CLASS_BODY = KalangTokenTypes.getRuleElementType(KalangParser.RULE_classBody);

    @NotNull
    @Override
    public FoldingDescriptor[] buildFoldRegions(@NotNull PsiElement root, @NotNull Document document, boolean quick) {
        List<FoldingDescriptor> descriptors = new ArrayList<>();
        PsiElement[] blockNodes = PsiTreeUtil.collectElements(root, e -> BLOCK_STMT.equals(e.getNode().getElementType()));
        for (PsiElement bsn : blockNodes) {
            int start = bsn.getTextRange().getStartOffset() + 1;
            int stop = bsn.getTextRange().getEndOffset() - 1;
            descriptors.add(new FoldingDescriptor(bsn.getNode(), new TextRange(start, stop)));
        }
        PsiElement[] classBodies = PsiTreeUtil.collectElements(root, e -> CLASS_BODY.equals(e.getNode().getElementType()));
        for (PsiElement cb : classBodies) {
            PsiElement startToken = getPrevSibling(cb, e -> "{".equals(e.getText()));
            PsiElement stopToken = getNextSibling(cb, e -> "}".equals(e.getText()));
            if (startToken == null || stopToken == null) {
                continue;
            }
            TextRange range = new TextRange(
                    startToken.getTextRange().getStartOffset() + 1,
                    stopToken.getTextRange().getEndOffset() - 1
            );
            descriptors.add(new FoldingDescriptor(cb.getNode(), range));
        }

        return descriptors.toArray(new FoldingDescriptor[0]);
    }

    @Override
    public @Nullable String getPlaceholderText(@NotNull ASTNode node) {
        return null;
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull ASTNode node) {
        return false;
    }

    private PsiElement getPrevSibling(PsiElement ele, Predicate<PsiElement> filter) {
        PsiElement ps = ele.getPrevSibling();
        if (ps == null) {
            return null;
        }
        if (filter.test(ps)) {
            return ps;
        }
        return getPrevSibling(ps, filter);
    }

    private PsiElement getNextSibling(PsiElement ele, Predicate<PsiElement> filter) {
        PsiElement ps = ele.getNextSibling();
        if (ps == null) {
            return null;
        }
        if (filter.test(ps)) {
            return ps;
        }
        return getNextSibling(ps, filter);
    }

}
