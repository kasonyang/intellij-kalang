package site.kason.kalang.sdk.compiler.complete;

import kalang.compiler.core.FieldDescriptor;

/**
 * @author KasonYang
 */
public class FieldCompletion  implements Completion{

    private FieldDescriptor field;

    private int anchorOffset;

    public FieldCompletion(FieldDescriptor field, int anchorOffset) {
        this.field = field;
        this.anchorOffset = anchorOffset;
    }

    public FieldDescriptor getField() {
        return field;
    }

    @Override
    public int getAnchorOffset() {
        return anchorOffset;
    }

}
