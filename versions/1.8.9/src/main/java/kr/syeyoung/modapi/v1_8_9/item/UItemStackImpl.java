package kr.syeyoung.modapi.v1_8_9.item;

import com.mojang.authlib.GameProfile;
import kr.syeyoung.modapi.item.Item;
import kr.syeyoung.modapi.item.UItemStack;
import lombok.Getter;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;

public class UItemStackImpl implements UItemStack {
    @Getter
    private ItemStack delegate;

    public UItemStackImpl(ItemStack delegate) {
        this.delegate = delegate;
    }

    public Item getItem() {
        if (delegate.getItem() == Items.skull) return Item.SKULL;
        if (delegate.getItem() == Items.dye) return Item.DYE;
        if (delegate.getItem() == Items.map ) return Item.MAP;
        if (delegate.getItem() == Items.filled_map) return Item.FILLED_MAP;
        if (delegate.getItem() == Items.arrow) return Item.ARROW;
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
}
