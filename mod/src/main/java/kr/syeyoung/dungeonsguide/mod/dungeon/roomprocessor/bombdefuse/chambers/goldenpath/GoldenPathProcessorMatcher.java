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

package kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers.goldenpath;

<<<<<<<< HEAD:mod/src/main/java/kr/syeyoung/dungeonsguide/roomprocessor/bombdefuse/chambers/goldenpath/GoldenPathProcessorMatcher.java
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.RoomProcessorBombDefuseSolver;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.BDChamber;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.BombDefuseChamberGenerator;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.ChamberProcessor;
========
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.RoomProcessorBombDefuseSolver;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers.BDChamber;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers.BombDefuseChamberGenerator;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers.ChamberProcessor;
>>>>>>>> origin/breaking-changes-just-working-im-not-putting-all-of-these-into-3.0-but-for-the-sake-of-beta-release-this-thing-exists:mod/src/main/java/kr/syeyoung/dungeonsguide/mod/dungeon/roomprocessor/bombdefuse/chambers/goldenpath/GoldenPathProcessorMatcher.java
import net.minecraft.init.Blocks;

public class GoldenPathProcessorMatcher implements BombDefuseChamberGenerator {
    @Override
    public boolean match(BDChamber left, BDChamber right) {
        return left.getBlock(4,0,0).getBlock() == Blocks.hardened_clay
                || left.getBlock(4,0,0).getBlock() == Blocks.stained_hardened_clay;
    }

    @Override
    public String getName() {
        return "goldPath";
    }
    @Override
    public ChamberProcessor createLeft(BDChamber left, RoomProcessorBombDefuseSolver solver) {
        return new GoldenPathLeftProcessor(solver, left);
    }

    @Override
    public ChamberProcessor createRight(BDChamber right, RoomProcessorBombDefuseSolver solver) {
        return new GoldenPathRightProcessor(solver, right);
    }
}
