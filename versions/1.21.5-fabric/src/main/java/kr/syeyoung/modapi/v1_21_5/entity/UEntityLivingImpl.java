package kr.syeyoung.modapi.v1_21_5.entity;

import kr.syeyoung.modapi.entity.UEntityLiving;
import kr.syeyoung.modapi.item.UItemStack;
import kr.syeyoung.modapi.v1_21_5.item.UItemStackImpl;
import lombok.Getter;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public class UEntityLivingImpl extends UEntityImpl implements UEntityLiving {
    @Getter
    protected LivingEntity delegate;

    public UEntityLivingImpl(LivingEntity delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    // TODO: should i let interpolation be done in rendering??? idk.
    public float getPrevRotationYawHead() {
        return delegate.lastHeadYaw;
    }

    public float getRotationYawHead() {
        return delegate.headYaw;
    }

    public float getHealth() {
        return delegate.getHealth();
    }

    // TODO: change to modern.

    private EquipmentSlot[] slot = new EquipmentSlot[] {
            EquipmentSlot.FEET,
            EquipmentSlot.LEGS,
            EquipmentSlot.CHEST,
            EquipmentSlot.HEAD
    };

    public UItemStack getCurrentArmor(int slotIn) {
        ItemStack itemStack =  delegate.getEquippedStack(slot[slotIn]);
        return itemStack == null ? null : new UItemStackImpl(itemStack);
    }

    public UItemStack getHeldItem() {
        ItemStack itemStack =  delegate.getMainHandStack();
        return itemStack == null ? null : new UItemStackImpl(itemStack);
    }
}
