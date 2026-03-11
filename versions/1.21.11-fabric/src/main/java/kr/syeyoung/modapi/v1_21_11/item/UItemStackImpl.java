package kr.syeyoung.modapi.v1_21_11.item;

import com.mojang.authlib.properties.Property;
import kr.syeyoung.modapi.item.Item;
import kr.syeyoung.modapi.item.UItemStack;
import kr.syeyoung.modapi.util.EnumDyeColor;
import kr.syeyoung.modapi.v1_21_11.util.NBTUtils;
import lombok.Getter;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minecraft.block.Blocks;
import net.minecraft.block.ConcretePowderBlock;
import net.minecraft.block.Stainable;
import net.minecraft.block.StainedGlassPaneBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.*;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class UItemStackImpl implements UItemStack {
    @Getter
    private ItemStack delegate;

    public UItemStackImpl(ItemStack delegate) {
        this.delegate = delegate;
    }


    public Item getItem() {
        if (delegate.getItem() == Items.PLAYER_HEAD || delegate.getItem() == Items.SKELETON_SKULL || delegate.getItem() == Items.WITHER_SKELETON_SKULL || delegate.getItem() == Items.CREEPER_HEAD || delegate.getItem() == Items.ZOMBIE_HEAD || delegate.getItem() == Items.PIGLIN_HEAD) return Item.SKULL;
        if (delegate.getItem() instanceof DyeItem) return Item.DYE;
        if (delegate.getItem() == Items.MAP) return Item.MAP;
        if (delegate.getItem() == Items.FILLED_MAP) return Item.FILLED_MAP;
        if (delegate.getItem() == Items.ARROW) return Item.ARROW;
        if (delegate.getItem() == Items.STICK) return Item.STICK;
        if (delegate.getItem() == Items.GOLDEN_AXE) return Item.GOLDEN_AXE;
        if (delegate.getItem() instanceof SpawnEggItem) return Item.SPAWN_EGG;
        if (delegate.getItem() == Items.GOLDEN_SHOVEL) return Item.GOLDEN_SHOVEL;
        if (delegate.getItem() == Items.BEDROCK) return Item.BEDROCK;
        if (delegate.getItem() == Items.BOOKSHELF) return Item.BOOKSHELF;
        if (delegate.getItem() instanceof BlockItem && ((BlockItem) delegate.getItem()).getBlock() instanceof StainedGlassPaneBlock) return Item.STAINED_GLASS_PANE;
        if (delegate.getItem() instanceof BlockItem && ((BlockItem) delegate.getItem()).getBlock() instanceof TerracottaBlock) return Item.STAINED_HARDENED_CLAY;
        return Item.UNKNOWN;
    }

    @Override
    public int getMetadata() {
        return delegate.getDamage();
    }

    @Override
    public String getSkullTexture() {
        if (delegate.getItem() != Items.PLAYER_HEAD) return null;
        if (delegate.getItem() instanceof PlayerHeadItem item) {
            ProfileComponent component = item.getComponents().get(DataComponentTypes.PROFILE);
            if (component == null) return null;
            return component.gameProfile().getProperties().get("textures").stream().findFirst().map(Property::value).orElse(null);
        }
        return null;
    }

    @Override
    public CompoundBinaryTag serialize() {
        NbtElement compound = delegate.toNbt(MinecraftClient.getInstance().world.getRegistryManager());
        return NBTUtils.convertNBT(compound);
    }

    @Override
    public CompoundBinaryTag getSkyblockAttrib() {
        NbtElement compound = delegate.toNbt(MinecraftClient.getInstance().world.getRegistryManager());
        return NBTUtils.convertNBT(compound).getCompound("ExtraAttributes");
    }


    @Override
    public List<String> getLore() { // TODO: to component.
        LoreComponent component = delegate.getComponents().get(DataComponentTypes.LORE);
        List<String> line = new ArrayList<>();
        for (Text styledLine : component.styledLines()) {
            line.add(styledLine.getString());
        }
        return line;
    }

    public String getSkyblockId() {
        CompoundBinaryTag tag = getSkyblockAttrib();
        if (tag == null) return null;
        return tag.getString("id");
    }

    @Override // TODO: to component.
    public String getDisplayName() {
        return delegate.getName().getString();
    }

    @Override
    public Object getItemStack() {
        return delegate;
    }

    @Override
    public List<String> getNormalTooltip() {
        List<Text> tooltip = delegate.getTooltip(
                net.minecraft.item.Item.TooltipContext.create(MinecraftClient.getInstance().world),
                MinecraftClient.getInstance().player,
                MinecraftClient.getInstance().options.advancedItemTooltips ? TooltipType.ADVANCED : TooltipType.BASIC);

        List<String> tooltipConv = new ArrayList<>();
        for (Text text : tooltip) {
            tooltipConv.add(text.getString());
        }
        return tooltipConv;
    }

    public boolean isItemEnchanted() {
        return delegate.hasEnchantments();
    }




    @Override
    public EnumDyeColor getItemColor() {
        if (delegate.getItem() instanceof DyeItem)
            return EnumDyeColor.VALUES[((DyeItem) delegate.getItem()).getColor().getIndex()];
        if (delegate.getItem() instanceof BlockItem && ((BlockItem) delegate.getItem()).getBlock() instanceof Stainable)
            return EnumDyeColor.VALUES[((Stainable) ((BlockItem) delegate.getItem()).getBlock()).getColor().getIndex()];
        if (delegate.getItem() instanceof BlockItem && ((BlockItem) delegate.getItem()).getBlock() instanceof TerracottaBlock terracottaBlock) {
            if (terracottaBlock == Blocks.WHITE_TERRACOTTA) return EnumDyeColor.WHITE;
            if (terracottaBlock == Blocks.ORANGE_TERRACOTTA) return EnumDyeColor.ORANGE;
            if (terracottaBlock == Blocks.MAGENTA_TERRACOTTA) return EnumDyeColor.MAGENTA;
            if (terracottaBlock == Blocks.LIGHT_BLUE_TERRACOTTA) return EnumDyeColor.LIGHT_BLUE;
            if (terracottaBlock == Blocks.YELLOW_TERRACOTTA) return EnumDyeColor.YELLOW;
            if (terracottaBlock == Blocks.LIME_TERRACOTTA) return EnumDyeColor.LIME;
            if (terracottaBlock == Blocks.PINK_TERRACOTTA) return EnumDyeColor.PINK;
            if (terracottaBlock == Blocks.GRAY_TERRACOTTA) return EnumDyeColor.GRAY;
            if (terracottaBlock == Blocks.CYAN_TERRACOTTA) return EnumDyeColor.CYAN;
            if (terracottaBlock == Blocks.PURPLE_TERRACOTTA) return EnumDyeColor.PURPLE;
            if (terracottaBlock == Blocks.BLUE_TERRACOTTA) return EnumDyeColor.BLUE;
            if (terracottaBlock == Blocks.BROWN_TERRACOTTA) return EnumDyeColor.BROWN;
            if (terracottaBlock == Blocks.GREEN_TERRACOTTA) return EnumDyeColor.GREEN;
            if (terracottaBlock == Blocks.RED_TERRACOTTA) return EnumDyeColor.RED;
            if (terracottaBlock == Blocks.BLACK_TERRACOTTA) return EnumDyeColor.BLACK;
        } else if (delegate.getItem() instanceof BlockItem && ((BlockItem) delegate.getItem()).getBlock() instanceof ConcretePowderBlock terracottaBlock) {
            if (terracottaBlock == Blocks.WHITE_WOOL) return EnumDyeColor.WHITE;
            if (terracottaBlock == Blocks.ORANGE_WOOL) return EnumDyeColor.ORANGE;
            if (terracottaBlock == Blocks.MAGENTA_WOOL) return EnumDyeColor.MAGENTA;
            if (terracottaBlock == Blocks.LIGHT_BLUE_WOOL) return EnumDyeColor.LIGHT_BLUE;
            if (terracottaBlock == Blocks.YELLOW_WOOL) return EnumDyeColor.YELLOW;
            if (terracottaBlock == Blocks.LIME_WOOL) return EnumDyeColor.LIME;
            if (terracottaBlock == Blocks.PINK_WOOL) return EnumDyeColor.PINK;
            if (terracottaBlock == Blocks.GRAY_WOOL) return EnumDyeColor.GRAY;
            if (terracottaBlock == Blocks.LIGHT_GRAY_WOOL) return EnumDyeColor.SILVER;
            if (terracottaBlock == Blocks.CYAN_WOOL) return EnumDyeColor.CYAN;
            if (terracottaBlock == Blocks.PURPLE_WOOL) return EnumDyeColor.PURPLE;
            if (terracottaBlock == Blocks.BLUE_WOOL) return EnumDyeColor.BLUE;
            if (terracottaBlock == Blocks.BROWN_WOOL) return EnumDyeColor.BROWN;
            if (terracottaBlock == Blocks.GREEN_WOOL) return EnumDyeColor.GREEN;
            if (terracottaBlock == Blocks.RED_WOOL) return EnumDyeColor.RED;
            if (terracottaBlock == Blocks.BLACK_WOOL) return EnumDyeColor.BLACK;
            // dont care about conrete or powder concrete, since they didn't exist in 1.8. maybe change this impl later with smth faster.
        }
        return null;
    }

    @Override
    public int getCount() {
        return delegate.getCount();
    }
}
