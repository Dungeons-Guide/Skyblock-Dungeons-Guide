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

import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import kr.syeyoung.dungeonsguide.mod.dungeon.world.DGWorld;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RoomProcessorTeleportMazeSolver extends GeneralRoomProcessor {
    @Nullable
    private BlockPos lastPlayerLocation;

    public RoomProcessorTeleportMazeSolver(DungeonRoom dungeonRoom) {
        super(dungeonRoom);
        yLevel = dungeonRoom.getRoomBounds().getMin().getY() - 1;
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



        DGWorld w = getDungeonRoom().getContext().getWorld();
        LocalPlayer entityPlayerSP = Minecraft.getInstance().player;
        BlockPos pos2 = BlockPos.containing(entityPlayerSP.getX(), entityPlayerSP.getY(), entityPlayerSP.getZ());
        BlockState b = w.getBlockState(pos2);
        Vec3 lookVec = entityPlayerSP.getLookAngle();

        if (times % 4 == 1) {
            posX1 = entityPlayerSP.getX();
            posZ1 = entityPlayerSP.getZ();
            slope1 = lookVec.z / lookVec.x;
            times ++;
        } else if (times % 4 == 3) {
            posX2 = entityPlayerSP.getX();
            posZ2 = entityPlayerSP.getZ();
            slope2 = lookVec.z / lookVec.x;

            double yInt1 = posZ1 - posX1 * slope1;
            double yInt2 = posZ2 - posX2 * slope2;

            intersectionX = (yInt2 - yInt1) / (slope1 - slope2);
            intersectionZ = (slope1 * intersectionX + yInt1);
            intersection = new BlockPos((int) intersectionX, yLevel, (int) intersectionZ);
            times++;
        }

        if (LegacyBlock.STONE_SLAB.is(b) || LegacyBlock.STONE_SLAB2.is(b)) {
            boolean teleport = false;
            if (lastPlayerLocation == null) {
                return;
            }
            if (lastPlayerLocation.distSqr(pos2) < 3) {
                return;
            }
            for (BlockPos allInBox : BlockPos.betweenClosed(lastPlayerLocation, pos2)) {
                if (LegacyBlock.IRON_BARS.is(w.getBlockState(allInBox.offset(0,1,0)))) {
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

                for (BlockPos allInBox : BlockPos.betweenClosed(pos2.offset(-1, 0, -1), pos2.offset(1, 0, 1))) {
                    if (LegacyBlock.END_PORTAL_FRAME.is(w.getBlockState(allInBox))) {
                        if (!visitedPortals.contains(allInBox))
                        visitedPortals.add(allInBox.immutable());
                        break;
                    }
                }
                for (BlockPos allInBox : BlockPos.betweenClosed(lastPlayerLocation.offset(-1, -1, -1), lastPlayerLocation.offset(1, 1, 1))) {
                    if (LegacyBlock.END_PORTAL_FRAME.is(w.getBlockState(allInBox))) {
                        if (!visitedPortals.contains(allInBox))
                        visitedPortals.add(allInBox.immutable());
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
            RenderUtils.highlightBoxAColor( new AABB(bpos.getX(), bpos.getY(), bpos.getZ(), bpos.getX()+1, bpos.getY() + 1, bpos.getZ() + 1),  FeatureRegistry.SOLVER_TELEPORT.getTargetColor2(), partialTicks, true);
        }

        if (intersection != null) {
            RenderUtils.highlightBoxAColor( new AABB(intersection.getX(), intersection.getY(), intersection.getZ(), intersection.getX()+1, intersection.getY() + 1, intersection.getZ() + 1),   FeatureRegistry.SOLVER_TELEPORT.getTargetColor(), partialTicks, false);
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
