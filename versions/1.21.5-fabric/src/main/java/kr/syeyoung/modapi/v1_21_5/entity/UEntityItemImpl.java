package kr.syeyoung.modapi.v1_21_5.entity;

import kr.syeyoung.modapi.entity.EntityType;
import kr.syeyoung.modapi.entity.UEntityItem;
import kr.syeyoung.modapi.item.UItemStack;
import kr.syeyoung.modapi.v1_8_9.item.UItemStackImpl;
import lombok.Getter;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;

public class UEntityItemImpl extends UEntityImpl implements UEntityItem {
    @Getter
    protected EntityItem delegate;

    public UEntityItemImpl(EntityItem delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    public UItemStack getItem() {
        ItemStack itemStack = delegate.getEntityItem();
        return itemStack == null ? null : new UItemStackImpl(itemStack);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.ITEM;
    }
}
