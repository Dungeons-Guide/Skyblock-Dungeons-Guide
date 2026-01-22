package kr.syeyoung.modapi.v1_8_9.gui;

import kr.syeyoung.modapi.gui.UContainerSlot;
import kr.syeyoung.modapi.item.UItemStack;
import kr.syeyoung.modapi.v1_8_9.item.UItemStackImpl;
import net.minecraft.inventory.Slot;

public class UContainerSlotImpl implements UContainerSlot {
    private Slot delegate;
    public UContainerSlotImpl(Slot delegate) {
        this.delegate = delegate;
    }

    @Override
    public int getX() {
        return delegate.xDisplayPosition;
    }

    @Override
    public int getY() {
        return delegate.yDisplayPosition;
    }

    @Override
    public int getSlotIndex() {
        return delegate.slotNumber;
    }

    @Override
    public UItemStack getItemStack() {
        return delegate.getStack() == null ? null : new UItemStackImpl(delegate.getStack());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UContainerSlotImpl)) return false;
        return delegate.equals(((UContainerSlotImpl) obj).delegate);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }
}
