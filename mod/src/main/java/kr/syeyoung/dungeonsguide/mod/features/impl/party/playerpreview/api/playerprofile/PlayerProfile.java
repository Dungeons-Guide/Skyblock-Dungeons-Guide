/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
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

package kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.playerprofile;

import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.playerprofile.dataclasses.*;
import lombok.Data;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class PlayerProfile {
    private String profileUID;
    private String memberUID;
    private String profileName;

    private boolean selected;

    private int fairySouls;
    private int fairyExchanges;

    private Armor currentArmor;
    private List<Armor> wardrobe = new ArrayList<>();
    private int selectedWardrobe = -1;

    private ItemStack[] inventory;
    private ItemStack[] enderChest;
    private ItemStack[] talismans;

    private int totalSecrets;

    @Data
    public static class Armor {
        private final ItemStack[] armorSlots = new ItemStack[4];

        public ItemStack getHelmet() { return armorSlots[3]; }
        public ItemStack getChestPlate() { return armorSlots[2]; }
        public ItemStack getLeggings() { return armorSlots[1]; }
        public ItemStack getBoots() { return armorSlots[0]; }
    }

    private Map<DungeonType, DungeonSpecificData<DungeonStat>> dungeonStats = new HashMap<>();

    private Map<DungeonClass, ClassSpecificData<PlayerClassData>> playerClassData = new HashMap<>();
    private DungeonClass selectedClass;
    @Data
    public static class PlayerClassData {
        private double experience;
    }

    private Map<Skill, Double> skillXp;

    private List<Pet> pets = new ArrayList<>();


    private Map<String, Object> additionalProperties = new HashMap<>();

    private LilyWeight lilyWeight;

    @Data
    public static class LilyWeight {
        private double skill_base;
        private double skill_overflow;
        private double catacombs_base;
        private double catacombs_master;
        private double catacombs_exp;
        private double slayer;

        public double getTotal() {
            return skill_base + skill_overflow + catacombs_base + catacombs_exp + catacombs_master + slayer;
        }
    }
}