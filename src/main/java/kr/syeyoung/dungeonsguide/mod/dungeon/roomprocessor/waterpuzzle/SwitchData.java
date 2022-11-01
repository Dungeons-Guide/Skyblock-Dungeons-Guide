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

package kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.waterpuzzle;

import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.waterpuzzle.nodes.WaterNodeStart;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.waterpuzzle.nodes.WaterNodeToggleable;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

@Data
@AllArgsConstructor
public class SwitchData {
    private WaterBoard waterBoard;

    private BlockPos switchLoc;
    private BlockPos blockLoc;

    private String blockId;

    public boolean getCurrentState(World w) {
        WaterNode waterNode = waterBoard.getToggleableMap().get(blockId);
        if (waterNode instanceof WaterNodeStart)
            return ((WaterNodeStart) waterNode).isTriggered(w);
        else if (waterNode instanceof WaterNodeToggleable)
            return ((WaterNodeToggleable) waterNode).isTriggered(w);
        return false;
    }
}
