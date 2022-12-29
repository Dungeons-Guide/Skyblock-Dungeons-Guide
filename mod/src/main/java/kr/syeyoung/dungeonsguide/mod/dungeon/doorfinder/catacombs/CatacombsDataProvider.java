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

package kr.syeyoung.dungeonsguide.mod.dungeon.doorfinder.catacombs;

import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.mod.dungeon.doorfinder.DungeonSpecificDataProvider;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import javax.vecmath.Vector2d;
import java.util.Collection;
import java.util.Set;

public abstract class CatacombsDataProvider implements DungeonSpecificDataProvider {

    private static final Set<Vector2d> directions = Sets.newHashSet(new Vector2d(0,1), new Vector2d(0, -1), new Vector2d(1, 0), new Vector2d(-1 , 0));

    @Nullable
    static Vector2d getVector2d(World w, Collection<EntityArmorStand> armorStand, Set<Vector2d> directions) {
        EntityArmorStand mort = armorStand.iterator().next();
        BlockPos pos = mort.getPosition();
        pos = pos.add(0, 3, 0);
        for (int i = 0; i < 5; i++) {
            for (Vector2d vector2d: directions) {
                BlockPos test = pos.add(vector2d.x * i, 0, vector2d.y * i);
                if (w.getChunkFromBlockCoords(test).getBlock(test) == Blocks.iron_bars) {
                    return vector2d;
                }
            }
        }
        return null;
    }

    public static Collection<EntityArmorStand> getMorts(World w){
        return w.getEntities(EntityArmorStand.class, input -> input.getName().equals("§bMort"));
    }

    /**
     * This gets all morts checks for iron bars near him
     * and based on iron bars determine the door location
     *
     * @param w World that we are going to look for the door in
     *          world is explicitly specified instead of mc.theWorld bc we can use cached worlds
     * @param dungeonName dungeon type eg master mode, currently unused
     * @return Block pos of the dungeon entrance
     */
    public BlockPos findDoor(World w, String dungeonName) {
        Collection<EntityArmorStand> armorStand = getMorts(w);

        if (!armorStand.isEmpty()) {
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

    public Vector2d findDoorOffset(World w, String dungeonName) {
        Collection<EntityArmorStand> armorStand = getMorts(w);

        if (!armorStand.isEmpty()) {
            return getVector2d(w, armorStand, directions);
        }
        return null;
    }
}
