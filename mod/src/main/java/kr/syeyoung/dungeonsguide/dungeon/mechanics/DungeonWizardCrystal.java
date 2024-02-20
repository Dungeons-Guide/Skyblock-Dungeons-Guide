/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2024  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.dungeon.mechanics;

import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.data.PrecalculatedStonk;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGBuilder;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import lombok.Data;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;

import java.awt.*;
import java.util.List;
import java.util.*;

@Data
public class DungeonWizardCrystal implements DungeonMechanic {
    private static final long serialVersionUID = 8328085181801219019L;
    private OffsetPoint secretPoint = new OffsetPoint(0, 0, 0);
    private PrecalculatedStonk secretCache;
    private List<String> preRequisite = new ArrayList<String>();

    @Override
    public void buildAction(String state, DungeonRoom dungeonRoom, ActionDAGBuilder builder) throws PathfindImpossibleException {
        if (state.equals(getCurrentState(dungeonRoom))) return;
        if (state.equalsIgnoreCase("navigate")) {
            builder = builder
                    .requires(new ActionMoveNearestAir(getRepresentingPoint(dungeonRoom)));
            for (String str : preRequisite) {
                if (str.isEmpty()) continue;
                builder.requires(new ActionChangeState(str.split(":")[0], str.split(":")[1]));
            }
            return;
        }

        if (!"obtained-self".equalsIgnoreCase(state) || getCurrentState(dungeonRoom).equals("obtained-other")) throw new PathfindImpossibleException(state+" is not valid state for secret");

        if (secretCache != null)
            ActionUtils.buildActionMoveAndClick(builder, dungeonRoom, secretCache, preRequisite, Collections.emptyList());
        else
            ActionUtils.buildActionMoveAndClick(builder, dungeonRoom, secretPoint, builder1 -> {
                for (String str : preRequisite) {
                    if (str.isEmpty()) continue;
                    builder1.optional(new ActionChangeState(str.split(":")[0], str.split(":")[1]));
                }
                return null;
            });
    }

    @Override
    public void highlight(Color color, String name, DungeonRoom dungeonRoom, float partialTicks) {
        BlockPos pos = secretPoint.getBlockPos(dungeonRoom);
        RenderUtils.highlightBlock(pos, color,partialTicks);
        RenderUtils.drawTextAtWorld(name, pos.getX() +0.5f, pos.getY()+0.75f, pos.getZ()+0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
        RenderUtils.drawTextAtWorld(getCurrentState(dungeonRoom), pos.getX() +0.5f, pos.getY()+0.25f, pos.getZ()+0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        DungeonWizardCrystal dungeonSecret = new DungeonWizardCrystal();
        dungeonSecret.secretPoint = (OffsetPoint) secretPoint.clone();
        dungeonSecret.preRequisite = new ArrayList<String>(preRequisite);
        dungeonSecret.secretCache = secretCache;
        return dungeonSecret;
    }
    @Override
    public String getCurrentState(DungeonRoom dungeonRoom) {
        for (ItemStack stack : Minecraft.getMinecraft().thePlayer.inventory.mainInventory) {
            if (stack == null) continue;
            if (stack.getItem() != Items.skull) continue;
            if (stack.getDisplayName().equals("§9Wizard's Crystal")) return "obtained-self";
        }
        if (secretPoint.getBlock(dungeonRoom) == Blocks.skull) {
            return "unobtained";
        }
        return "obtained-other";
    }

    @Override
    public Set<String> getPossibleStates(DungeonRoom dungeonRoom) {
        if (secretPoint.getBlock(dungeonRoom) == Blocks.skull) {
            return Sets.newHashSet("obtained-self", "navigate");
        }
        return Sets.newHashSet("navigate");
    }

    @Override
    public Set<String> getTotalPossibleStates(DungeonRoom dungeonRoom) {
        return Sets.newHashSet("obtained-self", "unobtained", "obtained-other");
    }

    @Override
    public OffsetPoint getRepresentingPoint(DungeonRoom dungeonRoom) {
        return secretPoint;
    }
}
