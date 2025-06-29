package kr.syeyoung.modapi.v1_8_9.world.entities;

import kr.syeyoung.modapi.world.UTileEntity;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class UTileEntityImpl implements UTileEntity {
    protected TileEntity delegate;
    public UTileEntityImpl(TileEntity delegate) {
        this.delegate = delegate;
    }

    public CompoundBinaryTag serialize() {
        NBTTagCompound compound = new NBTTagCompound();
        delegate.writeToNBT(compound);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(baos);
            CompressedStreamTools.write(compound, dataOutputStream);
            dataOutputStream.close();
            byte[] result = baos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(result);
            return BinaryTagIO.reader().read(bais);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
