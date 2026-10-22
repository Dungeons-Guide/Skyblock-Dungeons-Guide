/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2022  cyoung06 (syeyoung)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview;

import com.mojang.authlib.GameProfile;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.SkinFetcher;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.playerprofile.PlayerProfile;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;

// TODO: D124, only drawn by the 3D player preview, which is stubbed. The profile is kept; armor, held item, skin and
// the render-only overrides of 1.8.9 (below) come back with the previews
public class FakePlayer extends RemotePlayer {
    @Setter
    @Getter
    private PlayerProfile skyblockProfile;

    private final SkinFetcher.SkinSet skinSet;

    public FakePlayer(GameProfile playerProfile, SkinFetcher.SkinSet skinSet, PlayerProfile skyblockProfile) {
        super(Minecraft.getInstance().level, playerProfile);
        this.skyblockProfile = skyblockProfile;
        this.skinSet = skinSet;
    }

/*

    @Setter
    @Getter
    private PlayerProfile skyblockProfile;

    public void setSkyblockProfile(PlayerProfile skyblockProfile) {
        this.skyblockProfile = skyblockProfile;

        armor = skyblockProfile.getCurrentArmor();
        if (skyblockProfile.getCurrentArmor() != null)
            this.inventory.armorInventory = skyblockProfile.getCurrentArmor().getArmorSlots();
        else
            this.inventory.armorInventory = new ItemStack[4];

        int highestDungeonScore = Integer.MIN_VALUE;
        this.inventory.mainInventory[0] = null;
        if (skyblockProfile.getInventory() != null) {
            ItemStack highestItem = null;
            for (ItemStack itemStack : skyblockProfile.getInventory()) {
                if (itemStack == null) continue;
                CompoundTag display = itemStack.getTagCompound().getCompoundTag("display");
                if (display == null) continue;
                ListTag nbtTagList = display.getTagList("Lore", 8);
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

    private final SkinFetcher.SkinSet skinSet;
    private PlayerProfile.Armor armor;

    private FakePlayer(World w) {
        super(w, null);
        throw new UnsupportedOperationException("what");
    }

    public FakePlayer(GameProfile playerProfile, SkinFetcher.SkinSet skinSet, PlayerProfile skyblockProfile) {
        super(Minecraft.getInstance().level, playerProfile);
        this.skyblockProfile = skyblockProfile;
        this.skinSet = skinSet;
        armor = skyblockProfile.getCurrentArmor();
        this.inventory.armorInventory = skyblockProfile.getCurrentArmor().getArmorSlots();

        int highestDungeonScore = Integer.MIN_VALUE;
        if (skyblockProfile.getInventory() != null) {
            ItemStack highestItem = null;
            for (ItemStack itemStack : skyblockProfile.getInventory()) {
                if (itemStack == null) continue;
                CompoundTag display = itemStack.getTagCompound().getCompoundTag("display");
                if (display == null) continue;
                ListTag nbtTagList = display.getTagList("Lore", 8);
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

    public Identifier getLocationSkin() {
        return Objects.firstNonNull(skinSet.getSkinLoc(), DefaultPlayerSkin.getDefaultSkin(getGameProfile().getId()));
    }

    public Identifier getLocationCape() {
        return skinSet.getCapeLoc();
    }

    @Override
    public ItemStack[] getInventory() {
        return this.inventory.armorInventory;
    }

    @Override
    public boolean isInvisibleToPlayer(Player player) {
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
*/
}
