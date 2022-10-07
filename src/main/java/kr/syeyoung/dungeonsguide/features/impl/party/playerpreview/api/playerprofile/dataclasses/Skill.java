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

package kr.syeyoung.dungeonsguide.features.impl.party.playerpreview.api.playerprofile.dataclasses;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Skill {
    RUNECRAFTING("runecrafting", "Runecrafting"), COMBAT("combat", "Combat"), MINING("mining", "Mining"), ALCHEMY("alchemy", "Alchemy"), FARMING("farming", "Farming"), TAMING("taming", "Taming"), ENCHANTING("enchanting", "Enchanting"), FISHING("fishing", "Fishing"), FORAGING("foraging", "Foraging"), CARPENTRY("carpentry", "Carpentry");

    private final String jsonName;
    private final String friendlyName;
}
