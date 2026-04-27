package kr.syeyoung.modapi.v1_21_9.world.entities;

import kr.syeyoung.modapi.v1_21_9.util.NBTUtils;
import kr.syeyoung.modapi.world.UTileEntity;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;

public class UTileEntityImpl implements UTileEntity {
    protected BlockEntity delegate;
    public UTileEntityImpl(BlockEntity delegate) {
        this.delegate = delegate;
    }

    public CompoundBinaryTag serialize() {
        NbtCompound compound1 = delegate.createNbt(delegate.getWorld().getRegistryManager());
        return NBTUtils.convertNBT(compound1);
    }
}
