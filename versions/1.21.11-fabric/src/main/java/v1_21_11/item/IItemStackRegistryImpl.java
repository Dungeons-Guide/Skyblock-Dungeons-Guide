package v1_21_11.item;

import kr.syeyoung.modapi.item.IItemStackRegistry;
import kr.syeyoung.modapi.item.UItemStack;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;

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
            NbtCompound nbt = NbtIo.readCompound(new DataInputStream(bais));

            ItemStack itemStack = ItemStack.fromNbt(MinecraftClient.getInstance().world.getRegistryManager(), nbt).orElse(null);
            return itemStack == null ? null : new UItemStackImpl(itemStack);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
