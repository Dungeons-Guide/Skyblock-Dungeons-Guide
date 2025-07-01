package kr.syeyoung.modapi.v1_21_5.util;

import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class NBTUtils {

    @NotNull
    public static CompoundBinaryTag convertNBT(NBTTagCompound nbtTagCompound) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(baos);
            CompressedStreamTools.write(nbtTagCompound, dataOutputStream);
            dataOutputStream.close();
            byte[] result = baos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(result);
            return BinaryTagIO.reader().read(bais);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
