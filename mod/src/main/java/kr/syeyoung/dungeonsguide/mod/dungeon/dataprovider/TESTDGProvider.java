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

package kr.syeyoung.dungeonsguide.mod.dungeon.dataprovider;

import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bossfight.BossfightProcessor;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.world.UWorld;

import javax.vecmath.Vector2d;

public class TESTDGProvider implements DungeonSpecificDataProvider {
        @Override
        public VectorI3D findDoor(UWorld w, String dungeonName) {
            return new VectorI3D(0, 0, 0);
        }

        @Override
        public Vector2d findDoorOffset(UWorld w, String dungeonName) {
            return new Vector2d(0,0);
        }

        @Override
        public BossfightProcessor createBossfightProcessor(UWorld w, String dungeonName) {
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
