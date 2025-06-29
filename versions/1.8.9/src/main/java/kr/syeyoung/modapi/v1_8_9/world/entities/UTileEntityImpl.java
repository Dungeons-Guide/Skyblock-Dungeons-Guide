package kr.syeyoung.modapi.v1_8_9.world.entities;

import kr.syeyoung.modapi.world.UTileEntity;
import net.minecraft.tileentity.TileEntity;

public class UTileEntityImpl implements UTileEntity {
    protected TileEntity delegate;
    public UTileEntityImpl(TileEntity delegate) {
        this.delegate = delegate;
    }

    public String serialize() {
        return "";
    }
}
