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

package kr.syeyoung.dungeonsguide.mod.dungeon.mocking;

import kr.syeyoung.dungeonsguide.mod.dungeon.doorfinder.DungeonSpecificDataProvider;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bossfight.BossfightProcessor;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import javax.vecmath.Vector2d;

public class TESTDGProvider implements DungeonSpecificDataProvider {
        @Override
        public BlockPos findDoor(World w, String dungeonName) {
            return new BlockPos(0, 0, 0);
        }

        @Override
        public Vector2d findDoorOffset(World w, String dungeonName) {
            return new Vector2d(0,0);
        }

        @Override
        public BossfightProcessor createBossfightProcessor(World w, String dungeonName) {
            return null;
        }

        @Override
        public boolean isTrapSpawn(String dungeonName) {
            return false;
        }

        @Override
        public double secretPercentage(String dungeonName) {
            return 0;
        }

        @Override
        public int speedSecond(String dungeonName) {
            return 0;
        }
}
