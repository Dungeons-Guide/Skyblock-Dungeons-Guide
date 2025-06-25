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
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.Vector3D;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.entity.UPlayerSelf;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RoomProcessorTeleportMazeSolver extends GeneralRoomProcessor {
    @Nullable
    private VectorI3D lastPlayerLocation;

    public RoomProcessorTeleportMazeSolver(DungeonRoom dungeonRoom) {
        super(dungeonRoom);
        yLevel = dungeonRoom.getRoomBounds().getMin().getY() - 1;
    }

    private final List<VectorI3D> visitedPortals = new ArrayList<>();

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
        UPlayerSelf entityPlayerSP = ModAPI.getAPI().getPlayer();
        VectorI3D pos2 = entityPlayerSP.getPosition();
        Block b = w.getBlockState(new BlockPos(pos2.getX(), pos2.getY(), pos2.getZ())).getBlock();
        Vector3D lookVec = entityPlayerSP.getLook(0);
        Vector3D pos = entityPlayerSP.getPositionVector();
        
        if (times % 4 == 1) {
            posX1 = pos.x;
            posZ1 = pos.z;
            slope1 = lookVec.z / lookVec.x;
            times ++;
        } else if (times % 4 == 3) {
            posX2 = pos.x;
            posZ2 = pos.z;
            slope2 = lookVec.z / lookVec.x;

            double yInt1 = posZ1 - posX1 * slope1;
            double yInt2 = posZ2 - posX2 * slope2;

            intersectionX = (yInt2 - yInt1) / (slope1 - slope2);
            intersectionZ = (slope1 * intersectionX + yInt1);
            intersection = new BlockPos((int) intersectionX, yLevel, (int) intersectionZ);
            times++;
        }

        if (b == Blocks.stone_slab || b == Blocks.stone_slab2) {
            boolean teleport = false;
            if (lastPlayerLocation == null) {
                return;
            }
            if (lastPlayerLocation.distanceSq(pos2) < 3) {
                return;
            }
            for (VectorI3D allInBox : VectorI3D.getAllInBox(lastPlayerLocation, pos2)) {
                if (w.getBlockState(new BlockPos(allInBox.getX(), allInBox.getY(), allInBox.getZ())).getBlock() == Blocks.iron_bars) {
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

                for (VectorI3D allInBox : VectorI3D.getAllInBox(pos2.add(-1, 0, -1), pos2.add(1, 0, 1))) {
                    if (w.getBlockState(new BlockPos(allInBox.getX(), allInBox.getY(), allInBox.getZ())).getBlock() == Blocks.end_portal_frame) {
                        if (!visitedPortals.contains(allInBox))
                        visitedPortals.add(allInBox);
                        break;
                    }
                }
                for (VectorI3D allInBox : VectorI3D.getAllInBox(lastPlayerLocation.add(-1, -1, -1), lastPlayerLocation.add(1, 1, 1))) {
                    if (w.getBlockState(new BlockPos(allInBox.getX(), allInBox.getY(), allInBox.getZ())).getBlock() == Blocks.end_portal_frame) {
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
        for (VectorI3D bpos:visitedPortals) {
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
