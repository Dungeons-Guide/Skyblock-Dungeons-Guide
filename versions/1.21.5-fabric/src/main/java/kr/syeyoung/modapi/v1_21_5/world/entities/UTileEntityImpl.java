package kr.syeyoung.modapi.v1_21_5.world.entities;

import kr.syeyoung.modapi.v1_8_9.util.NBTUtils;
import kr.syeyoung.modapi.world.UTileEntity;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class UTileEntityImpl implements UTileEntity {
    protected TileEntity delegate;
    public UTileEntityImpl(TileEntity delegate) {
        this.delegate = delegate;
    }

    public CompoundBinaryTag serialize() {
        NBTTagCompound compound = new NBTTagCompound();
        delegate.writeToNBT(compound);
        return NBTUtils.convertNBT(compound);
    }
}
