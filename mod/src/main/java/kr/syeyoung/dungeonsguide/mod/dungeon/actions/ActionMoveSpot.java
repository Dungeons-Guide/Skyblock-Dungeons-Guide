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
import lombok.Getter;
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

@EqualsAndHashCode(callSuper=false)
public class ActionMoveSpot extends AbstractActionMove {
    @Getter
    private List<PossibleMoveSpot> targets;

    public ActionMoveSpot(List<PossibleMoveSpot> target, DungeonRoom dungeonRoom) {
        super(
                RaytraceHelper.chooseMinimalY2(target).stream().min(Comparator.comparingInt(b -> b.isBlocked() ? 1 : 0)).get()
                        .getOffsetPointSet().get(0),
                target.stream().flatMap(a -> a.getOffsetPointSet().stream()).collect(Collectors.toList())
        );
        this.targets = target;
    }

    public OffsetVec3 getTargetVec3() {
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

        super.onRenderWorld(dungeonRoom, partialTicks, actionRouteProperties, flag);
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
                        spot.getClusterId() + "/" + spot.isBlocked() + " / " + spot.getOffsetPointSet().size(), (float) cx, (float) cy, (float) cz, actual.getRGB() | 0xFF000000, 0.01f, false, true, partialTicks);


                GlStateManager.enableAlpha();
            }
        }
    }

    @Override
    public String toString() {
        return "Move\n- target: "+targets.get(0).toString();
    }

}
