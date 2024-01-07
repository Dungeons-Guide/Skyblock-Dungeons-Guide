/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2024  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.features.impl.party.customgui;

import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.ArrayList;
import java.util.List;

public class PartyFinderParty {
    public ItemStack itemStack;
    public String leader = "";
    public List<MemberData> members = new ArrayList<>();

    public String note = "";
    public boolean canJoin = true;
    public int requiredDungeonLevel = 0;
    public int requiredClassLevel = 0;

    @Data @AllArgsConstructor
    public static class MemberData {
        private String name;
        private String clazz;
        private int classLv;
    }

    public static PartyFinderParty fromItemStack(ItemStack itemStack) {
        PartyFinderParty party = fromItemNbt(itemStack.getTagCompound());
        party.itemStack = itemStack;
        return party;
    }

    private static PartyFinderParty fromItemNbt(NBTTagCompound stackTagCompound) {
        if (stackTagCompound != null && stackTagCompound.hasKey("display", 10)) {
            NBTTagCompound nbttagcompound = stackTagCompound.getCompoundTag("display");
            String name = nbttagcompound.getString("Name");

            if (nbttagcompound.getTagId("Lore") == 9) {
                NBTTagList nbttaglist1 = nbttagcompound.getTagList("Lore", 8);
                return fromLore(name, nbttaglist1);
            }
        }
        return new PartyFinderParty();
    }

    private static PartyFinderParty fromLore(String name, NBTTagList tagList) {
        PartyFinderParty party = new PartyFinderParty();
        for (int i = 0; i < tagList.tagCount(); i++) {
            String line = tagList.getStringTagAt(i);
            if (line.startsWith("§7§7Note:")) {
                party.note = line.substring(12).trim();
                if (i + 1 < tagList.tagCount()) {
                    String nextLine = tagList.getStringTagAt(i + 1);
                    if (!nextLine.contains("§7") && !nextLine.replaceAll("§.| ", "").isEmpty()) {
                        i++;
                        party.note += " " + nextLine;
                    }
                }
            } else if (line.startsWith("§cRequires ")) {
                party.canJoin = false;
            } else if (line.startsWith("§7Dungeon Level Required: §b")) {
                party.requiredDungeonLevel = Integer.parseInt(line.substring(28));
            } else if (line.startsWith("§7Class Level Required: §b")) {
                party.requiredClassLevel = Integer.parseInt(line.substring(26));
            } else if (line.startsWith(" ") && line.contains(":")) {
                String dungeonClass = TextUtils.stripColor(line).trim();
                String playerName = dungeonClass.split(":")[0].trim();
                String playerClazzData = dungeonClass.split(":")[1].trim();
                String clazz = playerClazzData.split(" ")[0];
                String clazzLv = playerClazzData.split(" ")[1];
                clazzLv = clazzLv.replace("(", "").replace(")", "");
                party.members.add(new MemberData(playerName, clazz, Integer.parseInt(clazzLv)));
            }
        }
        party.leader = name.split("'")[0];
        return party;
    }
}
