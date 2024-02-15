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

package kr.syeyoung.dungeonsguide.dungeon.mechanics;

import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.RouteBlocker;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGBuilder;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import lombok.Data;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
public class DungeonFakeChestTrap implements DungeonMechanic {
    private static final long serialVersionUID = -7347076019472222115L;
    private OffsetPointSet tnts = new OffsetPointSet();
    private OffsetPoint chest = new OffsetPoint(0,0,0);
    private List<String> preRequisite = new ArrayList<>();


    @Override
    public void buildAction(String state, DungeonRoom dungeonRoom, ActionDAGBuilder builder) throws PathfindImpossibleException {
        if (state.equalsIgnoreCase("navigate")) {
            builder = builder
                    .requires(new ActionMoveNearestAir(getRepresentingPoint(dungeonRoom)));
            for (String str : preRequisite) {
                if (str.isEmpty()) continue;
                builder.requires(new ActionChangeState(str.split(":")[0], str.split(":")[1]));
            }
            return;
        }
        if (!"triggered".equalsIgnoreCase(state)) {
            throw new PathfindImpossibleException(state + " is not valid state for tomb");
        }
        if (!isBlocking(dungeonRoom)) {
            return;
        }

        ActionUtils.buildActionMoveAndClick(builder, dungeonRoom, chest, builder1 -> {
            for (String str : preRequisite) {
                if (str.isEmpty()) continue;
                builder1.optional(new ActionChangeState(str.split(":")[0], str.split(":")[1]));
            }
            return null;
        });

    }

    @Override
    public void highlight(Color color, String name, DungeonRoom dungeonRoom, float partialTicks) {
        if (tnts.getOffsetPointList().isEmpty()) return;
        OffsetPoint firstPoint = tnts.getOffsetPointList().get(0);
        BlockPos pos = chest.getBlockPos(dungeonRoom);
        RenderUtils.highlightBlock(pos, color,partialTicks);
        RenderUtils.drawTextAtWorld(name, pos.getX() + 0.5f, pos.getY() + 0.75f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
        RenderUtils.drawTextAtWorld(getCurrentState(dungeonRoom), pos.getX() + 0.5f, pos.getY() + 0.25f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);

        for (OffsetPoint offsetPoint : tnts.getOffsetPointList()) {
            RenderUtils.highlightBlock(offsetPoint.getBlockPos(dungeonRoom), color, partialTicks);
        }
    }

    public boolean isBlocking(DungeonRoom dungeonRoom) {
        for (OffsetPoint offsetPoint : tnts.getOffsetPointList()) {
            if (offsetPoint.getBlock(dungeonRoom) != Blocks.air) return true;
        }
        return false;
    }

    public DungeonFakeChestTrap clone() throws CloneNotSupportedException {
        DungeonFakeChestTrap dungeonSecret = new DungeonFakeChestTrap();
        dungeonSecret.tnts = (OffsetPointSet) tnts.clone();
        dungeonSecret.chest = (OffsetPoint) chest.clone();
        dungeonSecret.preRequisite = new ArrayList<>(preRequisite);
        return dungeonSecret;
    }

    @Override
    public String getCurrentState(DungeonRoom dungeonRoom) {
        Block b = Blocks.air;
        if (!tnts.getOffsetPointList().isEmpty())
            b = tnts.getOffsetPointList().get(0).getBlock(dungeonRoom);
        return b == Blocks.air ? "triggered" : "untriggered";
    }

    @Override
    public Set<String> getPossibleStates(DungeonRoom dungeonRoom) {
        return isBlocking(dungeonRoom) ? Sets.newHashSet("triggered", "navigate") : Sets.newHashSet("navigate");
    }

    @Override
    public Set<String> getTotalPossibleStates(DungeonRoom dungeonRoom) {
        return Sets.newHashSet("triggered", "untriggered");
    }


    @Override
    public OffsetPoint getRepresentingPoint(DungeonRoom dungeonRoom) {
        return chest;
    }
}
