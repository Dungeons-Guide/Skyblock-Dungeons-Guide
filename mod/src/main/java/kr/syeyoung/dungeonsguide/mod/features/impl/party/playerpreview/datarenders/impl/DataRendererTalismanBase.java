/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021, 2023  cyoung06, Linnea Gräf
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
package kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.datarenders.impl;

import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.playerprofile.PlayerProfile;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.datarenders.IDataRenderer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.Optional;

public abstract class DataRendererTalismanBase implements IDataRenderer {
    private static final String TALISMAN_TALLY_CACHE_KEY = "talismanCnt";

    protected Optional<int[]> getTalismanRarityTallies(PlayerProfile playerProfile) {
        boolean apiDisabled = playerProfile.getTalismans() == null || playerProfile.getInventory() == null;
        if (apiDisabled) return Optional.empty();
        if (playerProfile.getAdditionalProperties().containsKey(TALISMAN_TALLY_CACHE_KEY)) {
            return Optional.of((int[]) playerProfile.getAdditionalProperties().get(TALISMAN_TALLY_CACHE_KEY));
        }
        int[] cnts = new int[DataRendererTalismans.Rarity.values().length];
        for (ItemStack talisman : playerProfile.getTalismans()) {
            if (talisman == null) continue;
            DataRendererTalismans.Rarity r = getRarity(talisman);
            if (r != null) cnts[r.ordinal()]++;
        }
        for (ItemStack itemStack : playerProfile.getInventory()) {
            if (itemStack == null) continue;
            DataRendererTalismans.Rarity r = getRarity(itemStack);
            if (r != null) cnts[r.ordinal()]++;
        }
        playerProfile.getAdditionalProperties().put(TALISMAN_TALLY_CACHE_KEY, cnts);
        return Optional.of(cnts);
    }

    private static DataRendererTalismans.Rarity getRarity(ItemStack itemStack) {
        NBTTagCompound display = itemStack.getTagCompound().getCompoundTag("display");
        if (display == null) return null;
        NBTTagList lore = display.getTagList("Lore", 8);
        if (lore == null) return null;
        for (int i = 0; i < lore.tagCount(); i++) {
            String line = lore.getStringTagAt(i);
            for (DataRendererTalismans.Rarity value : DataRendererTalismans.Rarity.values()) {
                if (line.startsWith(value.getColor()) && line.contains("CCESSORY")) return value;
            }
        }
        return null;
    }

    @AllArgsConstructor
    @Getter
    public enum Rarity {
        COMMON("§f", 0, 3),
        UNCOMMON("§a", 1, 5),
        RARE("§9", 2, 8),
        EPIC("§5", 3, 12),
        LEGENDARY("§6", 4, 16),
        MYTHIC("§d", 5, 22);

        private final String color;
        private final int index;
        private final int magicPower;
    }

}