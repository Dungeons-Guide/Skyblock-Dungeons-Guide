package kr.syeyoung.modapi.v1_8_9.entity;

import kr.syeyoung.modapi.entity.UPlayerSelf;
import kr.syeyoung.modapi.gui.UContainer;
import kr.syeyoung.modapi.item.UInventoryPlayer;
import kr.syeyoung.modapi.v1_8_9.gui.UContainerChestImpl;
import kr.syeyoung.modapi.v1_8_9.gui.UContainerImpl;
import kr.syeyoung.modapi.v1_8_9.item.UInventoryPlayerImpl;
import lombok.Getter;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;

public class UEntityPlayerSP extends UEntityPlayerImpl implements UPlayerSelf {
    @Getter
    protected EntityPlayerSP delegate;

    public UEntityPlayerSP(EntityPlayerSP delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    // TODO: is this the best place to be??
    public String getClientBrand() {
        return delegate.getClientBrand();
    }

    public UInventoryPlayer getInventory() {
        return delegate.inventory == null ? null : new UInventoryPlayerImpl(delegate.inventory);
    }

    public UContainer getOpenContainer() {
        Container c = delegate.openContainer;
        if (c == null) return null;
        if (c instanceof ContainerChest) return new UContainerChestImpl((ContainerChest) c);
        return new UContainerImpl(delegate.openContainer);
    }

    @Override
    public void setOpenContainer(UContainer guiChest) {
        delegate.openContainer = ((UContainerImpl)guiChest).getDelegate();
    }
}
