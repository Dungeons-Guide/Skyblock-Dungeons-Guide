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
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.BoundingBox;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.DungeonRoomButOpen;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.PathfindResult;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.algorithms.FineGridStonkingBFS;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.algorithms.PathfinderExecutor;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.client.Minecraft;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
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
        ActionMove.draw(dungeonRoom, partialTicks, actionRouteProperties, flag, target.getBlockPos(dungeonRoom), poses);
    }

    private int tick = -1;
    private PathfindResult poses;
    private PathfinderExecutor executor;
    @Override
    public void onTick(DungeonRoom dungeonRoom, ActionRouteProperties actionRouteProperties) {
        tick = (tick+1) % Math.max(1, actionRouteProperties.getLineRefreshRate());
        if (executor == null && actionRouteProperties.isPathfind()) {
            forceRefresh(dungeonRoom);
        }
        if (executor != null &&  !FeatureRegistry.SECRET_FREEZE_LINES.isEnabled() ) {
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
        BlockPos pos = target.getBlockPos(dungeonRoom);
        BoundingBox boundingBox = BoundingBox.of(AxisAlignedBB.fromBounds(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)
                .expand(0.5,1,0.5));
        System.out.println(boundingBox.getBoundingBoxes());
        if (executor == null) executor = dungeonRoom.createEntityPathTo(boundingBox);
        executor.setTarget(Minecraft.getMinecraft().thePlayer.getPositionVector());
    }
    @Override
    public String toString() {
        return "MoveNearestAir\n- target: "+target.toString();
    }

    @Override
    public double evalulateCost(RoomState state, DungeonRoom room, Map<String, Object> memoization) {
        BlockPos bpos = target.getBlockPos(room);

        if (memoization.containsKey("stupidheuristic")) {
            double cost = state.getPlayerPos().distanceTo(new Vec3(bpos));
            state.setPlayerPos(new Vec3(bpos));
            return cost;
        }

//        for (EnumFacing value : EnumFacing.VALUES) {
//            if (room.getCachedWorld().getBlockState(bpos.add(value.getFrontOffsetX(), value.getFrontOffsetY(), value.getFrontOffsetZ())).getBlock() == Blocks.air) {
//                bpos = bpos.add(value.getFrontOffsetX(), value.getFrontOffsetY(), value.getFrontOffsetZ());
//                break;
//            }
//        }
        PathfinderExecutor executor = (PathfinderExecutor) memoization.get(
                state.getOpenMechanics()+"-"+bpos
        );
        if (executor == null) {
            BoundingBox boundingBox = BoundingBox.of(AxisAlignedBB.fromBounds(bpos.getX(), bpos.getY(), bpos.getZ(), bpos.getX() + 1, bpos.getY() + 1, bpos.getZ() + 1)
                    .expand(0.5,1,0.5));

            executor = new PathfinderExecutor(new FineGridStonkingBFS(FeatureRegistry.SECRET_PATHFIND_SETTINGS.getAlgorithmSettings()),
                    boundingBox, new DungeonRoomButOpen(room, new HashSet<>(state.getOpenMechanics())));
            memoization.put(state.getOpenMechanics()+"-"+bpos, executor);
        }
        executor.setTarget(state.getPlayerPos());
        state.setPlayerPos(new Vec3(bpos.getX()+0.5, bpos.getY(), bpos.getZ()+0.5));
        double result = executor.findCost();
        if (Double.isNaN(result)) return 999999999;
        return result;
    }
}
