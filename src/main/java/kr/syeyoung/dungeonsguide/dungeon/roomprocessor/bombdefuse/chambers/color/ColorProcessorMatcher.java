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

package kr.syeyoung.dungeonsguide.dungeon.roomprocessor.bombdefuse.chambers.color;

import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.bombdefuse.RoomProcessorBombDefuseSolver;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.bombdefuse.chambers.BDChamber;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.bombdefuse.chambers.BombDefuseChamberGenerator;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.bombdefuse.chambers.ChamberProcessor;
import net.minecraft.init.Blocks;

public class ColorProcessorMatcher implements BombDefuseChamberGenerator {
    @Override
    public boolean match(BDChamber left, BDChamber right) {
        return
                right.getBlock(0,1,1).getBlock() == Blocks.stonebrick &&
                right.getBlock(0,1,2).getBlock() == Blocks.stonebrick &&
                right.getBlock(0,1,3).getBlock() == Blocks.stonebrick;
    }

    @Override
    public String getName() {
        return "colorMatch";
    }
    @Override
    public ChamberProcessor createLeft(BDChamber left, RoomProcessorBombDefuseSolver solver) {
        return new ColorLeftProcessor(solver,left);
    }

    @Override
    public ChamberProcessor createRight(BDChamber right, RoomProcessorBombDefuseSolver solver) {
        return new ColorRightProcessor(solver,right);
    }
}
