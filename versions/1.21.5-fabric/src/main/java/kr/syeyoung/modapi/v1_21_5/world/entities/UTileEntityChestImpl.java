package kr.syeyoung.modapi.v1_21_5.world.entities;

import kr.syeyoung.modapi.world.tileentities.UTileEntityChest;
import net.minecraft.tileentity.TileEntityChest;

public class UTileEntityChestImpl extends UTileEntityImpl implements UTileEntityChest {
    protected TileEntityChest delegate;
    public UTileEntityChestImpl(TileEntityChest delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    public int getViewers() {
        return delegate.numPlayersUsing;
    }
}
