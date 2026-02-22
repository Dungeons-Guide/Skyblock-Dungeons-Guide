package v1_21_11.entity;

import kr.syeyoung.modapi.entity.EntityType;
import kr.syeyoung.modapi.entity.UEntitySkeleton;
import lombok.Getter;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;

public class UEntitySkeletonImpl extends UEntityLivingImpl implements UEntitySkeleton {
    @Getter
    protected AbstractSkeletonEntity delegate;

    public UEntitySkeletonImpl(AbstractSkeletonEntity delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    public int getSkeletonType() {
        return delegate instanceof WitherSkeletonEntity ? 1 : 0;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.SKELETON;
    }
}
