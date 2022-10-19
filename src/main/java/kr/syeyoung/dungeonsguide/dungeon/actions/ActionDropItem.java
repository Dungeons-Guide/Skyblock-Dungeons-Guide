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
import kr.syeyoung.dungeonsguide.dungeon.actions.tree.ActionRoute;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.events.impl.PlayerInteractEntityEvent;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper=false)
public class ActionDropItem implements AbstractAction {
    private Set<AbstractAction> preRequisite = new HashSet<AbstractAction>();
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
        BlockPos pos = target.getBlockPos(dungeonRoom);
        List<EntityItem> item = dungeonRoom.getContext().getWorld().getEntitiesWithinAABB(EntityItem.class,
                AxisAlignedBB.fromBounds(pos.getX(), pos.getY(), pos.getZ(), pos.getX()+1, pos.getY() + 1, pos.getZ() + 1));
        if (item.size() == 0) return false;
        return (predicate == null || predicate.apply(item.get(0)));
    }

    @Override
    public void onPlayerInteract(DungeonRoom dungeonRoom, PlayerInteractEvent event, ActionRoute.ActionRouteProperties actionRouteProperties) {

    }

    @Override
    public void onRenderWorld(DungeonRoom dungeonRoom, float partialTicks, ActionRoute.ActionRouteProperties actionRouteProperties, boolean flag) {
        BlockPos pos = target.getBlockPos(dungeonRoom);
        RenderUtils.highlightBlock(pos, new Color(0, 255,255,50),partialTicks, true);
        RenderUtils.drawTextAtWorld("Drop Item", pos.getX() + 0.5f, pos.getY() + 0.3f, pos.getZ() + 0.5f, 0xFFFFFF00, 0.02f, false, false, partialTicks);
    }

    @Override
    public void onLivingDeath(DungeonRoom dungeonRoom, LivingDeathEvent event, ActionRoute.ActionRouteProperties actionRouteProperties) {

    }

    @Override
    public void onRenderScreen(DungeonRoom dungeonRoom, float partialTicks, ActionRoute.ActionRouteProperties actionRouteProperties) {

    }

    @Override
    public void onLivingInteract(DungeonRoom dungeonRoom, PlayerInteractEntityEvent event, ActionRoute.ActionRouteProperties actionRouteProperties) {

    }

    @Override
    public void onTick(DungeonRoom dungeonRoom, ActionRoute.ActionRouteProperties actionRouteProperties) {

    }

    @Override
    public String toString() {
        return "DropItem\n- target: "+target.toString()+"\n- predicate: "+predicate.getClass().getSimpleName();
    }
}
