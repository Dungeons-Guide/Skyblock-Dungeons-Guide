package kr.syeyoung.modapi.v1_21_11.world.entities;

import kr.syeyoung.modapi.world.tileentities.UTileEntityChest;
import net.minecraft.block.entity.ChestBlockEntity;

public class UTileEntityChestImpl extends UTileEntityImpl implements UTileEntityChest {
    protected ChestBlockEntity delegate;
    public UTileEntityChestImpl(ChestBlockEntity delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    public int getViewers() {
        return delegate.getAnimationProgress(0) > 0 ? 1 : 0;
    }
}
