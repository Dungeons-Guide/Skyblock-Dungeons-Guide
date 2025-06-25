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

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.modapi.data.VectorI3D;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

@Data
@EqualsAndHashCode(callSuper=false)
public class ActionClick extends AbstractAction {
    private OffsetPoint target;
    private Predicate<ItemStack> predicate = Predicates.alwaysTrue();

    private boolean clicked = false;

    public ActionClick(OffsetPoint target) {
        this.target = target;
    }

    @Override
    public boolean isComplete(DungeonRoom dungeonRoom) {
        return clicked;
    }

    @Override
    public void onPlayerInteract(DungeonRoom dungeonRoom, PlayerInteractEvent event) {
        if (clicked) return;
        if (target.getBlockPos(dungeonRoom).equals(new VectorI3D(event.pos.getX(), event.pos.getY(), event.pos.getZ())) &&
                (predicate == null || predicate.apply(event.entityLiving.getHeldItem()))) {
            clicked = true;
        }
    }

    @Override
    public String toString() {
        return "Click\n- target: "+target.toString()+"\n- predicate: "+predicate.getClass().getSimpleName();
    }
}
