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

package kr.syeyoung.dungeonsguide.roomprocessor;

import kr.syeyoung.dungeonsguide.config.Config;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RoomProcessorTeleportMazeSolver extends GeneralRoomProcessor {
    private BlockPos lastPlayerLocation;

    public RoomProcessorTeleportMazeSolver(DungeonRoom dungeonRoom) {
        super(dungeonRoom);
        yLevel = dungeonRoom.getMin().getY() - 1;
    }

    private final List<BlockPos> visitedPortals = new ArrayList<BlockPos>();

    private int yLevel = 0;
    private double slope1, slope2;
    private double posX1, posZ1, posX2, posZ2;
    private int times=0 ;

    private double intersectionX, intersectionZ;
    private BlockPos intersection;

    @Override
    public void tick() {
        super.tick();



        World w = getDungeonRoom().getContext().getWorld();
        EntityPlayerSP entityPlayerSP = Minecraft.getMinecraft().thePlayer;
        BlockPos pos2 = new BlockPos(Math.floor(entityPlayerSP.posX), Math.floor(entityPlayerSP.posY), Math.floor(entityPlayerSP.posZ));
        Block b = w.getChunkFromBlockCoords(pos2).getBlock(pos2);
        Vec3 lookVec = entityPlayerSP.getLookVec();

        if (times % 4 == 1) {
            posX1 = entityPlayerSP.posX;
            posZ1 = entityPlayerSP.posZ;
            slope1 = lookVec.zCoord / lookVec.xCoord;
            times ++;
        } else if (times % 4 == 3) {
            posX2 = entityPlayerSP.posX;
            posZ2 = entityPlayerSP.posZ;
            slope2 = lookVec.zCoord / lookVec.xCoord;

            double yInt1 = posZ1 - posX1 * slope1;
            double yInt2 = posZ2 - posX2 * slope2;

            intersectionX = (yInt2 - yInt1) / (slope1 - slope2);
            intersectionZ = (slope1 * intersectionX + yInt1);
            intersection = new BlockPos((int) intersectionX, yLevel, (int) intersectionZ);
            times++;
        }

        if (b == Blocks.stone_slab || b == Blocks.stone_slab2) {
            boolean teleport = false;
            if (lastPlayerLocation.distanceSq(pos2) < 3) return;
            for (BlockPos allInBox : BlockPos.getAllInBox(lastPlayerLocation, pos2)) {
                if (w.getChunkFromBlockCoords(allInBox).getBlock(allInBox) == Blocks.iron_bars) {
                    teleport = true;
                    break;
                }
            }

            if (teleport) {
                if (times % 4 == 0) {
                    times ++;
                } else if (times % 4 == 2){
                times++;
                }

                for (BlockPos allInBox : BlockPos.getAllInBox(pos2.add(-1, 0, -1), pos2.add(1, 0, 1))) {
                    if (w.getChunkFromBlockCoords(allInBox).getBlock(allInBox) == Blocks.end_portal_frame) {
                        if (!visitedPortals.contains(allInBox))
                        visitedPortals.add(allInBox);
                        break;
                    }
                }
                for (BlockPos allInBox : BlockPos.getAllInBox(lastPlayerLocation.add(-1, -1, -1), lastPlayerLocation.add(1, 1, 1))) {
                    if (w.getChunkFromBlockCoords(allInBox).getBlock(allInBox) == Blocks.end_portal_frame) {
                        if (!visitedPortals.contains(allInBox))
                        visitedPortals.add(allInBox);
                        break;
                    }
                }
            }
        }

        lastPlayerLocation = pos2;
    }

    @Override
    public void drawWorld(float partialTicks) {
        super.drawWorld(partialTicks);
        if (!FeatureRegistry.SOLVER_TELEPORT.isEnabled()) return;
        for (BlockPos bpos:visitedPortals) {
            RenderUtils.highlightBoxAColor( AxisAlignedBB.fromBounds(bpos.getX(), bpos.getY(), bpos.getZ(), bpos.getX()+1, bpos.getY() + 1, bpos.getZ() + 1),  FeatureRegistry.SOLVER_TELEPORT.getTargetColor2(), partialTicks, true);
        }

        if (intersection != null) {
            RenderUtils.highlightBoxAColor( AxisAlignedBB.fromBounds(intersection.getX(), intersection.getY(), intersection.getZ(), intersection.getX()+1, intersection.getY() + 1, intersection.getZ() + 1),   FeatureRegistry.SOLVER_TELEPORT.getTargetColor(), partialTicks, false);
        }
    }
    public static class Generator implements RoomProcessorGenerator<RoomProcessorTeleportMazeSolver> {
        @Override
        public RoomProcessorTeleportMazeSolver createNew(DungeonRoom dungeonRoom) {
            RoomProcessorTeleportMazeSolver defaultRoomProcessor = new RoomProcessorTeleportMazeSolver(dungeonRoom);
            return defaultRoomProcessor;
        }
    }
}
