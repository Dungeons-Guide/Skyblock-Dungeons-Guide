package v1_21_11.entity;

import kr.syeyoung.modapi.entity.EntityType;
import kr.syeyoung.modapi.entity.UEntityItemFrame;
import kr.syeyoung.modapi.item.UItemStack;
import kr.syeyoung.modapi.v1_21_5.item.UItemStackImpl;
import lombok.Getter;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;

public class UEntityItemFrameImpl extends UEntityImpl implements UEntityItemFrame {
    @Getter
    protected ItemFrameEntity delegate;

    public UEntityItemFrameImpl(ItemFrameEntity delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    public UItemStack getItem() {
        ItemStack itemStack = delegate.getHeldItemStack();
        return itemStack == null ? null : new UItemStackImpl(itemStack);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.ITEM_FRAME;
    }

    public int getRotation() {
        return delegate.getRotation();
    }
}
