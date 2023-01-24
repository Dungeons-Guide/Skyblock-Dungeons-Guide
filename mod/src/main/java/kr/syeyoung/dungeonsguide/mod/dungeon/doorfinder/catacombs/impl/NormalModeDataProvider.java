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
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bossfight.*;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

public class NormalModeDataProvider extends CatacombsDataProvider {


    @Override
    public BossfightProcessor createBossfightProcessor(World w, String dungeonName) {
        String floor = dungeonName.substring(14).trim();
        ChatTransmitter.sendDebugChat(new ChatComponentText("Floor: " +floor+ " Building boss fight processor"));
        switch (floor) {
            case "F1":
                return new BossfightProcessorBonzo();
            case "F2":
                return new BossfightProcessorScarf();
            case "F3":
                return new BossfightProcessorProf();
            case "F4":
                return new BossfightProcessorThorn();
            case "F5":
                return new BossfightProcessorLivid(false);
            case "F6":
                return new BossfightProcessorSadan();
            case "F7":
                return new BossfightProcessorNecron();
            default:
                return null;
        }
    }

    @Override
    public boolean isTrapSpawn(String dungeonName) {
        String floor = dungeonName.substring(14).trim();
        switch (floor) {
            case "F3":
            case "F4":
            case "F5":
            case "F6":
                return true;
            default:
                return floor.equals("F7");
        }
    }

    @Override
    public double secretPercentage(String dungeonName) {
        String floor = dungeonName.substring(14).trim();
        switch (floor) {
            case "F1":
            case "E":
                return 0.3;
            case "F2":
                return 0.4;
            case "F3":
                return 0.5;
            case "F4":
                return 0.6;
            case "F5":
                return 0.7;
            case "F6":
                return 0.85;
            default:
                return 1.0;
        }
    }

    @Override
    public int speedSecond(String dungeonName) {
        String floor = dungeonName.substring(14).trim();
        switch (floor) {
            case "F5":
            case "F7":
                return 720;
            default:
                return 600;
        }
    }
}
