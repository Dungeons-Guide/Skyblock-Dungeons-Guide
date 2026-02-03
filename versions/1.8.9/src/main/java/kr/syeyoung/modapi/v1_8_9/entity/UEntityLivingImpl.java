package kr.syeyoung.modapi.v1_8_9.entity;

import kr.syeyoung.modapi.entity.UEntityLiving;
import kr.syeyoung.modapi.item.UItemStack;
import kr.syeyoung.modapi.v1_8_9.item.UItemStackImpl;
import lombok.Getter;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

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

    public UItemStack getCurrentArmor(int slotIn) {
        ItemStack itemStack =  delegate.getCurrentArmor(slotIn);
        return itemStack == null ? null : new UItemStackImpl(itemStack);
    }

    public UItemStack getHeldItem() {
        ItemStack itemStack =  delegate.getHeldItem();
        return itemStack == null ? null : new UItemStackImpl(itemStack);
    }

    @Override
    public Object getHandle() {
        return delegate;
    }
}
