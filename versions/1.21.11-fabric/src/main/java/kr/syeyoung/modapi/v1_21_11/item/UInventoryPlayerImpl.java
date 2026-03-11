package kr.syeyoung.modapi.v1_21_11.item;

import kr.syeyoung.modapi.item.UInventoryPlayer;
import kr.syeyoung.modapi.item.UItemStack;
import net.minecraft.entity.player.PlayerInventory;

public class UInventoryPlayerImpl implements UInventoryPlayer {
    private PlayerInventory delegate;

    public UInventoryPlayerImpl(PlayerInventory delegate) {
        this.delegate = delegate;
    }

    public UItemStack[] getArmorInventory() {
        if (delegate == null) return null;
        UItemStack[] itemStacks = new UItemStack[4];
        for (int i = 0; i < itemStacks.length; i++) {
            itemStacks[i] = delegate.getStack(36+i) == null ? null : new UItemStackImpl(delegate.getStack(36+i));
        }
        return itemStacks;
    }

    public UItemStack[] getMainInventory() {
        if (delegate == null) return null;
        UItemStack[] itemStacks = new UItemStack[36];
        for (int i = 0; i < itemStacks.length; i++) {
            itemStacks[i] = delegate.getStack(i) == null ? null : new UItemStackImpl(delegate.getStack(i));
        }
        return itemStacks;
    }
}
