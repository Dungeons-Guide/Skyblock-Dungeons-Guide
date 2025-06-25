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


import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonActionContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.modapi.data.Vector3D;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.entity.Entity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import java.util.function.Predicate;

@Data
@EqualsAndHashCode(callSuper=false)
public class ActionKill extends AbstractAction {
    private OffsetPoint target;
    private Predicate<Entity> predicate = entity -> false;
    private int radius;

    public ActionKill(OffsetPoint target) {
        this.target = target;
    }

    @Override
    public boolean isComplete(DungeonRoom dungeonRoom) {
        Vector3D spawn = new Vector3D(target.getBlockPos(dungeonRoom));
        for (Integer killed : DungeonActionContext.getKilleds()) {
            if (DungeonActionContext.getSpawnLocation().get(killed) == null) continue;
            if (DungeonActionContext.getSpawnLocation().get(killed).distanceSq(spawn) < 100) {
                return true;
            }
        }

        return killed;
    }

    private boolean killed = false;
    @Override
    public void onLivingDeath(DungeonRoom dungeonRoom, LivingDeathEvent event) {
        if (killed) return;

        Vector3D spawnLoc = DungeonActionContext.getSpawnLocation().get(event.entity.getEntityId());
        if (spawnLoc == null) return;
        if (target.getBlockPos(dungeonRoom).distanceSq(spawnLoc.x, spawnLoc.y, spawnLoc.z) > radius * radius) return;
        if (!predicate.test(event.entity)) return;
        killed = true;
    }

    @Override
    public String toString() {
        return "KillEntity\n- target: "+target.toString()+"\n- radius: "+radius+"\n- predicate: "+(predicate.test(null) ? "null" : predicate.getClass().getSimpleName());
    }
}
