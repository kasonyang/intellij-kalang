package site.kason.kalang.sdk.compiler.complete;

import kalang.compiler.core.MethodDescriptor;

/**
 * @author KasonYang
 */
public class MethodCompletion implements Completion {

    private MethodDescriptor method;

    private int anchorOffset;

    public MethodCompletion(int anchorOffset, MethodDescriptor method) {
        this.anchorOffset = anchorOffset;
        this.method = method;
    }

    public MethodDescriptor getMethod() {
        return method;
    }

    @Override
    public int getAnchorOffset() {
        return anchorOffset;
    }
}
