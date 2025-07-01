package kr.syeyoung.modapi.v1_21_5.gui;

import kr.syeyoung.modapi.gui.UContainerChest;
import kr.syeyoung.modapi.gui.UContainerSlot;
import net.minecraft.inventory.ContainerChest;

public class UContainerChestImpl extends UContainerImpl implements UContainerChest {
    protected ContainerChest delegate;
    public UContainerChestImpl(ContainerChest delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    public String getName() {
        return delegate.getLowerChestInventory() == null ? null : delegate.getLowerChestInventory().getName();
    }

    @Override
    public int getChestContainerSize() {
        return delegate.getLowerChestInventory().getSizeInventory();
    }

    @Override
    public UContainerSlot getChestSlotAt(int slot) {
        return delegate.getSlot(slot) == null ? null : new UContainerSlotImpl(delegate.getSlot(slot));
    }
}
