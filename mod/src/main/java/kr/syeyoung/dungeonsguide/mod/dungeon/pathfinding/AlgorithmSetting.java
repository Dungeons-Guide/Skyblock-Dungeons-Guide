package kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding;

import lombok.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.*;

import java.io.DataInputStream;
import java.io.IOException;

@Getter
@Data
@With
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AlgorithmSetting implements Cloneable {
    @Data @AllArgsConstructor
    public static class ToolSettings {
        private final ItemTool tool;
        private final int efficiency;

        public NBTTagCompound serialize() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("level", tool.getRegistryName());
            tag.setInteger("efficiency", efficiency);
            return tag;
        }

        public static ToolSettings deserialize(NBTBase base) {
            if (base instanceof NBTTagByte) return null;
            else if (base instanceof NBTTagCompound) {
                return new ToolSettings(
                        (ItemTool) Item.getByNameOrId(((NBTTagCompound) base).getString("level")),
                        ((NBTTagCompound) base).getInteger("efficiency"));
            }
            throw new IllegalArgumentException("Invalid tool settings: "+base);
        }

        public double getSpeed(int haste) {
            int val2 = efficiency;
            Item.ToolMaterial toolMaterial = tool.getToolMaterial();
            double efficiency2 = toolMaterial.getEfficiencyOnProperMaterial();
            efficiency2 += val2 * val2 + 1;
            efficiency2 *= haste * 0.2 + 1;
            return efficiency2;
        }
    }

    private final ToolSettings pickaxe;
    private final ToolSettings shovel;
    private final ToolSettings axe;

    private final int hasteLevel;

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


    private final double pickaxeSpeed;
    private final double shovelSpeed;
    private final double axeSpeed;

    public AlgorithmSetting(NBTTagCompound nbt) {
        if (nbt.getInteger("version") != 2) throw new IllegalArgumentException("Unexpected Algo Settings version: "+nbt.getInteger("version")+" / Expected: 2");
        this.pickaxe = ToolSettings.deserialize(nbt.getTag("pickaxe"));
        this.shovel = ToolSettings.deserialize(nbt.getTag("shovel"));
        this.axe = ToolSettings.deserialize(nbt.getTag("axe"));

        this.hasteLevel = nbt.getInteger("haste");
        this.stonkDown = nbt.getBoolean("stonkDown");
        this.stonkTeleport = nbt.getBoolean("stonkTeleport");
        this.stonkEChest = nbt.getBoolean("stonkEChest");
        this.routeEtherwarp = nbt.getBoolean("routeEtherwarp");
        this.maxStonk = nbt.getInteger("maxStonk");
        this.enderpearl = nbt.getBoolean("enderpearl");
        this.tntpearl = nbt.getBoolean("tntpearl");
        this.etherwarpOffset = nbt.getDouble("etherwarpOffset");
        this.etherwarpRadius = nbt.getInteger("etherwarpRadius");
        this.etherwarpLeeway = nbt.getDouble("etherwarpLeeway");

        this.pickaxeSpeed = pickaxe == null ? -1 : pickaxe.getSpeed(hasteLevel);
        this.shovelSpeed = shovel == null ? -1 : shovel.getSpeed(hasteLevel) / 30.0;
        this.axeSpeed = axe == null ? -1 : axe.getSpeed(hasteLevel) / 30.0;
    }

    public AlgorithmSetting(ToolSettings pickaxe, ToolSettings shovel, ToolSettings axe, int hasteLevel, boolean stonkDown, boolean stonkTeleport, boolean stonkEChest, boolean routeEtherwarp, int maxStonk, boolean enderpearl, boolean tntpearl, double etherwarpOffset, int etherwarpRadius, double etherwarpLeeway) {
        this.pickaxe = pickaxe;
        this.shovel = shovel;
        this.axe = axe;
        this.hasteLevel = hasteLevel;
        this.stonkDown = stonkDown;
        this.stonkTeleport = stonkTeleport;
        this.stonkEChest = stonkEChest;
        this.routeEtherwarp = routeEtherwarp;
        this.maxStonk = maxStonk;
        this.enderpearl = enderpearl;
        this.tntpearl = tntpearl;
        this.etherwarpOffset = etherwarpOffset;
        this.etherwarpRadius = etherwarpRadius;
        this.etherwarpLeeway = etherwarpLeeway;

        this.pickaxeSpeed = pickaxe == null ? -1 : pickaxe.getSpeed(hasteLevel);
        this.shovelSpeed = shovel == null ? -1 : shovel.getSpeed(hasteLevel) / 30.0;
        this.axeSpeed = axe == null ? -1 : axe.getSpeed(hasteLevel) / 30.0;
    }


    public NBTTagCompound serializeToNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("version", 2);

        nbt.setInteger("haste", hasteLevel);

        nbt.setTag("pickaxe", pickaxe == null ? new NBTTagByte((byte)0) : pickaxe.serialize());
        nbt.setTag("shovel", shovel == null ? new NBTTagByte((byte)0) : shovel.serialize());
        nbt.setTag("axe", axe == null ? new NBTTagByte((byte)0) : axe.serialize());

        nbt.setBoolean("stonkDown", stonkDown);
        nbt.setBoolean("stonkTeleport", stonkTeleport);
        nbt.setBoolean("stonkEChest", stonkEChest);
        nbt.setBoolean("routeEtherwarp", routeEtherwarp);
        nbt.setInteger("maxStonk", maxStonk);
        nbt.setBoolean("enderpearl", enderpearl);
        nbt.setBoolean("tntpearl", tntpearl);
        nbt.setDouble("etherwarpOffset", etherwarpOffset);
        nbt.setInteger("etherwarpRadius", etherwarpRadius);
        nbt.setDouble("etherwarpLeeway", etherwarpLeeway);
        return nbt;
    }

    public static AlgorithmSetting deserialize(DataInputStream dataInputStream) throws IOException {
        NBTTagCompound nbtTagCompound = CompressedStreamTools.read(dataInputStream, new NBTSizeTracker(10000));
        return new AlgorithmSetting(nbtTagCompound);
    }

    @Override
    public AlgorithmSetting clone() {
        try {
            AlgorithmSetting clone = (AlgorithmSetting) super.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
