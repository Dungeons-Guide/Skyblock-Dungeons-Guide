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


import kr.syeyoung.dungeonsguide.dungeon.data.OffsetVec3;
import kr.syeyoung.dungeonsguide.dungeon.data.PossibleClickingSpot;
import kr.syeyoung.dungeonsguide.dungeon.data.PossibleMoveSpot;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonBreakableWall;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonDoor;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonOnewayDoor;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonTomb;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.RouteBlocker;
import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.route.ActionRouteProperties;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.route.RoomState;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.BoundingBox;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.DungeonRoomButOpen;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.PathfindRequest;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.PathfindResult;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.algorithms.FineGridStonkingBFS;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.algorithms.PathfinderExecutor;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper=false)
public class ActionMoveSpot extends AbstractAction {
    private List<PossibleMoveSpot> targets;

    public ActionMoveSpot(List<PossibleMoveSpot> target, DungeonRoom dungeonRoom) {
        this.targets = target;
    }

    public OffsetVec3 getTarget() {
        OffsetVec3 vec = targets.get(0).getOffsetPointSet().get(0);
        return new OffsetVec3(vec.xCoord, vec.yCoord, vec.zCoord);
    }

    @Override
    public boolean isComplete(DungeonRoom dungeonRoom) {
        return targets.stream().flatMap(a -> a.getOffsetPointSet().stream()).anyMatch(
                a-> a.getPos(dungeonRoom).squareDistanceTo(Minecraft.getMinecraft().thePlayer.getPositionVector()) < 0.625
        );
    }

