package site.kason.kalang.sdk.compiler.complete;

import kalang.compiler.ast.VarObject;

/**
 * @author KasonYang
 */
public class VarCompletion implements Completion {

    private VarObject varObject;

    private int anchorOffset;

    public VarCompletion(VarObject varObject, int anchorOffset) {
        this.varObject = varObject;
        this.anchorOffset = anchorOffset;
    }

    @Override
    public int getAnchorOffset() {
        return anchorOffset;
    }

    @Override
    public String getCompleteString() {
        return varObject.getName();
    }

    public VarObject getVarObject() {
        return varObject;
    }

}
