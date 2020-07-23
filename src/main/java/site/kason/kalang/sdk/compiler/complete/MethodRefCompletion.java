package site.kason.kalang.sdk.compiler.complete;

import java.util.Objects;

/**
 * @author KasonYang
 */
public class MethodRefCompletion implements Completion {

    private int anchorOffset;

    private String methodName;

    public MethodRefCompletion(int anchorOffset, String methodName) {
        this.anchorOffset = anchorOffset;
        this.methodName = methodName;
    }

    @Override
    public int getAnchorOffset() {
        return anchorOffset;
    }

    @Override
    public String getCompleteString() {
        return methodName;
    }

    public String getMethodName() {
        return methodName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodRefCompletion that = (MethodRefCompletion) o;
        return anchorOffset == that.anchorOffset &&
                methodName.equals(that.methodName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(anchorOffset, methodName);
    }
}
