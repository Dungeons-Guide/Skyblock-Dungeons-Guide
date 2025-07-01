package kr.syeyoung.modapi.v1_21_5.item;

import kr.syeyoung.modapi.item.IItemStackRegistry;
import kr.syeyoung.modapi.item.UItemStack;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;

public class IItemStackRegistryImpl implements IItemStackRegistry {
    @Override
    public UItemStack fromNBT(CompoundBinaryTag binaryTag) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BinaryTagIO.writer().write(binaryTag, baos);
            byte[] result = baos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(result);
            NBTTagCompound nbt = CompressedStreamTools.read(new DataInputStream(bais));

            ItemStack itemStack = new ItemStack(Blocks.stone);
            itemStack.deserializeNBT(nbt);
            return new UItemStackImpl(itemStack);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
