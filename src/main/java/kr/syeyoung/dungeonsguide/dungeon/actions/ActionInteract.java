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

import kr.syeyoung.dungeonsguide.dungeon.DungeonActionContext;
import kr.syeyoung.dungeonsguide.dungeon.actions.tree.ActionRouteProperties;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.events.impl.PlayerInteractEntityEvent;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

@Data
@EqualsAndHashCode(callSuper=false)
public class ActionInteract extends AbstractAction {
    private Set<AbstractAction> preRequisite = new HashSet<>();
    private OffsetPoint target;
    private Predicate<Entity> predicate = entity -> false;
    private int radius;

    public ActionInteract(OffsetPoint target) {
        this.target = target;
    }

    @Override
    public Set<AbstractAction> getPreRequisites(DungeonRoom dungeonRoom) {
        return preRequisite;
    }

    @Override
    public boolean isComplete(DungeonRoom dungeonRoom) {
        return interacted;
    }

    private boolean interacted = false;
    @Override
    public void onLivingInteract(DungeonRoom dungeonRoom, PlayerInteractEntityEvent event, ActionRouteProperties actionRouteProperties) {
        if (interacted) return;

        Vec3 spawnLoc = DungeonActionContext.getSpawnLocation().get(event.getEntity().getEntityId());
        if (spawnLoc == null) {
            return;
        }
        if (target.getBlockPos(dungeonRoom).distanceSq(spawnLoc.xCoord, spawnLoc.yCoord, spawnLoc.zCoord) > radius * radius) {
            return;
        }
        if (!predicate.test(event.getEntity())) {
            return;
        }
        interacted = true;
    }

    @Override
    public void onRenderWorld(DungeonRoom dungeonRoom, float partialTicks, ActionRouteProperties actionRouteProperties, boolean flag) {
        BlockPos pos = target.getBlockPos(dungeonRoom);
        RenderUtils.highlightBlock(pos, new Color(0, 255,255,50),partialTicks, true);
        RenderUtils.drawTextAtWorld("Interact", pos.getX() + 0.5f, pos.getY() + 0.3f, pos.getZ() + 0.5f, 0xFFFFFF00, 0.02f, false, false, partialTicks);
    }

    @Override
    public String toString() {
        return "InteractEntity\n- target: "+target.toString()+"\n- radius: "+radius+"\n- predicate: "+(predicate.test(null) ? "null" : predicate.getClass().getSimpleName());
    }
}
