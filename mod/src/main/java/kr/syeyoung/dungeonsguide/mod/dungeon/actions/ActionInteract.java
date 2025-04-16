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


import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonActionContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.events.impl.PlayerInteractEntityEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;

import java.util.function.Predicate;

@Data
@EqualsAndHashCode(callSuper=false)
public class ActionInteract extends AbstractAction {
    private OffsetPoint target;
    private Predicate<Entity> predicate = entity -> false;
    private int radius;

    public ActionInteract(OffsetPoint target) {
        this.target = target;
    }

    @Override
    public boolean isComplete(DungeonRoom dungeonRoom) {
        return interacted;
    }

    private boolean interacted = false;
    @Override
    public void onLivingInteract(DungeonRoom dungeonRoom, PlayerInteractEntityEvent event) {
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
    public String toString() {
        return "InteractEntity\n- target: "+target.toString()+"\n- radius: "+radius+"\n- predicate: "+(predicate.test(null) ? "null" : predicate.getClass().getSimpleName());
    }
}
