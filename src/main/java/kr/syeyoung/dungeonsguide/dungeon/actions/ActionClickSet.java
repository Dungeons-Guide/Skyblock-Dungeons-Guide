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

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

@Data
public class ActionClickSet extends AbstractAction {
    private Set<Action> preRequisite = new HashSet<Action>();
    private OffsetPointSet target;
    private Predicate<ItemStack> predicate = Predicates.alwaysTrue();

    public ActionClickSet(OffsetPointSet target) {
        this.target = target;
    }

    @Override
    public Set<Action> getPreRequisites(DungeonRoom dungeonRoom) {
        return preRequisite;
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
            if (pt2.getBlockPos(dungeonRoom).equals(event.pos) &&
                    (predicate == null || predicate.apply(event.entityLiving.getHeldItem()))) {
                clicked = true;
            }
        }

    }

    @Override
    public void onRenderWorld(DungeonRoom dungeonRoom, float partialTicks) {
        float xAcc = 0;
        float yAcc = 0;
        float zAcc = 0;
        int size = target.getOffsetPointList().size();
        for (OffsetPoint offsetPoint : target.getOffsetPointList()) {
            BlockPos pos = offsetPoint.getBlockPos(dungeonRoom);
            xAcc += pos.getX() + 0.5f;
            yAcc += pos.getY()+ 0.5f;
            zAcc += pos.getZ()+ 0.5f;
            RenderUtils.highlightBlock(offsetPoint.getBlockPos(dungeonRoom), new Color(0, 255,255,50),partialTicks, true);
        }

        RenderUtils.drawTextAtWorld("Click", xAcc / size, yAcc / size, zAcc / size, 0xFFFFFF00, 0.02f, false, false, partialTicks);
    }

    @Override
    public boolean isComplete(DungeonRoom dungeonRoom) {
        return clicked;
    }
}
