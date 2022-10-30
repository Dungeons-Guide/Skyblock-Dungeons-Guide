package kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview;

import com.google.common.base.Objects;
import com.mojang.authlib.GameProfile;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.playerprofile.PlayerProfile;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.SkinFetcher;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class FakePlayer extends EntityOtherPlayerMP {
    @Setter
    @Getter
    private PlayerProfile skyblockProfile;
    private final SkinFetcher.SkinSet skinSet;
    private final PlayerProfile.Armor armor;
    @Getter
    private final int profileNumber;

    private FakePlayer(World w) {
        super(w, null);
        throw new UnsupportedOperationException("what");
    }

    public FakePlayer(GameProfile playerProfile, SkinFetcher.SkinSet skinSet, PlayerProfile skyblockProfile, int profileNumber) {
        super(Minecraft.getMinecraft().theWorld, playerProfile);
        this.profileNumber = profileNumber;
        this.skyblockProfile = skyblockProfile;
        this.skinSet = skinSet;
        armor = skyblockProfile.getCurrentArmor();
        this.inventory.armorInventory = skyblockProfile.getCurrentArmor().getArmorSlots();

        int highestDungeonScore = Integer.MIN_VALUE;
        if (skyblockProfile.getInventory() != null) {
            ItemStack highestItem = null;
            for (ItemStack itemStack : skyblockProfile.getInventory()) {
                if (itemStack == null) continue;
                NBTTagCompound display = itemStack.getTagCompound().getCompoundTag("display");
                if (display == null) continue;
                NBTTagList nbtTagList = display.getTagList("Lore", 8);
                if (nbtTagList == null) continue;
                for (int i = 0; i < nbtTagList.tagCount(); i++) {
                    String str = nbtTagList.getStringTagAt(i);
                    if (TextUtils.stripColor(str).startsWith("Gear")) {
                        int dungeonScore = Integer.parseInt(TextUtils.keepIntegerCharactersOnly(TextUtils.stripColor(str).split(" ")[2]));
                        if (dungeonScore > highestDungeonScore) {
                            highestItem = itemStack;
                            highestDungeonScore = dungeonScore;
                        }
                    }
                }
            }

            this.inventory.mainInventory[0] = highestItem;
            this.inventory.currentItem = 0;
        }
    }

    public String getSkinType() {
        return this.skinSet == null ? DefaultPlayerSkin.getSkinType(getGameProfile().getId()) : this.skinSet.getSkinType();
    }

    public ResourceLocation getLocationSkin() {
        return Objects.firstNonNull(skinSet.getSkinLoc(), DefaultPlayerSkin.getDefaultSkin(getGameProfile().getId()));
    }

    public ResourceLocation getLocationCape() {
        return skinSet.getCapeLoc();
    }

    @Override
    public ItemStack[] getInventory() {
        return this.inventory.armorInventory;
    }

    @Override
    public boolean isInvisibleToPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public Team getTeam() {
        return new ScorePlayerTeam(null, null) {
            @Override
            public EnumVisible getNameTagVisibility() {
                return EnumVisible.NEVER;
            }
        };
    }
}
