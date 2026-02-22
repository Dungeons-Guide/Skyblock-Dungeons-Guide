package v1_21_11.gui;

import kr.syeyoung.modapi.gui.UContainerSlot;
import kr.syeyoung.modapi.item.UItemStack;
import kr.syeyoung.modapi.v1_21_5.item.UItemStackImpl;
import net.minecraft.screen.slot.Slot;

public class UContainerSlotImpl implements UContainerSlot {
    private Slot delegate;
    public UContainerSlotImpl(Slot delegate) {
        this.delegate = delegate;
    }

    @Override
    public int getX() {
        return delegate.x;
    }

    @Override
    public int getY() {
        return delegate.y;
    }

    @Override
    public int getSlotIndex() {
        return delegate.getIndex();
    }

    @Override
    public UItemStack getItemStack() {
        return delegate.getStack() == null ? null : new UItemStackImpl(delegate.getStack());
    }
}