    @Override
    public void onRenderWorld(DungeonRoom dungeonRoom, float partialTicks, ActionRouteProperties actionRouteProperties, boolean flag) {

        {
            double cx = 0, cy =0 , cz = 0;
            int cnt = 0;
            for (OffsetVec3 _offsetVec3 : targets.stream().flatMap( a-> a.getOffsetPointSet().stream()).collect(Collectors.toList())) {
                Vec3 offsetVec3 = _offsetVec3.getPos(dungeonRoom);
                cx += offsetVec3.xCoord;
                cy += offsetVec3.yCoord;
                cz += offsetVec3.zCoord;
                cnt ++;
            }
            cx /= cnt;
            cy /= cnt;
            cz /= cnt;
            ActionMove.draw(dungeonRoom, partialTicks, actionRouteProperties, flag, new BlockPos(cx,cy,cz), poses);
        }

        if (FeatureRegistry.DEBUG_ST.isEnabled()) {
            int i = 0;
            for (PossibleMoveSpot spot : targets) {
                GlStateManager.disableAlpha();
                i++;
                Color c = Color.getHSBColor(
                        1.0f * i / targets.size(), 0.5f, 1.0f
                );
                Color actual;


                GlStateManager.disableAlpha();
                if (!spot.isBlocked()) {
                    actual = new Color(c.getRGB() & 0xFFFFFF | 0x90000000, true);
                    PossibleMoveSpot spot2 = RaytraceHelper.chooseMinimalY2(Arrays.asList(spot)).get(0);
                    for (OffsetVec3 _vec3 : spot2.getOffsetPointSet()) {
                        Vec3 offsetVec3 = _vec3.getPos(dungeonRoom);
                        RenderUtils.highlightBox(
                                new AxisAlignedBB(
                                        offsetVec3.xCoord - 0.25f, offsetVec3.yCoord + 0.025f, offsetVec3.zCoord - 0.25f,
                                        offsetVec3.xCoord + 0.25f, offsetVec3.yCoord + 0.026f, offsetVec3.zCoord + 0.25f
                                ).expand(0.0030000000949949026, 0.0030000000949949026, 0.0030000000949949026),
                                actual,
                                partialTicks,
                                true
                        );
                    }
                }
                actual = new Color(c.getRGB() & 0xFFFFFF | 0x10000000, true);
                for (OffsetVec3 _vec3 : spot.getOffsetPointSet()) {
                    Vec3 offsetVec3 = _vec3.getPos(dungeonRoom);
                    RenderUtils.highlightBox(
                            new AxisAlignedBB(
                                    offsetVec3.xCoord - 0.25f, offsetVec3.yCoord - 0.025f, offsetVec3.zCoord - 0.25f,
                                    offsetVec3.xCoord + 0.25f, offsetVec3.yCoord + 0.475f, offsetVec3.zCoord + 0.25f
                            ).expand(0.0030000000949949026, 0.0030000000949949026, 0.0030000000949949026),
                            actual,
                            partialTicks,
                            true
                    );
                }
                double cx = 0, cy = 0, cz = 0;
                for (OffsetVec3 _offsetVec3 : spot.getOffsetPointSet()) {
                    Vec3 offsetVec3 = _offsetVec3.getPos(dungeonRoom);
                    cx += offsetVec3.xCoord;
                    cy += offsetVec3.yCoord;
                    cz += offsetVec3.zCoord;
                }
                cx /= spot.getOffsetPointSet().size();
                cy /= spot.getOffsetPointSet().size();
                cz /= spot.getOffsetPointSet().size();
                cy += 0.2f;
                RenderUtils.drawTextAtWorld(
                        spot.getClusterId() + "/" + spot.isBlocked() + " / "+spot.getOffsetPointSet().size(), (float) cx, (float) cy, (float) cz, actual.getRGB() | 0xFF000000, 0.01f, false, true, partialTicks);


                GlStateManager.enableAlpha();
            }
        }

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
        if (executor != null && !FeatureRegistry.SECRET_FREEZE_LINES.isEnabled() ) {
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
        BoundingBox boundingBox = new BoundingBox();
        for (OffsetVec3 offsetPoint : targets.stream().flatMap(a -> a.getOffsetPointSet().stream()).collect(Collectors.toList())) {
            Vec3 pos = offsetPoint.getPos(dungeonRoom);
            boundingBox.addBoundingBox(new AxisAlignedBB(
                    pos.xCoord - 0.1, pos.yCoord - 0.1, pos.zCoord - 0.1,
                    pos.xCoord + 0.1, pos.yCoord + 0.1, pos.zCoord + 0.1
            ));
        }

        if (executor == null) executor = dungeonRoom.loadPrecalculated(new PathfindRequest(
                FeatureRegistry.SECRET_PATHFIND_SETTINGS.getAlgorithmSettings(),
                dungeonRoom.getDungeonRoomInfo(),
                dungeonRoom.getMechanics().entrySet().stream().filter(b -> {
                    return  b.getValue() instanceof DungeonDoor || b.getValue() instanceof DungeonOnewayDoor;
                }).filter(b -> !((RouteBlocker)b).isBlocking(dungeonRoom)).map(Map.Entry::getKey).collect(Collectors.toSet()),
                getTargets().stream().flatMap(b -> b.getOffsetPointSet().stream())
                        .collect(Collectors.toList())
        ).getId());
        if (executor == null) executor = dungeonRoom.createEntityPathTo(boundingBox);
        executor.setTarget(Minecraft.getMinecraft().thePlayer.getPositionVector());
    }
    @Override
    public String toString() {
        return "Move\n- target: "+targets.get(0).toString();
    }

    @Override
    public double evalulateCost(RoomState state, DungeonRoom room, Map<String, Object> memoization) {


        double cx = 0, cy =0 , cz = 0;
        int size = (int) targets.stream().flatMap(a -> a.getOffsetPointSet().stream()).count();
        for (OffsetVec3 _offsetVec3 : targets.stream().flatMap(a -> a.getOffsetPointSet().stream()).collect(Collectors.toList())) {
            Vec3 offsetVec3 = _offsetVec3.getPos(room);
            cx += offsetVec3.xCoord;
            cy += offsetVec3.yCoord;
            cz += offsetVec3.zCoord;
        }
        cx /= size;
        cy /= size;
        cz /= size;
        Vec3 bpos = new Vec3(cx,cy,cz);

        if (memoization.containsKey("stupidheuristic")) {
            double cost = state.getPlayerPos().distanceTo(bpos);
            state.setPlayerPos(bpos);
            return cost;
        }

        PathfinderExecutor executor = (PathfinderExecutor) memoization.get(
                state.getOpenMechanics()+"-"+bpos
        );
        FineGridStonkingBFS a = null;
        if (executor == null) {
            executor = room.loadPrecalculated(new PathfindRequest(
                    FeatureRegistry.SECRET_PATHFIND_SETTINGS.getAlgorithmSettings(),
                    room.getDungeonRoomInfo(),
                    state.getOpenMechanics().stream().filter(b -> {
                        return room.getMechanics().get(b) instanceof DungeonDoor || room.getMechanics().get(b) instanceof DungeonOnewayDoor;
                    }).collect(Collectors.toSet()),
                    getTargets().stream().flatMap(b -> b.getOffsetPointSet().stream())
                            .collect(Collectors.toList())
            ).getId());
            if (executor == null) {
                BoundingBox boundingBox = new BoundingBox();
                for (OffsetVec3 offsetPoint : targets.stream().flatMap(b -> b.getOffsetPointSet().stream()).collect(Collectors.toList())) {
                    Vec3 pos = offsetPoint.getPos(room);
                    boundingBox.addBoundingBox(new AxisAlignedBB(
                            pos.xCoord - 0.1, pos.yCoord - 0.1, pos.zCoord - 0.1,
                            pos.xCoord + 0.1, pos.yCoord + 0.1, pos.zCoord + 0.1
                    ));
                }

                executor = new PathfinderExecutor(new FineGridStonkingBFS(FeatureRegistry.SECRET_PATHFIND_SETTINGS.getAlgorithmSettings()),
                        boundingBox, new DungeonRoomButOpen(room, new HashSet<>(state.getOpenMechanics())));
            }
            memoization.put(state.getOpenMechanics()+"-"+bpos, executor);
        }
        executor.setTarget(state.getPlayerPos());
        OffsetVec3 pos = RaytraceHelper.chooseMinimalY2(targets).stream().min(Comparator.comparingInt(b -> b.isBlocked() ? 1 : 0)).get()
                .getOffsetPointSet().get(0);

        state.setPlayerPos(pos.getPos(room));
        double result = executor.findCost();
        if (Double.isNaN(result)) return 999999999;
        return result;
    }
}
