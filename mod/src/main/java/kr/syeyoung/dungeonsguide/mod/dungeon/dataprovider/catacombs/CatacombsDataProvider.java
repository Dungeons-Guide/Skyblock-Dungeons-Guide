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
import kr.syeyoung.dungeonsguide.mod.dungeon.data.LegacyBlock;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import kr.syeyoung.dungeonsguide.mod.dungeon.world.DGWorld;
import org.jetbrains.annotations.Nullable;

import org.joml.Vector2d;
import java.util.Collection;
import java.util.Set;

public abstract class CatacombsDataProvider implements DungeonSpecificDataProvider {

    private static final Set<Vector2d> directions = Sets.newHashSet(new Vector2d(0,1), new Vector2d(0, -1), new Vector2d(1, 0), new Vector2d(-1 , 0));

    @Nullable
    static Vector2d getVector2d(DGWorld w, Collection<ArmorStand> armorStand, Set<Vector2d> directions) {
        ArmorStand mort = armorStand.iterator().next();
        // TODO: 1.8.9's getPosition() floored (x, y + 0.5, z), (x + 0.5, y + 0.5, z + 0.5) for the local player; blockPosition() floors (x, y, z)
        BlockPos pos = mort.blockPosition();
        pos = pos.offset(0, 3, 0);
        for (int i = 0; i < 5; i++) {
            for (Vector2d vector2d: directions) {
                BlockPos test = BlockPos.containing(pos.getX() + vector2d.x * i, pos.getY(), pos.getZ() + vector2d.y * i);
                if (LegacyBlock.IRON_BARS.is(w.getBlockState(test))) {
                    return vector2d;
                }
            }
        }
        return null;
    }

    public static Collection<ArmorStand> getMorts(DGWorld w){
        // the loaded chunk area around the player (D114)
        Minecraft mc = Minecraft.getInstance();
        ChunkPos center = mc.player.chunkPosition();
        int radius = mc.options.getEffectiveRenderDistance() * 16;
        AABB loaded = new AABB(center.getMinBlockX() - radius, mc.level.getMinY(), center.getMinBlockZ() - radius,
                center.getMaxBlockX() + 1 + radius, mc.level.getMaxY() + 1, center.getMaxBlockZ() + 1 + radius);
        // TODO: D5 entity name stopgap (D116)
        return mc.level.getEntitiesOfClass(ArmorStand.class, loaded, input -> TextUtils.getFormattedText(input.getName()).equals("§bMort"));
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
    public BlockPos findDoor(DGWorld w, String dungeonName) {
        Collection<ArmorStand> armorStand = getMorts(w);

        if (!armorStand.isEmpty()) {
            ArmorStand mort = armorStand.iterator().next();
            // TODO: 1.8.9's getPosition() floored (x, y + 0.5, z), (x + 0.5, y + 0.5, z + 0.5) for the local player; blockPosition() floors (x, y, z)
            BlockPos pos = mort.blockPosition();
            pos = pos.offset(0, 3, 0);
            for (int i = 0; i < 5; i++) {
                for (Vector2d vector2d:directions) {
                    BlockPos test = BlockPos.containing(pos.getX() + vector2d.x * i, pos.getY(), pos.getZ() + vector2d.y * i);
                    if (LegacyBlock.IRON_BARS.is(w.getBlockState(test))) {
                        return BlockPos.containing(pos.getX() + vector2d.x * (i + 2), pos.getY() - 2, pos.getZ() + vector2d.y * (i+2));
                    }
                }
            }
        }
        return null;
    }

    public Vector2d findDoorOffset(DGWorld w, String dungeonName) {
        Collection<ArmorStand> armorStand = getMorts(w);

        if (!armorStand.isEmpty()) {
            return getVector2d(w, armorStand, directions);
        }
        return null;
    }
}
