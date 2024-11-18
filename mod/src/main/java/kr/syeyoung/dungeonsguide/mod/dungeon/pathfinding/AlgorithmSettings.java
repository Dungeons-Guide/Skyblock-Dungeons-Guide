package kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;

import java.io.DataInputStream;
import java.io.IOException;

@AllArgsConstructor
@Getter
public class AlgorithmSettings {
    private final Item pickaxe;
    private final double pickaxeSpeed;
    private final double shovelSpeed;
    private final double axeSpeed;

    private final boolean stonkDown;
    private final boolean stonkTeleport;
    private final boolean stonkEChest;

    private final boolean routeEtherwarp;

    private final int maxStonk;
    private final boolean enderpearl;
    private final boolean tntpearl;

    private final double etherwarpOffset;
    private final int etherwarpRadius;
    private final double etherwarpLeeway;


    public NBTTagCompound serializeToNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setDouble("version", 1);
        nbt.setString("pickaxe", pickaxe.getRegistryName());
        nbt.setDouble("pickaxeSpeed", pickaxeSpeed);
        nbt.setDouble("shovelSpeed", shovelSpeed);
        nbt.setDouble("axeSpeed", axeSpeed);
        nbt.setBoolean("stonkDown", stonkTeleport);
        nbt.setBoolean("stonkTeleport", stonkTeleport);
        nbt.setBoolean("stonkEChest", stonkEChest);
        nbt.setBoolean("routeEtherwarp", routeEtherwarp);
        nbt.setInteger(("maxStonk"), maxStonk);
        nbt.setBoolean("enderpearl", enderpearl);
        nbt.setBoolean("tntpearl", tntpearl);
        nbt.setDouble("etherwarpOffset", etherwarpOffset);
        nbt.setInteger("etherwarpRadius", etherwarpRadius);
        nbt.setDouble("etherwarpLeeway", etherwarpLeeway);
        return nbt;
    }

    public static AlgorithmSettings deserialize(NBTTagCompound nbt) {
        return new AlgorithmSettings(
                Item.getByNameOrId(nbt.getString("pickaxe")),
                nbt.getDouble("pickaxeSpeed"),
                nbt.getDouble("shovelSpeed"),
                nbt.getDouble("axeSpeed"),
                nbt.getBoolean("stonkDown"),
                nbt.getBoolean("stonkTeleport"),
                nbt.getBoolean("stonkEChest"),
                nbt.getBoolean("routeEtherwarp"),
                nbt.getInteger("maxStonk"),
                nbt.getBoolean("enderpearl"),
                nbt.getBoolean("tntpearl"),
                nbt.getDouble("etherwarpOffset"),
                nbt.getInteger("etherwarpRadius"),
                nbt.getDouble("etherwarpLeeway")
        );
    }

    public static AlgorithmSettings deserialize(DataInputStream dataInputStream) throws IOException {
        NBTTagCompound nbtTagCompound = CompressedStreamTools.read(dataInputStream, new NBTSizeTracker(10000));
        return AlgorithmSettings.deserialize(nbtTagCompound);
    }
}
