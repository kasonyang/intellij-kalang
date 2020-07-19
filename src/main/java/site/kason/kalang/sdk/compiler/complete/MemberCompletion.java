package site.kason.kalang.sdk.compiler.complete;

import kalang.compiler.core.ObjectType;

import javax.annotation.Nullable;

/**
 * @author KasonYang
 */
public class MemberCompletion implements Completion {

    private ObjectType ownerType;

    @Nullable
    private Boolean staticMember;

    private int anchorOffset;

    public MemberCompletion(int anchorOffset, ObjectType ownerType, @Nullable Boolean staticMember) {
        this.anchorOffset = anchorOffset;
        this.ownerType = ownerType;
        this.staticMember = staticMember;
    }

    public ObjectType getOwnerType() {
        return ownerType;
    }

    @Nullable
    public Boolean getStaticMember() {
        return staticMember;
    }

    @Override
    public int getAnchorOffset() {
        return anchorOffset;
    }
}
