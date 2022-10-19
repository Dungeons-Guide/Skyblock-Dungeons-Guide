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

package kr.syeyoung.dungeonsguide.dungeon.doorfinder;

import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.bossfight.BossfightProcessor;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.bossfight.BossfightProcessorLivid;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import javax.vecmath.Vector2d;
import java.util.Collection;
import java.util.Set;

public class CatacombMasterDataProvider extends DungeonSpecificDataProvider {

    private static final Set<Vector2d> directions = Sets.newHashSet(new Vector2d(0,1), new Vector2d(0, -1), new Vector2d(1, 0), new Vector2d(-1 , 0));

    @Override
    public Vector2d findDoorOffset(World w, String dungeonName) {
        Collection<EntityArmorStand> armorStand = w.getEntities(EntityArmorStand.class, input -> input.getName().equals("§bMort"));

        if (!armorStand.isEmpty()) {
            return CatacombDataProvider.getVector2d(w, armorStand, directions);
        }
        return null;
    }

    @Override
    public BossfightProcessor createBossfightProcessor(World w, String dungeonName) {
        String floor = dungeonName.substring(14).trim();
        DungeonsGuide.sendDebugChat(new ChatComponentText("Floor: Master mode "+floor+ " Building bossfight processor"));
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
