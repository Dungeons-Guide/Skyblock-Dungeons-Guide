package kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting;

import kr.syeyoung.modapi.world.UBlock;
import lombok.*;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.ByteBinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemTool;

import java.io.DataInput;
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

        public CompoundBinaryTag serialize() {
            return CompoundBinaryTag.builder()
                    .putString("level", tool.getRegistryName())
                    .putInt("efficiency", efficiency).build();
        }

        public static ToolSettings deserialize(BinaryTag base) {
            if (base instanceof ByteBinaryTag) return null;
            else if (base instanceof CompoundBinaryTag) {
                return new ToolSettings(
                        (ItemTool) Item.getByNameOrId(((CompoundBinaryTag) base).getString("level")),
                        ((CompoundBinaryTag) base).getInt("efficiency"));
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

        public boolean canHarvest(UBlock b) {
            return tool.canHarvestBlock(null);
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

    public AlgorithmSetting(CompoundBinaryTag nbt) {
        if (nbt.getInt("version") != 2) throw new IllegalArgumentException("Unexpected Algo Settings version: "+nbt.getInt("version")+" / Expected: 2");
        this.pickaxe = ToolSettings.deserialize(nbt.get("pickaxe"));
        this.shovel = ToolSettings.deserialize(nbt.get("shovel"));
        this.axe = ToolSettings.deserialize(nbt.get("axe"));

        this.hasteLevel = nbt.getInt("haste");
        this.stonkDown = nbt.getBoolean("stonkDown");
        this.stonkTeleport = nbt.getBoolean("stonkTeleport");
        this.stonkEChest = nbt.getBoolean("stonkEChest");
        this.allowSlowStonkPath = nbt.get("slowStonk") != null ? nbt.getBoolean("slowStonk") : true;
        this.routeEtherwarp = nbt.getBoolean("routeEtherwarp");
        this.maxStonk = nbt.getInt("maxStonk");
        this.enderpearl = nbt.getBoolean("enderpearl");
        this.tntpearl = nbt.getBoolean("tntpearl");
        this.etherwarpOffset = nbt.getDouble("etherwarpOffset");
        this.etherwarpRadius = nbt.getInt("etherwarpRadius");
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


    public CompoundBinaryTag serializeToNBT() {
        CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
        builder.putInt("version", 2);

        builder.putInt("haste", hasteLevel);

        if (pickaxe == null) builder.putByte("pickaxe", (byte) 0);
        else builder.put("pickaxe", pickaxe.serialize());
        if (shovel == null) builder.putByte("shovel", (byte) 0);
        else builder.put("pickaxe", shovel.serialize());
        if (axe == null) builder.putByte("axe", (byte) 0);
        else builder.put("axe", axe.serialize());
        builder.putBoolean("slowStonk", allowSlowStonkPath);

        builder.putBoolean("stonkDown", stonkDown);
        builder.putBoolean("stonkTeleport", stonkTeleport);
        builder.putBoolean("stonkEChest", stonkEChest);
        builder.putBoolean("routeEtherwarp", routeEtherwarp);
        builder.putInt("maxStonk", maxStonk);
        builder.putBoolean("enderpearl", enderpearl);
        builder.putBoolean("tntpearl", tntpearl);
        builder.putDouble("etherwarpOffset", etherwarpOffset);
        builder.putInt("etherwarpRadius", etherwarpRadius);
        builder.putDouble("etherwarpLeeway", etherwarpLeeway);
        return builder.build();
    }

    public static AlgorithmSetting deserialize(DataInputStream dataInputStream) throws IOException {
        CompoundBinaryTag tag = BinaryTagIO.reader().read((DataInput) dataInputStream);
        return new AlgorithmSetting(tag);
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
