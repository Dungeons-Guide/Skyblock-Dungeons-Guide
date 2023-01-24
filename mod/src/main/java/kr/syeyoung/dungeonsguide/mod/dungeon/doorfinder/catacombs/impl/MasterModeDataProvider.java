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

package kr.syeyoung.dungeonsguide.mod.dungeon.doorfinder.catacombs.impl;

import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.dungeon.doorfinder.catacombs.CatacombsDataProvider;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bossfight.BossfightProcessor;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bossfight.BossfightProcessorLivid;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

public class MasterModeDataProvider extends CatacombsDataProvider {


    @Override
    public BossfightProcessor createBossfightProcessor(World w, String dungeonName) {
        String floor = dungeonName.substring(14).trim();
        ChatTransmitter.sendDebugChat(new ChatComponentText("Floor: Master mode " +floor+ " Building boss fight processor"));
        if (floor.equals("M5")) {
            return new BossfightProcessorLivid(true);
        }
        return null;
    }

    @Override
    public boolean isTrapSpawn(String dungeonName) {
        String floor = dungeonName.substring(14).trim();
        switch (floor) {
            case "M3":
            case "M4":
            case "M5":
            case "M6":
                return true;
            default:
                return floor.equals("M7");
        }
    }

    @Override
    public double secretPercentage(String dungeonName) {
        return 1.0;
    }

    @Override
    public int speedSecond(String dungeonName) {
        return 480;
    }
}
