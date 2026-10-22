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



import kr.syeyoung.dungeonsguide.mod.utils.SkyblockUtils;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import kr.syeyoung.dungeonsguide.mod.events.impl.ItemTooltipEvent;

public class FeatureTooltipDungeonStat extends SimpleFeature {
    public FeatureTooltipDungeonStat() {
        super("Misc", "Dungeon Item Stats", "Shows quality of dungeon items (floor, percentage)", "tooltip.dungeonitem");
    }

    @DGEventHandler
    public void onTooltip(ItemTooltipEvent event) {
        

        ItemStack hoveredItem = event.itemStack;
        // 1.8.9's ExtraAttributes nbt, custom_data on 26.1 (D125)
        CompoundTag nbtTagCompound = SkyblockUtils.getExtraAttributes(hoveredItem);
        if (nbtTagCompound.isEmpty())
            return;

        int floor = nbtTagCompound.getIntOr("item_tier", 0);
        int percentage = nbtTagCompound.getIntOr("baseStatBoostPercentage", 0);

        if (nbtTagCompound.contains("item_tier"))
            event.toolTip.add("§7Obtained in: §c"+(floor == 0 ? "Entrance" : "Floor "+floor));
        if (nbtTagCompound.contains("baseStatBoostPercentage"))
            event.toolTip.add("§7Stat Percentage: §"+(percentage == 50 ? "6§l":"c")+(percentage * 2)+"%");
    }
}
