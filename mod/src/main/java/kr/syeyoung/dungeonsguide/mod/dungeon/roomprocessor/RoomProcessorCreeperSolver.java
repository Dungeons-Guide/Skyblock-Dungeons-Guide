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


import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.modapi.data.AABB;
import kr.syeyoung.modapi.data.Vector3D;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.rendering.UWorldRenderContext;
import kr.syeyoung.modapi.world.BlockType;
import kr.syeyoung.modapi.world.IBlockAccessible;
import kr.syeyoung.modapi.world.UBlockState;
import kr.syeyoung.modapi.world.UWorld;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RoomProcessorCreeperSolver extends GeneralRoomProcessor {

    private final List<VectorI3D[]> poses = new ArrayList<VectorI3D[]>();

    private final boolean bugged = false;

    public RoomProcessorCreeperSolver(DungeonRoom dungeonRoom) {
        super(dungeonRoom);

        findCreeperAndDoPoses();
    }

    private boolean check(AABB axis, Vector3D vec) {
        if (vec == null) return false;
        return axis.isVecInside(vec);
    }

    private void findCreeperAndDoPoses() {
        IBlockAccessible w = getDungeonRoom().getRoomWorld();
        List<VectorI3D> prismarines = new ArrayList<VectorI3D>();
        final VectorI3D low = getDungeonRoom().getRoomBounds().getMin().add(0,-2,0);
        final VectorI3D high = getDungeonRoom().getRoomBounds().getMax().add(0,20,0);
        final AABB axis = new AABB(
                low.getX() + 17, low.getY() + 7, low.getZ() + 17,
                low.getX() + 16, low.getY() + 10.5, low.getZ() + 16
        );

        for (VectorI3D pos : VectorI3D.getAllInBox(low, high)) {
            UBlockState b = getDungeonRoom().getRoomWorld().getBlockStateAt(pos);
            if (b.isOf(BlockType.PRISMARINE, BlockType.SEA_LANTERN)) {
                for (kr.syeyoung.modapi.data.EnumFacing face: kr.syeyoung.modapi.data.EnumFacing.VALUES) {
                    VectorI3D newPos = pos.add(face.getDirectionVec());
                    if (w.getBlockStateAt(newPos).isOf(BlockType.AIR)) {
                        prismarines.add(pos);
                        break;
                    }
                }
            }
        }
        double offset = 0.1;

        while (prismarines.size() > 1) {
            VectorI3D first = prismarines.get(0);
            VectorI3D highestMatch = null;
            int highestDist = 0;
            label: for (int i = 1; i  < prismarines.size(); i++) {
                VectorI3D second = prismarines.get(i);

                if (second.distanceSq(first) < highestDist) continue;

                Vector3D startLoc = new Vector3D(first).add(0.5,0.5,0.5);
                Vector3D dest = new Vector3D(second).add(0.5,0.5,0.5);
                if (check(axis, startLoc.getIntermediateWithYValue(dest, axis.minY+offset)) ||
                        check(axis, startLoc.getIntermediateWithYValue(dest, axis.maxY-offset)) ||
                        check(axis, startLoc.getIntermediateWithXValue(dest, axis.minX+offset)) ||
                        check(axis, startLoc.getIntermediateWithXValue(dest, axis.maxX-offset)) ||
                        check(axis, startLoc.getIntermediateWithZValue(dest, axis.minZ+offset)) ||
                        check(axis, startLoc.getIntermediateWithZValue(dest, axis.maxZ-offset))) {
                    highestDist = (int) second.distanceSq(first);
                    highestMatch = second;
                }

            }


            if (highestMatch == null) {
                prismarines.remove(first);
            } else {
                prismarines.remove(first);
                prismarines.remove(highestMatch);
                poses.add(new VectorI3D[] {first, highestMatch});
            }
        }
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
    public void drawWorld(UWorldRenderContext context, float partialTicks) {
        super.drawWorld(context, partialTicks);
        if (!FeatureRegistry.SOLVER_CREEPER.isEnabled()) return;
        UWorld w = getDungeonRoom().getContext().getWorld();
        for (int i = 0; i < poses.size(); i++) {
            VectorI3D[] poset = poses.get(i);
            Color color = colors[i % colors.length];
            boolean oneIsConnected = !w.getBlockStateAt(poset[0]).isOf(BlockType.SEA_LANTERN) &&
                    !w.getBlockStateAt(poset[1]).isOf(BlockType.SEA_LANTERN);
            context.drawLinesVec3(
                    Arrays.asList(new Vector3D(poset[0]).add(0.5, 0.5, 0.5), new Vector3D(poset[1]).add(0.5, 0.5, 0.5))
                    , oneIsConnected ? 0x32000000 : color.getRGB(), false, 0.0f, 1.0f, partialTicks, true);
        }
        final VectorI3D low = getDungeonRoom().getRoomBounds().getMin();
        final AABB axis = new AABB(
                low.getX() + 17, low.getY() + 5, low.getZ() + 17,
                low.getX() + 16, low.getY() + 8.5, low.getZ() + 16
        );
        context.highlightBox(axis, 0x4400FF00, partialTicks, false);
    }

    public static class Generator implements RoomProcessorGenerator<RoomProcessorCreeperSolver> {
        @Override
        public RoomProcessorCreeperSolver createNew(DungeonRoom dungeonRoom) {
            RoomProcessorCreeperSolver defaultRoomProcessor = new RoomProcessorCreeperSolver(dungeonRoom);
            return defaultRoomProcessor;
        }
    }
}
