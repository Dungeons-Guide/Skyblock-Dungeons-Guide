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

package kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers.arrow;

import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.RoomProcessorBombDefuseSolver;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers.BDChamber;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers.BombDefuseChamberGenerator;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers.ChamberProcessor;
import kr.syeyoung.modapi.world.BlockType;

public class ArrowProcessorMatcher implements BombDefuseChamberGenerator {
    @Override
    public boolean match(BDChamber left, BDChamber right) {
        return left.getBlock(8,1,1).isOf(BlockType.WOOD_PLANKS) &&
                left.getBlock(8,1,2).isOf(BlockType.WOOD_PLANKS) &&
                left.getBlock(8,1,3).isOf(BlockType.WOOD_PLANKS) &&
                right.getBlock(0,1,1).isOf(BlockType.WOOD_PLANKS) &&
                right.getBlock(0,1,2).isOf(BlockType.WOOD_PLANKS) &&
                right.getBlock(0,1,3).isOf(BlockType.WOOD_PLANKS);
    }

    @Override
    public String getName() {
        return "arrowMatch";
    }

    @Override
    public ChamberProcessor createLeft(BDChamber left, RoomProcessorBombDefuseSolver solver) {
        return new ArrowLeftProcessor(solver, left);
    }

    @Override
    public ChamberProcessor createRight(BDChamber right, RoomProcessorBombDefuseSolver solver) {
        return new ArrowRightProcessor(solver, right);
    }
}
