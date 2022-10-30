/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.playerprofile.dataclasses;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum DungeonClass {
    MAGE("mage", "Mage"), ARCHER("archer","Archer"), HEALER("healer", "Healer"), TANK("tank", "Tank"), BERSERK("berserk", "Berserk");


    private final String jsonName;
    private final String familarName;
    private static final Map<String, DungeonClass> jsonNameToClazz = new HashMap<>();
    static {
        for (DungeonClass value : values()) {
            jsonNameToClazz.put(value.getJsonName(), value);
        }
    }

    public static DungeonClass getClassByJsonName(String name) {
        return jsonNameToClazz.get(name);
    }

}
