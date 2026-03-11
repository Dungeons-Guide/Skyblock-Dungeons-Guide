package kr.syeyoung.modapi.v1_21_11.entity;

import kr.syeyoung.modapi.entity.UEntityArmorStand;
import kr.syeyoung.modapi.item.UItemStack;
import kr.syeyoung.modapi.v1_21_11.entity.UEntityLivingImpl;
import kr.syeyoung.modapi.v1_21_11.item.UItemStackImpl;
import lombok.Getter;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;

public class UEntityArmorStandImpl extends UEntityLivingImpl implements UEntityArmorStand {
    @Getter
    protected ArmorStandEntity delegate;

    public UEntityArmorStandImpl(ArmorStandEntity delegate) {
        super(delegate);
        this.delegate = delegate;
    }


    EquipmentSlot[] slot = new EquipmentSlot[] {
            EquipmentSlot.MAINHAND,
            EquipmentSlot.FEET,
            EquipmentSlot.LEGS,
            EquipmentSlot.CHEST,
            EquipmentSlot.HEAD
    };
    public UItemStack getEquipmentInSlot(int slotIn) {
        ItemStack itemStack = delegate.getEquippedStack(slot[slotIn]);
        return itemStack == null ? null : new UItemStackImpl(itemStack);
    }

    public boolean getAlwaysRenderNameTag() {
        return delegate.isCustomNameVisible();
    }
}
