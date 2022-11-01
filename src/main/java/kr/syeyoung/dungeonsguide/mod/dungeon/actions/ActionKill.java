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

package kr.syeyoung.dungeonsguide.mod.dungeon.actions;

import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonActionContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionRouteProperties;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

@Data
@EqualsAndHashCode(callSuper=false)
public class ActionKill extends AbstractAction {
    private Set<AbstractAction> preRequisite = new HashSet<AbstractAction>();
    private OffsetPoint target;
    private Predicate<Entity> predicate = entity -> false;
    private int radius;

    public ActionKill(OffsetPoint target) {
        this.target = target;
    }

    @Override
    public Set<AbstractAction> getPreRequisites(DungeonRoom dungeonRoom) {
        return preRequisite;
    }

    @Override
    public boolean isComplete(DungeonRoom dungeonRoom) {
        Vec3 spawn = new Vec3(target.getBlockPos(dungeonRoom));
        for (Integer killed : DungeonActionContext.getKilleds()) {
            if (DungeonActionContext.getSpawnLocation().get(killed) == null) continue;
            if (DungeonActionContext.getSpawnLocation().get(killed).squareDistanceTo(spawn) < 100) {
                return true;
            }
        }

        return killed;
    }

    private boolean killed = false;
    @Override
    public void onLivingDeath(DungeonRoom dungeonRoom, LivingDeathEvent event, ActionRouteProperties actionRouteProperties) {
        if (killed) return;

        Vec3 spawnLoc = DungeonActionContext.getSpawnLocation().get(event.entity.getEntityId());
        if (spawnLoc == null) return;
        if (target.getBlockPos(dungeonRoom).distanceSq(spawnLoc.xCoord, spawnLoc.yCoord, spawnLoc.zCoord) > radius * radius) return;
        if (!predicate.test(event.entity)) return;
        killed = true;
    }
    @Override
    public void onRenderWorld(DungeonRoom dungeonRoom, float partialTicks, ActionRouteProperties actionRouteProperties, boolean flag) {
        BlockPos pos = target.getBlockPos(dungeonRoom);
        RenderUtils.highlightBlock(pos, new Color(0, 255,255,50),partialTicks, true);
        RenderUtils.drawTextAtWorld("Spawn", pos.getX() + 0.5f, pos.getY() + 0.3f, pos.getZ() + 0.5f, 0xFFFFFF00, 0.02f, false, false, partialTicks);
    }

    @Override
    public String toString() {
        return "KillEntity\n- target: "+target.toString()+"\n- radius: "+radius+"\n- predicate: "+(predicate.test(null) ? "null" : predicate.getClass().getSimpleName());
    }
}
