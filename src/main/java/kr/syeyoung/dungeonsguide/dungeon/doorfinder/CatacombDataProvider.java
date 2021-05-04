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

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.roomprocessor.bossfight.*;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import javax.vecmath.Vector2d;
import java.util.Collection;
import java.util.Set;

public class CatacombDataProvider implements DungeonSpecificDataProvider {

    private static final Set<Vector2d> directions = Sets.newHashSet(new Vector2d(0,1), new Vector2d(0, -1), new Vector2d(1, 0), new Vector2d(-1 , 0));

    @Override
    public BlockPos findDoor(World w, String dungeonName) {
        Collection<EntityArmorStand> armorStand = w.getEntities(EntityArmorStand.class, new Predicate<EntityArmorStand>() {
            @Override
            public boolean apply(EntityArmorStand input) {
                return input.getName().equals("§bMort");
            }
        });

        if (armorStand.size() != 0) {
            EntityArmorStand mort = armorStand.iterator().next();
            BlockPos pos = mort.getPosition();
            pos = pos.add(0, 3, 0);
            for (int i = 0; i < 5; i++) {
                for (Vector2d vector2d:directions) {
                    BlockPos test = pos.add(vector2d.x * i, 0, vector2d.y * i);
                    if (w.getChunkFromBlockCoords(test).getBlock(test) == Blocks.iron_bars) {
                        return pos.add(vector2d.x * (i + 2), -2, vector2d.y * (i+2));
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Vector2d findDoorOffset(World w, String dungeonName) {
        Collection<EntityArmorStand> armorStand = w.getEntities(EntityArmorStand.class, new Predicate<EntityArmorStand>() {
            @Override
            public boolean apply(EntityArmorStand input) {
                return input.getName().equals("§bMort");
            }
        });

        if (armorStand.size() != 0) {
            EntityArmorStand mort = armorStand.iterator().next();
            BlockPos pos = mort.getPosition();
            pos = pos.add(0, 3, 0);
            for (int i = 0; i < 5; i++) {
                for (Vector2d vector2d:directions) {
                    BlockPos test = pos.add(vector2d.x * i, 0, vector2d.y * i);
                    if (w.getChunkFromBlockCoords(test).getBlock(test) == Blocks.iron_bars) {
                        return vector2d;
                    }
                }
            }
        }
        return null;
    }
    /*
     *
    * */

    @Override
    public BossfightProcessor createBossfightProcessor(World w, String dungeonName) {
        String floor = dungeonName.substring(14).trim();
        e.sendDebugChat(new ChatComponentText("Floor: "+floor+ " Building bossfight processor"));
        if (floor.equals("F1")) {
            return new BossfightProcessorBonzo();
        } else if (floor.equals("F2")) {
            return new BossfightProcessorScarf();
        } else if (floor.equals("F3")) {
            return new BossfightProcessorProf();
        } else if (floor.equals("F4")) {
            return new BossfightProcessorThorn();
        } else if (floor.equals("F5")) {
            return new BossfightProcessorLivid();
        } else if (floor.equals("F6")) {
            return new BossfightProcessorSadan();
        } else if (floor.equals("F7")) {
            return new BossfightProcessorNecron();
        }
        return null;
    }

    @Override
    public boolean isTrapSpawn(String dungeonName) {
        String floor = dungeonName.substring(14).trim();
        if (floor.equals("F3")) {
            return true;
        } else if (floor.equals("F4")) {
            return true;
        } else if (floor.equals("F5")) {
            return true;
        } else if (floor.equals("F6")) {
            return true;
        } else return floor.equals("F7");
    }
}
