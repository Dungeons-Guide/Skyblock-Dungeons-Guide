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

package kr.syeyoung.dungeonsguide.dungeon.actions;

import kr.syeyoung.dungeonsguide.dungeon.actions.tree.ActionRouteProperties;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Data
@EqualsAndHashCode(callSuper=false)
public class ActionMoveNearestAir extends AbstractAction {
    private Set<AbstractAction> preRequisite = new HashSet<>();
    private OffsetPoint target;

    public ActionMoveNearestAir(OffsetPoint target) {
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
        ActionMove.draw(dungeonRoom, partialTicks, actionRouteProperties, flag, target, poses);
    }

    private int tick = -1;
    private List<Vec3> poses;
    private Future<List<Vec3>> latestFuture;
    @Override
    public void onTick(DungeonRoom dungeonRoom, ActionRouteProperties actionRouteProperties) {
        tick = (tick+1) % Math.max(1, actionRouteProperties.getLineRefreshRate());
        if (latestFuture != null && latestFuture.isDone()) {
            try {
                poses = latestFuture.get();
                latestFuture = null;
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        if (tick == 0 && actionRouteProperties.isPathfind() && latestFuture == null) {
            if (!FeatureRegistry.getInstance().SECRET_FREEZE_LINES.isEnabled() || poses == null) {
                latestFuture = dungeonRoom.createEntityPathTo(dungeonRoom.getContext().getWorld(), Minecraft.getMinecraft().thePlayer, target.getBlockPos(dungeonRoom), Integer.MAX_VALUE, 10000);
            }
        }
    }


    public void forceRefresh(DungeonRoom dungeonRoom) {
        if (latestFuture == null) {
            latestFuture = dungeonRoom.createEntityPathTo(dungeonRoom.getContext().getWorld(), Minecraft.getMinecraft().thePlayer, target.getBlockPos(dungeonRoom), Integer.MAX_VALUE, 10000);
        }
    }
    @Override
    public String toString() {
        return "MoveNearestAir\n- target: "+target.toString();
    }
}
