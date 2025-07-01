package kr.syeyoung.modapi.v1_21_5.entity;

import kr.syeyoung.modapi.entity.UEntityArmorStand;
import kr.syeyoung.modapi.item.UItemStack;
import kr.syeyoung.modapi.v1_8_9.item.UItemStackImpl;
import lombok.Getter;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.item.ItemStack;

public class UEntityArmorStandImpl extends UEntityLivingImpl implements UEntityArmorStand {
    @Getter
    protected ArmorStandEntity delegate;

    public UEntityArmorStandImpl(ArmorStandEntity delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    public UItemStack getEquipmentInSlot(int slotIn) {
        ItemStack itemStack = delegate.getEquipmentInSlot(slotIn);
        return itemStack == null ? null : new UItemStackImpl(itemStack);
    }

    public boolean getAlwaysRenderNameTag() {
        return delegate.getAlwaysRenderNameTag();
    }
}
