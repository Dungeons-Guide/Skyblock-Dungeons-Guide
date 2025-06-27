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
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.modapi.event.events.PlayerInteractEvent;
import kr.syeyoung.modapi.item.UItemStack;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.function.Predicate;

@Data
@EqualsAndHashCode(callSuper=false)
public class ActionClickSet extends AbstractAction {
    private OffsetPointSet target;
    private Predicate<UItemStack> predicate = stack -> true;

    public ActionClickSet(OffsetPointSet target) {
        this.target = target;
    }

    @Override
    public String toString() {
        return "ClickSet\n- targets size: "+target.getOffsetPointList().size()+"\n- predicate: "+predicate.getClass().getSimpleName();
    }

    private boolean clicked = false;
    @Override
    public void onPlayerInteract(DungeonRoom dungeonRoom, PlayerInteractEvent event) {
        if (clicked) return;
        for (OffsetPoint pt2: target.getOffsetPointList()) {
            if (pt2.getBlockPos(dungeonRoom).equals(event.pos) && predicate.test(event.player.getHeldItem())) {
                clicked = true;
            }
        }

    }

    @Override
    public boolean isComplete(DungeonRoom dungeonRoom) {
        return clicked;
    }
}
