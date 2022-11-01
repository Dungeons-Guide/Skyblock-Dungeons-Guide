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

package kr.syeyoung.dungeonsguide.mod.dungeon.doorfinder;

import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bossfight.BossfightProcessor;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import javax.vecmath.Vector2d;

public interface DungeonSpecificDataProvider {

    BlockPos findDoor(World w, String dungeonName);

    Vector2d findDoorOffset(World w, String dungeonName);
    BossfightProcessor createBossfightProcessor(World w, String dungeonName);

    boolean isTrapSpawn(String dungeonName);

    double secretPercentage(String dungeonName);

    int speedSecond(String dungeonName);
}