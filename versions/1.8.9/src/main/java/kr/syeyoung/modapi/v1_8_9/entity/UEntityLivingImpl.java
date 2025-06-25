package kr.syeyoung.modapi.v1_8_9.entity;

import kr.syeyoung.modapi.entity.UEntityLiving;
import lombok.Getter;
import net.minecraft.entity.EntityLivingBase;

public class UEntityLivingImpl extends UEntityImpl implements UEntityLiving {
    @Getter
    protected EntityLivingBase delegate;

    public UEntityLivingImpl(EntityLivingBase delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    // TODO: should i let interpolation be done in rendering??? idk.
    public float getPrevRotationYawHead() {
        return delegate.prevRotationYawHead;
    }

    public float getRotationYawHead() {
        return delegate.rotationYawHead;
    }

    public float getHealth() {
        return delegate.getHealth();
    }
}
