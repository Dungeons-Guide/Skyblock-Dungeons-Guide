package kr.syeyoung.modapi.v1_21_11.util;

import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class NBTUtils {

    @NotNull
    public static CompoundBinaryTag convertNBT(NbtElement nbtTagCompound) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(baos);
            NbtIo.write(nbtTagCompound, dataOutputStream);
            dataOutputStream.close();
            byte[] result = baos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(result);
            return BinaryTagIO.reader().read(bais);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
