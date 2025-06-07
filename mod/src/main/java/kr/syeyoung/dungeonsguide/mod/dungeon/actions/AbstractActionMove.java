/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2024  cyoung06 (syeyoung)
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

import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetVec3;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.DungeonDoorState;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.DungeonOnewayDoorState;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.WorldMutatingMechanicState;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.route.RoomState;
import kr.syeyoung.dungeonsguide.mod.pathfinding.BoundingBox;
import kr.syeyoung.dungeonsguide.mod.pathfinding.TSPCache;
import kr.syeyoung.dungeonsguide.mod.pathfinding.precalculation.PathfindPrecalculation;
import kr.syeyoung.dungeonsguide.mod.pathfinding.precalculation.TSPCacheRegistry;
import kr.syeyoung.dungeonsguide.mod.pathfinding.preset.RoomPresetPathPlanner;
import kr.syeyoung.dungeonsguide.mod.pathfinding.world.PathfindRequest;
import kr.syeyoung.dungeonsguide.mod.pathfinding.pathfinder.PathfinderExecutor;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.GeneralRoomProcessor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper=false)
public abstract class AbstractActionMove extends AbstractAction {
//    private List<PossibleClickingSpot> targets;
    private String id;

    private OffsetVec3 targetVec3;
    private BlockPos beaconTargetPos;
    private List<OffsetVec3> targetOffsetPointSet;

    public AbstractActionMove(OffsetVec3 targetVec3, BlockPos beaconPos, List<OffsetVec3> targetOffsetPtSet) {
        this.targetVec3 = targetVec3;
        this.beaconTargetPos = beaconPos;
        this.targetOffsetPointSet = targetOffsetPtSet;
    }
    public AbstractActionMove(OffsetVec3 targetVec3, List<OffsetVec3> targetOffsetPtSet) {
        this.targetVec3 = targetVec3;
        this.targetOffsetPointSet = targetOffsetPtSet;
    }
    public AbstractActionMove(OffsetVec3 targetVec3) {
        this.targetVec3 = targetVec3;
        this.targetOffsetPointSet = Arrays.asList(
                targetVec3
        );
    }


    private BoundingBox boundingBox;
    public BoundingBox getPathfindBoundingBox(DungeonRoom dungeonRoom) {
        if (this.boundingBox != null) return boundingBox;
        BoundingBox boundingBox = new BoundingBox();
        for (OffsetVec3 offsetPoint : getTargetOffsetPointSet()) {
            Vec3 pos = offsetPoint.getPos(dungeonRoom);
            boundingBox.addBoundingBox(new AxisAlignedBB(
                    pos.xCoord - 0.1, pos.yCoord - 0.1, pos.zCoord - 0.1,
                    pos.xCoord + 0.1, pos.yCoord + 0.1, pos.zCoord + 0.1
            ));
        }
        return this.boundingBox = boundingBox;
    }

    public BlockPos getBeaconTargetPos(DungeonRoom dungeonRoom) {
        if (beaconTargetPos != null) return beaconTargetPos;
        return beaconTargetPos = new BlockPos(targetVec3.getPos(dungeonRoom));
    }

    private Vec3 transformedTargetVec3;
    private String vec3Str;
    private Vec3 getTransformedTargetVec3(DungeonRoom dungeonRoom) {
        if (transformedTargetVec3 != null) return transformedTargetVec3;
        this.transformedTargetVec3 = getTargetVec3().getPos(dungeonRoom);
        vec3Str = transformedTargetVec3.toString();
        return transformedTargetVec3;
    }

    //    @Override
//    public boolean isComplete(DungeonRoom dungeonRoom) {
//        return targets.stream().flatMap(a -> a.getOffsetPointSet().stream()).anyMatch(
//                a-> a.getPos(dungeonRoom).squareDistanceTo(Minecraft.getMinecraft().thePlayer.getPositionVector()) < 0.625
//        );
//    }



    public PathfindRequest getPathfindRequest(DungeonRoom dungeonRoom) {
        GeneralRoomProcessor generalRoomProcessor = (GeneralRoomProcessor) dungeonRoom.getRoomProcessor();
        return new PathfindRequest(
                generalRoomProcessor.getAlgorithmSetting(),
                dungeonRoom.getDungeonRoomInfo(),
                dungeonRoom.getMechanics().entrySet().stream().filter(b -> {
                    return  (b.getValue() instanceof DungeonDoorState || b.getValue() instanceof DungeonOnewayDoorState);
                }).filter(b -> !((WorldMutatingMechanicState)b.getValue()).isBlocking(dungeonRoom)).map(Map.Entry::getKey).collect(Collectors.toSet()),
                getTargetOffsetPointSet()
        );
    }

    public void forceRefresh(DungeonRoom dungeonRoom) {
        throw new UnsupportedOperationException("BRUH");
    }


    @Override
    public String toString() {
        return "Move\n- target: "+ getTargetVec3().toString();
    }

    private volatile String[] hashCache;

    @Override
    public double evalulateCost(RoomState state, DungeonRoom room, TSPCache tspCache, RoomPresetPathPlanner pathPlanner) {
        Vec3 bpos = getTransformedTargetVec3(room);

        if (hashCache == null) {
            hashCache = new String[1 << state.getOpenMechanicsIndex().size()];
        }

        String hash = hashCache[state.openMechanicsBitset];
        if (hash == null) {
            int bitset = state.openMechanicsBitset;
            Set<String> setConstruction = new HashSet<>();
            for (int i = 0; i < state.getOpenMechanicsIndex().size(); i++) {
                if ((bitset & (1 << i)) != 0) {
                    setConstruction.add(state.getOpenMechanicsIndex().get(i));
                }
            }

            hashCache[state.openMechanicsBitset] = hash = new PathfindRequest(
                    pathPlanner.getAlgorithmSetting(),
                    room.getDungeonRoomInfo(),
                    setConstruction,
                    getTargetOffsetPointSet()
            ).getHash();
        }


        kr.syeyoung.dungeonsguide.mod.pathfinding.precalculation.TSPCache tspCache1 = pathPlanner.getTSPCache();
        double cost = tspCache1 == null ? -1 : tspCache1.getCost(hash, state.getPlayerPosOff());
        if (cost >= 0) {
            state.setPlayerPos(bpos);
            return cost;
        }

        cost = tspCache.getCost(hash, state.getPlayerPos());
        if (cost >= 0) {
            state.setPlayerPos(bpos);
            return cost;
        }

        if (cost == -2) {
            PathfindPrecalculation precalculation = pathPlanner.getPrecalcByHash(hash);
            if (precalculation == null) return Double.POSITIVE_INFINITY;
            try {
                tspCache.addToCache(precalculation);
            } catch (IOException e) { throw new RuntimeException(e); }

            cost = tspCache.getCost(hash, state.getPlayerPos());
            state.setPlayerPos(bpos);
            return cost;
        }

//        System.out.println(state.getPlayerPos());

        PathfinderExecutor executor = pathPlanner.loadPrecalculatedByHash(hash, room); // might be used later soon.

        double result = executor.getPathfinder().getCost(state.getPlayerPos());

        executor.close();

        state.setPlayerPos(bpos);
        if (Double.isNaN(result)) return 999999999;
        return result;
    }
}
