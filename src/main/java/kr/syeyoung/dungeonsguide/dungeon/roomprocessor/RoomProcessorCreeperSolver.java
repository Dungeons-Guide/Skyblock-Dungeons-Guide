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

package kr.syeyoung.dungeonsguide.dungeon.roomprocessor;

import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

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

    private boolean check(AxisAlignedBB axis, Vec3 vec) {
        if (vec == null) return false;
        return axis.isVecInside(vec);
    }

    private void findCreeperAndDoPoses() {
        World w = getDungeonRoom().getContext().getWorld();
        List<BlockPos> prismarines = new ArrayList<BlockPos>();
        final BlockPos low = getDungeonRoom().getMin().add(0,-2,0);
        final BlockPos high = getDungeonRoom().getMax().add(0,20,0);
        final AxisAlignedBB axis = AxisAlignedBB.fromBounds(
                low.getX() + 17, low.getY() + 7, low.getZ() + 17,
                low.getX() + 16, low.getY() + 10.5, low.getZ() + 16
        );

        for (BlockPos pos : BlockPos.getAllInBox(low, high)) {
            Block b = w.getBlockState(pos).getBlock();
            if (b == Blocks.prismarine || b == Blocks.sea_lantern) {
                for (EnumFacing face:EnumFacing.VALUES) {
                    if (w.getBlockState(pos.offset(face)).getBlock() == Blocks.air) {
                        prismarines.add(pos);
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

                if (second.distanceSq(first) < highestDist) continue;

                Vec3 startLoc = new Vec3(first).addVector(0.5,0.5,0.5);
                Vec3 dest = new Vec3(second).addVector(0.5,0.5,0.5);
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
                poses.add(new BlockPos[] {first, highestMatch});
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
    public void drawWorld(float partialTicks) {
        super.drawWorld(partialTicks);
        if (!FeatureRegistry.SOLVER_CREEPER.isEnabled()) return;
        World w = getDungeonRoom().getContext().getWorld();
        for (int i = 0; i < poses.size(); i++) {
            BlockPos[] poset = poses.get(i);
            Color color = colors[i % colors.length];
            boolean oneIsConnected = w.getChunkFromBlockCoords(poset[0]).getBlock(poset[0]) != Blocks.sea_lantern &&
                    w.getChunkFromBlockCoords(poset[1]).getBlock(poset[1]) != Blocks.sea_lantern;
            RenderUtils.drawLine(new Vec3(poset[0].getX() +0.5, poset[0].getY() +0.5, poset[0].getZ()+0.5),
                    new Vec3(poset[1].getX() +0.5, poset[1].getY() +0.5, poset[1].getZ()+0.5), oneIsConnected ? new Color(0,0,0,50) : color, partialTicks, true);
        }
        final BlockPos low = getDungeonRoom().getMin();
        final AxisAlignedBB axis = AxisAlignedBB.fromBounds(
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
