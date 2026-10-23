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

package kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor;

import kr.syeyoung.dungeonsguide.mod.dungeon.data.LegacyBlock;

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import kr.syeyoung.dungeonsguide.mod.dungeon.world.DGWorld;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class RoomProcessorCreeperSolver extends GeneralRoomProcessor {

    private final List<BlockPos[]> poses = new ArrayList<BlockPos[]>();

    private final boolean bugged = false;

    public RoomProcessorCreeperSolver(DungeonRoom dungeonRoom) {
        super(dungeonRoom);

        findCreeperAndDoPoses();
    }

    private boolean check(AABB axis, Vec3 vec) {
        if (vec == null) return false;
        return axis.contains(vec);
    }

    private void findCreeperAndDoPoses() {
        DGWorld w = getDungeonRoom().getContext().getWorld();
        List<BlockPos> prismarines = new ArrayList<BlockPos>();
        final BlockPos low = getDungeonRoom().getRoomBounds().getMin().offset(0,-2,0);
        final BlockPos high = getDungeonRoom().getRoomBounds().getMax().offset(0,20,0);
        final AABB axis = new AABB(
                low.getX() + 17, low.getY() + 7, low.getZ() + 17,
                low.getX() + 16, low.getY() + 10.5, low.getZ() + 16
        );

        for (BlockPos pos : BlockPos.betweenClosed(low, high)) {
            BlockState b = DungeonsGuide.getDungeonsGuide().getBlockCache().getBlockState(pos);
            if (LegacyBlock.PRISMARINE.is(b) || LegacyBlock.SEA_LANTERN.is(b)) {
                for (Direction face:Direction.values()) {
                    if (LegacyBlock.AIR.is(w.getBlockState(pos.relative(face)))) {
                        // betweenClosed reuses one mutable pos, 1.8.9's getAllInBox didn't
                        prismarines.add(pos.immutable());
                        break;
                    }
                }
            }
        }
        double offset = 0.1;

        while (prismarines.size() > 1) {
            BlockPos first = prismarines.get(0);
            BlockPos highestMatch = null;
            int highestDist = 0;
            label: for (int i = 1; i  < prismarines.size(); i++) {
                BlockPos second = prismarines.get(i);

                if (second.distSqr(first) < highestDist) continue;

                Vec3 startLoc = Vec3.atLowerCornerOf(first).add(0.5,0.5,0.5);
                Vec3 dest = Vec3.atLowerCornerOf(second).add(0.5,0.5,0.5);
                if (check(axis, getIntermediateWithValue(startLoc, dest, Direction.Axis.Y, axis.minY+offset)) ||
                        check(axis, getIntermediateWithValue(startLoc, dest, Direction.Axis.Y, axis.maxY-offset)) ||
                        check(axis, getIntermediateWithValue(startLoc, dest, Direction.Axis.X, axis.minX+offset)) ||
                        check(axis, getIntermediateWithValue(startLoc, dest, Direction.Axis.X, axis.maxX-offset)) ||
                        check(axis, getIntermediateWithValue(startLoc, dest, Direction.Axis.Z, axis.minZ+offset)) ||
                        check(axis, getIntermediateWithValue(startLoc, dest, Direction.Axis.Z, axis.maxZ-offset))) {
                    highestDist = (int) second.distSqr(first);
                    highestMatch = second;
                }

            }


            if (highestMatch == null) {
                prismarines.remove(first);
            } else {
                prismarines.remove(first);
                prismarines.remove(highestMatch);
                poses.add(new BlockPos[] {first, highestMatch});
            }
        }
    }

    // 1.8.9's Vec3.getIntermediateWithXValue / Y / Z: the point on from -> to where the axis has that value
    private static Vec3 getIntermediateWithValue(Vec3 from, Vec3 to, Direction.Axis axis, double value) {
        double d = to.get(axis) - from.get(axis);
        if (d * d < 1.0000000116860974E-7) return null;
        double t = (value - from.get(axis)) / d;
        if (t < 0.0 || t > 1.0) return null;
        return new Vec3(from.x + (to.x - from.x) * t, from.y + (to.y - from.y) * t, from.z + (to.z - from.z) * t);
    }

    @Override
    public void tick() {
        super.tick();
        if (bugged) {
            findCreeperAndDoPoses();
        }
    }

    private static final Color[] colors = new Color[] {Color.red, Color.orange, Color.green, Color.cyan, Color.blue, Color.pink, Color.yellow, Color.darkGray, Color.lightGray};
    @Override
    public void drawWorld(float partialTicks) {
        super.drawWorld(partialTicks);
        if (!FeatureRegistry.SOLVER_CREEPER.isEnabled()) return;
        DGWorld w = getDungeonRoom().getContext().getWorld();
        for (int i = 0; i < poses.size(); i++) {
            BlockPos[] poset = poses.get(i);
            Color color = colors[i % colors.length];
            boolean oneIsConnected = !LegacyBlock.SEA_LANTERN.is(w.getBlockState(poset[0])) &&
                    !LegacyBlock.SEA_LANTERN.is(w.getBlockState(poset[1]));
            RenderUtils.drawLine(new Vec3(poset[0].getX() +0.5, poset[0].getY() +0.5, poset[0].getZ()+0.5),
                    new Vec3(poset[1].getX() +0.5, poset[1].getY() +0.5, poset[1].getZ()+0.5), oneIsConnected ? new Color(0,0,0,50) : color, partialTicks, true);
        }
        final BlockPos low = getDungeonRoom().getRoomBounds().getMin();
        final AABB axis = new AABB(
                low.getX() + 17, low.getY() + 5, low.getZ() + 17,
                low.getX() + 16, low.getY() + 8.5, low.getZ() + 16
        );
        RenderUtils.highlightBox(axis, new Color(0x4400FF00, true), partialTicks, false);
    }

    public static class Generator implements RoomProcessorGenerator<RoomProcessorCreeperSolver> {
        @Override
        public RoomProcessorCreeperSolver createNew(DungeonRoom dungeonRoom) {
            RoomProcessorCreeperSolver defaultRoomProcessor = new RoomProcessorCreeperSolver(dungeonRoom);
            return defaultRoomProcessor;
        }
    }
}
