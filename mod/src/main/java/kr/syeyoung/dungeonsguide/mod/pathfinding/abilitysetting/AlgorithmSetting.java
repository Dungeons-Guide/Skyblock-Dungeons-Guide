package kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting;

import lombok.*;
import net.minecraft.world.item.Item;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.*;

import java.io.DataInputStream;
import java.io.IOException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NbtIo;

@Getter
@Data
@With
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AlgorithmSetting implements Cloneable {
    @Data @AllArgsConstructor
    public static class ToolSettings {
        private final ItemTool tool;
        private final int efficiency;

        public CompoundTag serialize() {
            CompoundTag tag = new CompoundTag();
            tag.setString("level", tool.getRegistryName());
            tag.putInt("efficiency", efficiency);
            return tag;
        }

        public static ToolSettings deserialize(Tag base) {
            if (base instanceof ByteTag) return null;
            else if (base instanceof CompoundTag) {
                return new ToolSettings(
                        (ItemTool) Item.getByNameOrId(((CompoundTag) base).getString("level")),
                        ((CompoundTag) base).getInteger("efficiency"));
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
    private final boolean allowSlowStonkPath;

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

    public AlgorithmSetting(CompoundTag nbt) {
        if (nbt.getInteger("version") != 2) throw new IllegalArgumentException("Unexpected Algo Settings version: "+nbt.getInteger("version")+" / Expected: 2");
        this.pickaxe = ToolSettings.deserialize(nbt.getTag("pickaxe"));
        this.shovel = ToolSettings.deserialize(nbt.getTag("shovel"));
        this.axe = ToolSettings.deserialize(nbt.getTag("axe"));

        this.hasteLevel = nbt.getInteger("haste");
        this.stonkDown = nbt.getBoolean("stonkDown");
        this.stonkTeleport = nbt.getBoolean("stonkTeleport");
        this.stonkEChest = nbt.getBoolean("stonkEChest");
        this.allowSlowStonkPath = nbt.hasKey("slowStonk") ? nbt.getBoolean("slowStonk") : true;
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

    public AlgorithmSetting(ToolSettings pickaxe, ToolSettings shovel, ToolSettings axe, int hasteLevel, boolean stonkDown, boolean stonkTeleport, boolean stonkEChest, boolean routeEtherwarp, int maxStonk, boolean enderpearl, boolean tntpearl, double etherwarpOffset, int etherwarpRadius, double etherwarpLeeway, boolean slowStonk) {
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
        this.allowSlowStonkPath = slowStonk;
    }


    public CompoundTag serializeToNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("version", 2);

        nbt.putInt("haste", hasteLevel);

        nbt.put("pickaxe", pickaxe == null ? new ByteTag((byte)0) : pickaxe.serialize());
        nbt.put("shovel", shovel == null ? new ByteTag((byte)0) : shovel.serialize());
        nbt.put("axe", axe == null ? new ByteTag((byte)0) : axe.serialize());
        nbt.putBoolean("slowStonk", allowSlowStonkPath);

        nbt.putBoolean("stonkDown", stonkDown);
        nbt.putBoolean("stonkTeleport", stonkTeleport);
        nbt.putBoolean("stonkEChest", stonkEChest);
        nbt.putBoolean("routeEtherwarp", routeEtherwarp);
        nbt.putInt("maxStonk", maxStonk);
        nbt.putBoolean("enderpearl", enderpearl);
        nbt.putBoolean("tntpearl", tntpearl);
        nbt.putDouble("etherwarpOffset", etherwarpOffset);
        nbt.putInt("etherwarpRadius", etherwarpRadius);
        nbt.putDouble("etherwarpLeeway", etherwarpLeeway);
        return nbt;
    }

    public static AlgorithmSetting deserialize(DataInputStream dataInputStream) throws IOException {
        CompoundTag nbtTagCompound = NbtIo.read(dataInputStream, new NBTSizeTracker(10000));
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
