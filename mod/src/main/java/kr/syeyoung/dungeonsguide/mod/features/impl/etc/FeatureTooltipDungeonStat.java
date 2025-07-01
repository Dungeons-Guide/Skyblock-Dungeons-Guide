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

package kr.syeyoung.dungeonsguide.mod.features.impl.etc;


import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.modapi.event.events.ItemTooltipEvent;
import kr.syeyoung.modapi.item.UItemStack;
import net.kyori.adventure.nbt.CompoundBinaryTag;

public class FeatureTooltipDungeonStat extends SimpleFeature {
    public FeatureTooltipDungeonStat() {
        super("Misc", "Dungeon Item Stats", "Shows quality of dungeon items (floor, percentage)", "tooltip.dungeonitem");
    }

    @DGEventHandler
    public void onTooltip(ItemTooltipEvent event) {
        UItemStack hoveredItem = event.getItemStack();
        CompoundBinaryTag compound = hoveredItem.getSkyblockAttrib();
        if (compound == null) return;

        int floor = compound.getInt("item_tier");
        int percentage = compound.getInt("baseStatBoostPercentage");

        if (compound.keySet().contains("item_tier"))
            event.toolTip.add("§7Obtained in: §c"+(floor == 0 ? "Entrance" : "Floor "+floor));
        if (compound.keySet().contains("baseStatBoostPercentage"))
            event.toolTip.add("§7Stat Percentage: §"+(percentage == 50 ? "6§l":"c")+(percentage * 2)+"%");
    }
}
