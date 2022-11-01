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

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionRouteProperties;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = false)
public class ActionDropItem extends AbstractAction {
    private Set<AbstractAction> preRequisite = new HashSet<>();
    private OffsetPoint target;
    private Predicate<EntityItem> predicate = Predicates.alwaysTrue();

    public ActionDropItem(OffsetPoint target) {
        this.target = target;
    }

    @Override
    public Set<AbstractAction> getPreRequisites(DungeonRoom dungeonRoom) {
        return preRequisite;
    }

    @Override
    public boolean isComplete(DungeonRoom dungeonRoom) {
        BlockPos secretLocation = target.getBlockPos(dungeonRoom);
        List<EntityItem> item = dungeonRoom.getContext().getWorld().getEntitiesWithinAABB(EntityItem.class,
                AxisAlignedBB.fromBounds(
                        secretLocation.getX(),
                        secretLocation.getY(),
                        secretLocation.getZ(),
                        secretLocation.getX() + 1,
                        secretLocation.getY() + 1,
                        secretLocation.getZ() + 1));
        if (item.isEmpty()) {
            return false;
        }
        return (predicate == null || predicate.apply(item.get(0)));
    }
    @Override
    public void onRenderWorld(DungeonRoom dungeonRoom, float partialTicks, ActionRouteProperties actionRouteProperties, boolean flag) {
        BlockPos pos = target.getBlockPos(dungeonRoom);
        RenderUtils.highlightBlock(pos, new Color(0, 255, 255, 50), partialTicks, true);
        RenderUtils.drawTextAtWorld("Drop Item", pos.getX() + 0.5f, pos.getY() + 0.3f, pos.getZ() + 0.5f, 0xFFFFFF00, 0.02f, false, false, partialTicks);
    }


    @Override
    public String toString() {
        return "DropItem\n- target: " + target.toString() + "\n- predicate: " + predicate.getClass().getSimpleName();
    }
}
