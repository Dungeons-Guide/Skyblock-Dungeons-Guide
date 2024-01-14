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

package kr.syeyoung.dungeonsguide.mod.dungeon.actions;


import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.route.ActionRouteProperties;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.route.RoomState;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.DungeonRoomButOpen;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.PathfindResult;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.algorithms.AStarFineGridStonking;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.algorithms.AStarFineGridStonkingBetter;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.algorithms.PathfinderExecutor;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

import java.util.HashSet;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper=false)
public class ActionMoveNearestAir extends AbstractAction {
    private OffsetPoint target;

    public ActionMoveNearestAir(OffsetPoint target) {
        this.target = target;
    }

    @Override
    public boolean isComplete(DungeonRoom dungeonRoom) {
        return target.getBlockPos(dungeonRoom).distanceSq(Minecraft.getMinecraft().thePlayer.getPosition()) < 25;
    }
    @Override
    public void onRenderWorld(DungeonRoom dungeonRoom, float partialTicks, ActionRouteProperties actionRouteProperties, boolean flag) {
        ActionMove.draw(dungeonRoom, partialTicks, actionRouteProperties, flag, target, poses);
    }

    private int tick = -1;
    private PathfindResult poses;
    private PathfinderExecutor executor;
    @Override
    public void onTick(DungeonRoom dungeonRoom, ActionRouteProperties actionRouteProperties) {
        tick = (tick+1) % Math.max(1, actionRouteProperties.getLineRefreshRate());
        if (executor == null && actionRouteProperties.isPathfind()) {
            executor = dungeonRoom.createEntityPathTo(target.getBlockPos(dungeonRoom));
            executor.setTarget(Minecraft.getMinecraft().thePlayer.getPositionVector());
        }
        if (executor != null) {
            poses = executor.getRoute(Minecraft.getMinecraft().thePlayer.getPositionVector());
        }

        if (tick == 0 && actionRouteProperties.isPathfind() && executor != null) {
            if (actionRouteProperties.getLineRefreshRate() != -1 && !FeatureRegistry.SECRET_FREEZE_LINES.isEnabled() && executor.isComplete()) {
                executor.setTarget(Minecraft.getMinecraft().thePlayer.getPositionVector());
            }
        }
    }

    @Override
    public void cleanup(DungeonRoom dungeonRoom, ActionRouteProperties actionRouteProperties) {
        executor = null;
    }

    public void forceRefresh(DungeonRoom dungeonRoom) {
        if (executor == null) executor = dungeonRoom.createEntityPathTo(target.getBlockPos(dungeonRoom));
        executor.setTarget(Minecraft.getMinecraft().thePlayer.getPositionVector());
    }
    @Override
    public String toString() {
        return "MoveNearestAir\n- target: "+target.toString();
    }

    @Override
    public double evalulateCost(RoomState state, DungeonRoom room, Map<String, Object> memoization) {
        BlockPos bpos = target.getBlockPos(room);
//        for (EnumFacing value : EnumFacing.VALUES) {
//            if (room.getCachedWorld().getBlockState(bpos.add(value.getFrontOffsetX(), value.getFrontOffsetY(), value.getFrontOffsetZ())).getBlock() == Blocks.air) {
//                bpos = bpos.add(value.getFrontOffsetX(), value.getFrontOffsetY(), value.getFrontOffsetZ());
//                break;
//            }
//        }
        PathfinderExecutor executor = (PathfinderExecutor) memoization.get(
                state.getOpenMechanics()+"-"+bpos
        );
        AStarFineGridStonkingBetter a = null;
        if (executor == null) {
            executor = new PathfinderExecutor(a = new AStarFineGridStonkingBetter(FeatureRegistry.SECRET_PATHFIND_SETTINGS.getAlgorithmSettings()), new Vec3(bpos.getX(), bpos.getY(), bpos.getZ())
                    .addVector(0.5, 0, 0.5), new DungeonRoomButOpen(room, new HashSet<>(state.getOpenMechanics()), bpos));
            memoization.put(state.getOpenMechanics()+"-"+bpos, executor);
            System.out.println("Generating new executor");
        }
        executor.setTarget(state.getPlayerPos());
        state.setPlayerPos(new Vec3(bpos.getX()+0.5, bpos.getY(), bpos.getZ()+0.5));
        long start = System.currentTimeMillis();
        double result = executor.findCost();
        long end= System.currentTimeMillis();
        if (a != null)
        {
            System.out.println("Generation took "+(end-start)+"ms");
//            System.out.println(a.getNodeMap().size());
            if (Double.isNaN(result)) {
                FeatureRegistry.SECRET_DAGS.setBetter(a);
                FeatureRegistry.SECRET_DAGS.setFrom(new BlockPos(executor.getTarget()));
            }
        }
        System.out.println(executor.getTarget()+"  to "+bpos+" vs "+result);

        if (Double.isNaN(result)) return 999999999;
        return result;
    }
}
