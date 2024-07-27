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

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetVec3;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonDoor;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonOnewayDoor;
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
import net.minecraft.init.Blocks;
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
    public AbstractActionMove(OffsetVec3 targetVec3, OffsetPoint targetOffsetPtSet) {
        this.targetVec3 = targetVec3;
        this.targetOffsetPointSet = Arrays.asList(
                new OffsetVec3(targetOffsetPtSet.getX(), targetOffsetPtSet.getY(), targetOffsetPtSet.getZ())
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

    @Override
    public void onRenderWorld(DungeonRoom dungeonRoom, float partialTicks, ActionRouteProperties actionRouteProperties, boolean flag) {
        draw(dungeonRoom, partialTicks, actionRouteProperties, flag, getBeaconTargetPos(dungeonRoom), poses);
        {
//            double cx = 0, cy =0 , cz = 0;
//            int cnt = 0;
//            for (OffsetVec3 _offsetVec3 : targets.stream().flatMap( a-> a.getOffsetPointSet().stream()).collect(Collectors.toList())) {
//                Vec3 offsetVec3 = _offsetVec3.getPos(dungeonRoom);
//                cx += offsetVec3.xCoord;
//                cy += offsetVec3.yCoord;
//                cz += offsetVec3.zCoord;
//                cnt ++;
//            }
//            cx /= cnt;
//            cy /= cnt;
//            cz /= cnt;
        }
    }

    static void draw(DungeonRoom dungeonRoom, float partialTicks, ActionRouteProperties actionRouteProperties, boolean flag, BlockPos target, PathfindResult poses) {
        float distance = MathHelper.sqrt_double(target.distanceSq(Minecraft.getMinecraft().thePlayer.getPosition()));
        float multiplier = distance / 120f; //mobs only render ~120 blocks away
        if (flag) multiplier *= 2.0f;
        float scale = 0.45f * multiplier;
        scale *= 25.0 / 6.0;
        if (actionRouteProperties.isBeacon()) {
            RenderUtils.renderBeaconBeam(target.getX(), target.getY(), target.getZ(), actionRouteProperties.getBeaconBeamColor(), partialTicks);
            RenderUtils.highlightBlock(target, actionRouteProperties.getBeaconColor(), partialTicks);
        }
        RenderUtils.drawTextAtWorld("Destination", target.getX() + 0.5f, target.getY() + 0.5f + scale, target.getZ() + 0.5f, 0xFF00FF00, flag ? 2f : 1f, true, false, partialTicks);

        RenderUtils.drawTextAtWorld(String.format("%.2f",MathHelper.sqrt_double(target.distanceSq(Minecraft.getMinecraft().thePlayer.getPosition())))+"m", target.getX() + 0.5f, target.getY() + 0.5f - scale, target.getZ() + 0.5f, 0xFFFFFF00, flag ? 2f : 1f, true, false, partialTicks);

        if (!FeatureRegistry.SECRET_TOGGLE_KEY.isEnabled() || !FeatureRegistry.SECRET_TOGGLE_KEY.togglePathfindStatus) {
            if (poses != null){
                AbstractActionMove.drawLinesPathfindNode(poses.getNodeList(), actionRouteProperties.getLineColor(), (float) actionRouteProperties.getLineWidth(), partialTicks);

                int cnt = 0;
                int warp = 0;
                for (PathfindResult.PathfindNode pose : poses.getNodeList()) {
                    cnt ++;
                    if (pose.getType() != null && pose.getType() != PathfindResult.PathfindNode.NodeType.WALK && pose.getType() != PathfindResult.PathfindNode.NodeType.STONK_WALK && pose.distanceSq(Minecraft.getMinecraft().thePlayer.getPosition()) < 100) {
                        RenderUtils.drawTextAtWorld(pose.getType().toString(), pose.getX(), pose.getY() + 0.5f, pose.getZ(), 0xFF00FF00, 0.02f, false, true, partialTicks);
                    }

                    if (warp == 1) {
                        BlockPos pos = new BlockPos(Math.floor(pose.getX()), Math.floor(pose.getY()) -1 , Math.floor(pose.getZ()));
                        RenderUtils.highlightBox(Blocks.stone.getSelectedBoundingBox(null, pos).expand(0.003, 0.003, 0.003), Color.green, partialTicks, true);
                        warp = 2;
                    }
                    if (pose.getType() == PathfindResult.PathfindNode.NodeType.ETHERWARP && cnt < 10 && warp < 2) {
                        warp = 1;
                    }

                }
            }
        }
    }

    public static void drawLinesPathfindNode(List<PathfindResult.PathfindNode> poses, AColor colour, float thickness, float partialTicks) {
        if (poses.size() == 0) return;
        Entity render = Minecraft.getMinecraft().getRenderViewEntity();
        WorldRenderer worldRenderer = Tessellator.getInstance().getWorldRenderer();

        double realX = render.lastTickPosX + (render.posX - render.lastTickPosX) * partialTicks;
        double realY = render.lastTickPosY + (render.posY - render.lastTickPosY) * partialTicks;
        double realZ = render.lastTickPosZ + (render.posZ - render.lastTickPosZ) * partialTicks;

        GlStateManager.pushMatrix();
        GlStateManager.translate(-realX, -realY, -realZ);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GL11.glLineWidth(thickness);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        if ((poses.get(0).getType() == PathfindResult.PathfindNode.NodeType.STONK_WALK ) && poses.get(0).distanceSq(Minecraft.getMinecraft().thePlayer.getPosition()) < 100) {
            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);
        }


//        GlStateManager.color(colour.getRed() / 255f, colour.getGreen() / 255f, colour.getBlue()/ 255f, colour.getAlpha() / 255f);
        GlStateManager.color(1,1,1,1);
        worldRenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        int num = 0;

        PathfindResult.PathfindNode lastNode = null;
        for (PathfindResult.PathfindNode pos:poses) {
            int i = RenderUtils.getColorAt(num++ * 10,0, colour);
            worldRenderer.pos(pos.getX(), pos.getY(), pos.getZ()).color(
                    ((i >> 16) &0xFF)/255.0f,
                    ((i >> 8) &0xFF)/255.0f,
                    (i &0xFF)/255.0f,
                    ((i >> 24) &0xFF)/255.0f
            ).endVertex();

            if (lastNode != null && lastNode.getType() != pos.getType()) {
                Tessellator.getInstance().draw();
                worldRenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);


                if ((pos.getType() == PathfindResult.PathfindNode.NodeType.STONK_WALK || poses.get(0).getType() == PathfindResult.PathfindNode.NodeType.STONK_WALK)&& pos.distanceSq(Minecraft.getMinecraft().thePlayer.getPosition()) < 100) {
                    GlStateManager.disableDepth();
                    GlStateManager.depthMask(false);
                } else {
                    GlStateManager.enableDepth();
                    GlStateManager.depthMask(true);
                }

                worldRenderer.pos(pos.getX(), pos.getY(), pos.getZ()).color(
                        ((i >> 16) &0xFF)/255.0f,
                        ((i >> 8) &0xFF)/255.0f,
                        (i &0xFF)/255.0f,
                        ((i >> 24) &0xFF)/255.0f
                ).endVertex();
            }
            lastNode = pos;

        }
        Tessellator.getInstance().draw();

        GlStateManager.translate(realX, realY, realZ);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
        GL11.glLineWidth(1);
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
        if (executor == null) executor = dungeonRoom.loadPrecalculated(new PathfindRequest(
                FeatureRegistry.SECRET_PATHFIND_SETTINGS.getAlgorithmSettings(),
                dungeonRoom.getDungeonRoomInfo(),
                dungeonRoom.getMechanics().entrySet().stream().filter(b -> {
                    return  (b.getValue() instanceof DungeonDoor || b.getValue() instanceof DungeonOnewayDoor);
                }).filter(b -> !((RouteBlocker)b.getValue()).isBlocking(dungeonRoom)).map(Map.Entry::getKey).collect(Collectors.toSet()),
                getTargetOffsetPointSet()
        ).getId());
        if (executor == null) executor = dungeonRoom.createEntityPathTo(getPathfindBoundingBox(dungeonRoom));
        if (executor != null) executor.setTarget(Minecraft.getMinecraft().thePlayer.getPositionVector());
    }


    @Override
    public String toString() {
        return "Move\n- target: "+ getTargetVec3().toString();
    }

    @Override
    public double evalulateCost(RoomState state, DungeonRoom room, Map<String, Object> memoization) {
        Vec3 bpos = getTransformedTargetVec3(room);

        if (memoization.containsKey("stupidheuristic")) {
            double cost = state.getPlayerPos().distanceTo(bpos);
            state.setPlayerPos(bpos);
            return cost;
        }

        PathfinderExecutor executor = (PathfinderExecutor) memoization.get(
                state.getOpenMechanics()+"-"+vec3Str
        );
        FineGridStonkingBFS a = null;
        if (executor == null) {
            executor = room.loadPrecalculated(new PathfindRequest(
                    FeatureRegistry.SECRET_PATHFIND_SETTINGS.getAlgorithmSettings(),
                    room.getDungeonRoomInfo(),
                    state.getOpenMechanics().stream().filter(b -> {
                        return  room.getMechanics().get(b) instanceof DungeonDoor || room.getMechanics().get(b) instanceof DungeonOnewayDoor;
                    }).collect(Collectors.toSet()),
                    getTargetOffsetPointSet()
            ).getId());
            if (executor == null) return 999999999;
            if (executor == null) {
                executor = new PathfinderExecutor(new FineGridStonkingBFS(FeatureRegistry.SECRET_PATHFIND_SETTINGS.getAlgorithmSettings()),
                        getPathfindBoundingBox(room), new DungeonRoomButOpen(room, new HashSet<>(state.getOpenMechanics())));
            }
            memoization.put(state.getOpenMechanics()+"-"+vec3Str, executor);

        }
        executor.setTarget(state.getPlayerPos());
        state.setPlayerPos(bpos);

        double result = executor.findCost();
        if (Double.isNaN(result)) return 999999999;
        return result;
    }
}
