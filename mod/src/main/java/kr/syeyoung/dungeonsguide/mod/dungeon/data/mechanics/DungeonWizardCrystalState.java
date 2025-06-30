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

package kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics;

import com.google.common.collect.Sets;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.ActionChangeState;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.ActionMoveNearestAir;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.ActionUtils;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.PathfindImpossibleException;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGBuilder;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.PrecalculatedStonk;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicData;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicState;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSetting;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.item.Item;
import kr.syeyoung.modapi.item.UItemStack;
import kr.syeyoung.modapi.world.BlockType;
import lombok.Data;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Data
public class DungeonWizardCrystalState implements DungeonMechanicState {
    private final DungeonWizardCrystalData data;
    private final DungeonRoom room;

    public DungeonWizardCrystalState(DungeonWizardCrystalData data, DungeonRoom room) {
        this.data = data;
        this.room = room;
    }

    @Override
    public void buildAction(String action, ActionDAGBuilder builder, AlgorithmSetting algorithmSetting) throws PathfindImpossibleException {
        if (action.equals(getCurrentState())) return;
        if (action.equalsIgnoreCase("navigate")) {
            builder = builder
                    .requires(new ActionMoveNearestAir(getRepresentingPoint()), algorithmSetting);
            for (String str : data.preRequisite) {
                if (str.isEmpty()) continue;
                builder.requires(new ActionChangeState(str.split(":")[0], str.split(":")[1]), algorithmSetting);
            }
            return;
        }

        if (!"obtained-self".equalsIgnoreCase(action) || getCurrentState().equals("obtained-other"))
            throw new PathfindImpossibleException(action + " is not valid state for secret");

        if (data.secretCache != null)
            ActionUtils.buildActionMoveAndClick(builder, room, data.secretCache, data.preRequisite, Collections.emptyList(), algorithmSetting);
        else
            ActionUtils.buildActionMoveAndClick(builder, room, data.secretPoint, builder1 -> {
                for (String str : data.preRequisite) {
                    if (str.isEmpty()) continue;
                    builder1.optional(new ActionChangeState(str.split(":")[0], str.split(":")[1]), algorithmSetting);
                }
                return null;
            }, algorithmSetting);
    }

    @Override
    public void highlight(Color color, String name, float partialTicks) {
        VectorI3D pos = data.secretPoint.getBlockPos(room);
        RenderUtils.highlightBlockStencil(pos, partialTicks, color, false);
        RenderUtils.drawTextAtWorld(name, pos.getX() + 0.5f, pos.getY() + 0.75f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);
        RenderUtils.drawTextAtWorld(getCurrentState(), pos.getX() + 0.5f, pos.getY() + 0.25f, pos.getZ() + 0.5f, 0xFFFFFFFF, 0.03f, false, true, partialTicks);

        if (data.secretCache != null && FeatureRegistry.DEBUG_ST.isEnabled())
            data.secretCache.render(partialTicks, room);
    }

    @Override
    public String getCurrentState() {

        for (UItemStack stack : ModAPI.getAPI().getPlayer().getInventory().getMainInventory()) {
            if (stack == null) continue;
            if (stack.getItem() != Item.SKULL) continue;
            if (stack.getDisplayName().equals("§9Wizard's Crystal")) return "obtained-self";
        }
        if (data.secretPoint.getBlock(room).isOf(BlockType.SKULL)) {
            return "unobtained";
        }
        return "obtained-other";
    }

    @Override
    public Set<String> getAvailableActions() {
        if (data.secretPoint.getBlock(room).isOf(BlockType.SKULL)) {
            return Sets.newHashSet("obtained-self", "navigate");
        }
        return Sets.newHashSet("navigate");
    }

    @Override
    public Set<String> getTotalPossibleStates() {
        return Sets.newHashSet("obtained-self", "unobtained", "obtained-other");
    }

    @Override
    public OffsetPoint getRepresentingPoint() {
        return data.secretPoint;
    }

    @Data
    public static class DungeonWizardCrystalData implements DungeonMechanicData {
        private OffsetPoint secretPoint = new OffsetPoint(0, 0, 0);
        private PrecalculatedStonk secretCache;
        private List<String> preRequisite = new ArrayList<String>();


        public DungeonWizardCrystalData() {
        }

        @Override
        public DungeonWizardCrystalState createState(DungeonRoom room) {
            return new DungeonWizardCrystalState(this, room);
        }

        @Override
        public DungeonWizardCrystalData clone() throws CloneNotSupportedException {
            DungeonWizardCrystalData data = new DungeonWizardCrystalData();
            data.secretPoint = (OffsetPoint) secretPoint.clone();
            data.preRequisite = new ArrayList<String>(preRequisite);
            data.secretCache = secretCache;
            ;
            return data;
        }
    }
}
