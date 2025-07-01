package kr.syeyoung.modapi.v1_8_9.item;

import com.mojang.authlib.GameProfile;
import kr.syeyoung.modapi.item.Item;
import kr.syeyoung.modapi.item.UItemStack;
import kr.syeyoung.modapi.util.EnumDyeColor;
import kr.syeyoung.modapi.v1_8_9.util.NBTUtils;
import lombok.Getter;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UItemStackImpl implements UItemStack {
    @Getter
    private ItemStack delegate;

    public UItemStackImpl(ItemStack delegate) {
        this.delegate = delegate;
    }


    public Item getItem() {
        if (delegate.getItem() == Items.skull) return Item.SKULL;
        if (delegate.getItem() == Items.dye) return Item.DYE;
        if (delegate.getItem() == Items.map) return Item.MAP;
        if (delegate.getItem() == Items.filled_map) return Item.FILLED_MAP;
        if (delegate.getItem() == Items.arrow) return Item.ARROW;
        if (delegate.getItem() == Items.stick) return Item.STICK;
        if (delegate.getItem() == Items.golden_axe) return Item.GOLDEN_AXE;
        if (delegate.getItem() == Items.spawn_egg) return Item.SPAWN_EGG;
        if (delegate.getItem() == Items.golden_shovel) return Item.GOLDEN_SHOVEL;
        if (delegate.getItem() == net.minecraft.item.Item.getItemFromBlock(Blocks.bedrock)) return Item.BEDROCK;
        if (delegate.getItem() == net.minecraft.item.Item.getItemFromBlock(Blocks.bookshelf)) return Item.BOOKSHELF;
        if (delegate.getItem() == net.minecraft.item.Item.getItemFromBlock(Blocks.stained_glass_pane)) return Item.STAINED_GLASS_PANE;
        if (delegate.getItem() == net.minecraft.item.Item.getItemFromBlock(Blocks.stained_hardened_clay)) return Item.STAINED_HARDENED_CLAY;
        return Item.UNKNOWN;
    }

    @Override
    public int getMetadata() {
        return delegate.getMetadata();
    }

    @Override
    public String getSkullTexture() {
        if (delegate.getItem() != Items.skull) return null;

        if (delegate.hasTagCompound()) {
            NBTTagCompound nbttagcompound = delegate.getTagCompound();
            if (nbttagcompound.hasKey("SkullOwner", 10)) {
                GameProfile gameprofile = NBTUtil.readGameProfileFromNBT(nbttagcompound.getCompoundTag("SkullOwner"));
                return gameprofile.getProperties().get("textures").stream().findFirst().map(a -> a.getValue()).orElse(null);
            }
        }
        return null;
    }

    @Override
    public CompoundBinaryTag serialize() {
        NBTTagCompound compound = delegate.getTagCompound();
        return NBTUtils.convertNBT(compound);
    }

    @Override
    public CompoundBinaryTag getSkyblockAttrib() {
        NBTTagCompound compound = delegate.getTagCompound();
        if (compound == null)
            return null;
        if (!compound.hasKey("ExtraAttributes"))
            return null;
        NBTTagCompound nbtTagCompound = compound.getCompoundTag("ExtraAttributes");
        return NBTUtils.convertNBT(nbtTagCompound);
    }


    @Override
    public List<String> getLore() {
        NBTTagCompound display = delegate.getTagCompound().getCompoundTag("display");
        if (display == null) return Collections.emptyList();
        NBTTagList nbtTagList = display.getTagList("Lore", 8);
        if (nbtTagList == null) return Collections.emptyList();
        List<String> lore = new ArrayList<>();
        for (int i = 0; i < nbtTagList.tagCount(); i++) {
            String str = nbtTagList.getStringTagAt(i);
            lore.add(str);
        }
        return lore;
    }

    public String getSkyblockId() {
        NBTTagCompound nbt = delegate.getTagCompound();
        NBTTagCompound extra = nbt.getCompoundTag("ExtraAttributes");
        if (extra == null) return null;
        return extra.getString("id");
    }

    @Override
    public String getDisplayName() {
        return delegate.getDisplayName();
    }

    @Override
    public Object getItemStack() {
        return delegate;
    }

    @Override
    public List<String> getNormalTooltip() {
        List<String> tooltip = delegate.getTooltip(Minecraft.getMinecraft().thePlayer, Minecraft.getMinecraft().gameSettings.advancedItemTooltips);
        for (int i = 0; i < tooltip.size(); ++i) {
            if (i == 0) {
                tooltip.set(i, delegate.getRarity().rarityColor + tooltip.get(i));
            } else {
                tooltip.set(i, EnumChatFormatting.GRAY + tooltip.get(i));
            }
        }
        return tooltip;
    }

    public boolean isItemEnchanted() {
        return delegate.isItemEnchanted();
    }

    @Override
    public EnumDyeColor getItemColor() {
        if (delegate.getItem() == Items.dye) return EnumDyeColor.VALUES[15 - delegate.getItemDamage()];
        return EnumDyeColor.VALUES[delegate.getItemDamage()];
    }

    @Override
    public int getCount() {
        return delegate.stackSize;
    }
}
