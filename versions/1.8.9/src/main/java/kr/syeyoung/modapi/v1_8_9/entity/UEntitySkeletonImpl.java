package kr.syeyoung.modapi.v1_8_9.entity;

import kr.syeyoung.modapi.entity.EntityType;
import kr.syeyoung.modapi.entity.UEntitySkeleton;
import lombok.Getter;
import net.minecraft.entity.monster.EntitySkeleton;

public class UEntitySkeletonImpl extends UEntityLivingImpl implements UEntitySkeleton {
    @Getter
    protected EntitySkeleton delegate;

    public UEntitySkeletonImpl(EntitySkeleton delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    public int getSkeletonType() {
        return delegate.getSkeletonType();
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.SKELETON;
    }
}
