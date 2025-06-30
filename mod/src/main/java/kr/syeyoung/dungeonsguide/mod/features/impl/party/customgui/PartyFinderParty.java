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
import kr.syeyoung.modapi.item.UItemStack;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

public class PartyFinderParty {
    public UItemStack itemStack;
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

    public static PartyFinderParty fromItemStack(UItemStack itemStack) {
        if (itemStack.getLore().isEmpty()) return new PartyFinderParty();


        PartyFinderParty party =fromLore(itemStack.getDisplayName(), itemStack.getLore());
        party.itemStack = itemStack;
        return party;
    }
    private static PartyFinderParty fromLore(String name, List<String> lore) {
        PartyFinderParty party = new PartyFinderParty();
        for (int i = 0; i < lore.size(); i++) {
            String line = lore.get(i);
            if (line.startsWith("§7§7Note: ")) {
                party.note = line.substring(10).trim();
                if (i + 1 < lore.size()) {
                    String nextLine = lore.get(i + 1);
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
                String clazzLv;
                if (!playerClazzData.contains(" ")){
                    clazzLv = playerClazzData.split(" ")[1];
                    clazzLv = clazzLv.replace("(", "").replace(")", "");
                } else {
                    clazzLv = "0";
                }
                party.members.add(new MemberData(playerName, clazz, Integer.parseInt(clazzLv)));
            }
        }
        party.leader = name.split("'")[0];
        return party;
    }
}
