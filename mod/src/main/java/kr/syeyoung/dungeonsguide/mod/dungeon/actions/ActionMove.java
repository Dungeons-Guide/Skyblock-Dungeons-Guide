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
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionRouteProperties;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.algorithms.PathfinderExecutor;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper=false)
public class ActionMove extends AbstractAction {
    private Set<AbstractAction> preRequisite = new HashSet<>();
    private OffsetPoint target;

    public ActionMove(OffsetPoint target) {
        this.target = target;
    }

    @Override
    public Set<AbstractAction> getPreRequisites(DungeonRoom dungeonRoom) {
        return preRequisite;
    }

    @Override
    public boolean isComplete(DungeonRoom dungeonRoom) {
        return target.getBlockPos(dungeonRoom).distanceSq(Minecraft.getMinecraft().thePlayer.getPosition()) < 25;
    }

    @Override
    public void onRenderWorld(DungeonRoom dungeonRoom, float partialTicks, ActionRouteProperties actionRouteProperties, boolean flag) {
        draw(dungeonRoom, partialTicks, actionRouteProperties, flag, target, poses);
    }

    static void draw(DungeonRoom dungeonRoom, float partialTicks, ActionRouteProperties actionRouteProperties, boolean flag, OffsetPoint target, List<Vec3> poses) {
        BlockPos pos = target.getBlockPos(dungeonRoom);

        float distance = MathHelper.sqrt_double(pos.distanceSq(Minecraft.getMinecraft().thePlayer.getPosition()));
        float multiplier = distance / 120f; //mobs only render ~120 blocks away
        if (flag) multiplier *= 2.0f;
        float scale = 0.45f * multiplier;
        scale *= 25.0 / 6.0;
        if (actionRouteProperties.isBeacon()) {
            RenderUtils.renderBeaconBeam(pos.getX(), pos.getY(), pos.getZ(), actionRouteProperties.getBeaconBeamColor(), partialTicks);
            RenderUtils.highlightBlock(pos, actionRouteProperties.getBeaconColor(), partialTicks);
        }
        RenderUtils.drawTextAtWorld("Destination", pos.getX() + 0.5f, pos.getY() + 0.5f + scale, pos.getZ() + 0.5f, 0xFF00FF00, flag ? 2f : 1f, true, false, partialTicks);

        RenderUtils.drawTextAtWorld(String.format("%.2f",MathHelper.sqrt_double(pos.distanceSq(Minecraft.getMinecraft().thePlayer.getPosition())))+"m", pos.getX() + 0.5f, pos.getY() + 0.5f - scale, pos.getZ() + 0.5f, 0xFFFFFF00, flag ? 2f : 1f, true, false, partialTicks);

        if (!FeatureRegistry.SECRET_TOGGLE_KEY.isEnabled() || !FeatureRegistry.SECRET_TOGGLE_KEY.togglePathfindStatus) {
            if (poses != null){
                RenderUtils.drawLinesVec3(poses, actionRouteProperties.getLineColor(), actionRouteProperties.getLineWidth(), partialTicks,  true);
            }
        }
    }

    private int tick = -1;
    private List<Vec3> poses;
    private PathfinderExecutor executor;
    @Override
    public void onTick(DungeonRoom dungeonRoom, ActionRouteProperties actionRouteProperties) {
        tick = (tick+1) % Math.max(1, actionRouteProperties.getLineRefreshRate());
        if (executor == null) {
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


    public void forceRefresh(DungeonRoom dungeonRoom) {
        if (executor != null) executor.setTarget(Minecraft.getMinecraft().thePlayer.getPositionVector());
    }
    @Override
    public String toString() {
        return "Move\n- target: "+target.toString();
    }
}
