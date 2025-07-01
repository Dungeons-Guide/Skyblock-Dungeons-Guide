/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2022  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.dungeon.dataprovider.catacombs;

import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.mod.dungeon.dataprovider.DungeonSpecificDataProvider;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.entity.EntityType;
import kr.syeyoung.modapi.entity.UEntity;
import kr.syeyoung.modapi.entity.UEntityArmorStand;
import kr.syeyoung.modapi.world.BlockType;
import kr.syeyoung.modapi.world.UWorld;
import org.jetbrains.annotations.Nullable;

import javax.vecmath.Vector2d;
import java.util.List;
import java.util.Set;

public abstract class CatacombsDataProvider implements DungeonSpecificDataProvider {

    private static final Set<Vector2d> directions = Sets.newHashSet(new Vector2d(0,1), new Vector2d(0, -1), new Vector2d(1, 0), new Vector2d(-1 , 0));

    @Nullable
    static Vector2d getVector2d(UWorld w, UEntityArmorStand mort, Set<Vector2d> directions) {
        VectorI3D pos = mort.getPosition();
        pos = pos.add(0, 3, 0);
        for (int i = 0; i < 5; i++) {
            for (Vector2d vector2d: directions) {
                VectorI3D test = pos.add((int) (vector2d.x * i), 0, (int) (vector2d.y * i));
                if (w.getBlockStateAt(test).isOf(BlockType.IRON_BARS)) {
                    return vector2d;
                }
            }
        }
        return null;
    }

    public static UEntityArmorStand getMorts(UWorld w){
        List<UEntity> uEntityList = w.getEntities(EntityType.ARMOR_STAND);
        for (UEntity uEntity : uEntityList) {
            if (uEntity.getName().equals("§bMort")) return (UEntityArmorStand) uEntity;
        }
        return null;
    }

    /**
     * This gets all Mort's checks for iron bars near him
     * and based on iron bars determine the door location
     *
     * @param w World that we are going to look for the door in
     *          world is explicitly specified instead of mc.theWorld bc we can use cached worlds
     * @param dungeonName dungeon type e.g. master mode, currently unused
     * @return Block pos of the dungeon entrance
     */
    public VectorI3D findDoor(UWorld w, String dungeonName) {
        UEntityArmorStand armorStand = getMorts(w);

        if (armorStand != null) {
            VectorI3D pos = armorStand.getPosition();
            pos = pos.add(0, 3, 0);
            for (int i = 0; i < 5; i++) {
                for (Vector2d vector2d:directions) {
                    VectorI3D test = pos.add((int) (vector2d.x * i), 0, (int) (vector2d.y * i));
                    if (w.getBlockStateAt(test).isOf(BlockType.IRON_BARS)) {
                        return new VectorI3D(pos.getX(), pos.getY(), pos.getZ()).add((int) (vector2d.x * (i + 2)), -2, (int) (vector2d.y * (i+2)));
                    }
                }
            }
        }
        return null;
    }

    public Vector2d findDoorOffset(UWorld w, String dungeonName) {
        UEntityArmorStand armorStand = getMorts(w);

        if (armorStand != null) {
            return getVector2d(w, armorStand, directions);
        }
        return null;
    }
}
