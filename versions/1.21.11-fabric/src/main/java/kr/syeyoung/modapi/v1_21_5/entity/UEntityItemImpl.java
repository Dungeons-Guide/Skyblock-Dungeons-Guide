package kr.syeyoung.modapi.v1_21_5.entity;

import kr.syeyoung.modapi.entity.EntityType;
import kr.syeyoung.modapi.entity.UEntityItem;
import kr.syeyoung.modapi.item.UItemStack;
import kr.syeyoung.modapi.v1_21_5.item.UItemStackImpl;
import lombok.Getter;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;

public class UEntityItemImpl extends UEntityImpl implements UEntityItem {
    @Getter
    protected ItemEntity delegate;

    public UEntityItemImpl(ItemEntity delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    public UItemStack getItem() {
        ItemStack itemStack = delegate.getStack();
        return itemStack == null ? null : new UItemStackImpl(itemStack);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.ITEM;
    }
}
