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

package kr.syeyoung.dungeonsguide.dungeon.roomprocessor.waterpuzzle;

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.RoomProcessorGenerator;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import net.minecraft.util.BlockPos;

import java.awt.*;
import java.util.List;

public class RoomProcessorWaterPuzzle extends GeneralRoomProcessor {

    private boolean argumentsFulfilled = false;

    private WaterBoard waterBoard;
    private final OffsetPointSet doorsClosed;
    private final OffsetPointSet levers;
    private final OffsetPointSet frontBoard;
    private final OffsetPointSet backBoard;
    private final OffsetPoint water_lever;

    public RoomProcessorWaterPuzzle(DungeonRoom dungeonRoom) {
        super(dungeonRoom);
        frontBoard = (OffsetPointSet) dungeonRoom.getDungeonRoomInfo().getProperties().get("front");
        backBoard = (OffsetPointSet) dungeonRoom.getDungeonRoomInfo().getProperties().get("back");
        levers = (OffsetPointSet) dungeonRoom.getDungeonRoomInfo().getProperties().get("levers");
        doorsClosed = (OffsetPointSet) dungeonRoom.getDungeonRoomInfo().getProperties().get("doors");
        water_lever = (OffsetPoint) dungeonRoom.getDungeonRoomInfo().getProperties().get("water-lever");

        if (frontBoard == null || backBoard == null || levers == null || doorsClosed == null ||water_lever == null) {
           argumentsFulfilled = false;
        } else {
            argumentsFulfilled = true;

            try {
                waterBoard = new WaterBoard(this, frontBoard, backBoard, levers, doorsClosed, water_lever);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!FeatureRegistry.getInstance().SOLVER_WATERPUZZLE.isEnabled()) return;
        if (!argumentsFulfilled) return;
        try {
            waterBoard.tick();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void drawScreen(float partialTicks) {
        super.drawScreen(partialTicks);
    }

    @Override
    public void drawWorld(float partialTicks) {
        super.drawWorld(partialTicks);
        if (!FeatureRegistry.getInstance().SOLVER_WATERPUZZLE.isEnabled()) return;
        if (!argumentsFulfilled) return;
        if (waterBoard == null) return;

        Route route = waterBoard.getCurrentRoute();
        if (route != null) {
            int j = 1;
            for (int i = 0; i < route.getConditionList().size(); i++) {
                LeverState condition = route.getConditionList().get(i);
                if (condition == null) continue;
                SwitchData switchData = waterBoard.getValidSwitches().get(condition.getBlockId());
                if (switchData.getCurrentState(getDungeonRoom().getContext().getWorld()) != condition.isRequiredState()) {

                    RenderUtils.highlightBlock(switchData.getSwitchLoc(), new Color(0,255,0,50), partialTicks, true);
                    RenderUtils.drawTextAtWorld("#"+j,switchData.getSwitchLoc().getX(), switchData.getSwitchLoc().getY()+1, switchData.getSwitchLoc().getZ(),  0xFF000000,0.1f, false, false, partialTicks);
                    RenderUtils.drawTextAtWorld(condition.isRequiredState() ? "on":"off",switchData.getSwitchLoc().getX(), switchData.getSwitchLoc().getY(), switchData.getSwitchLoc().getZ(),  0xFF000000,0.1f, false, false, partialTicks);
                    j++;
                }
            }
            for (WaterNode node : route.getNodes()) {
                RenderUtils.highlightBlock(node.getBlockPos(), new Color(0,255,255,50), partialTicks, true);
            }
        }
        List<BlockPos> targets = waterBoard.getTarget();
        if (targets != null) {
            for (BlockPos target : targets) {
                RenderUtils.highlightBlock(target, new Color(0,255,255,100), partialTicks, true);
            }
            RenderUtils.highlightBlock(waterBoard.getToggleableMap().get("mainStream").getBlockPos(), new Color(0,255,0,255), partialTicks, true);
        }
    }

    public static class Generator implements RoomProcessorGenerator<RoomProcessorWaterPuzzle> {
        @Override
        public RoomProcessorWaterPuzzle createNew(DungeonRoom dungeonRoom) {
            RoomProcessorWaterPuzzle defaultRoomProcessor = new RoomProcessorWaterPuzzle(dungeonRoom);
            return defaultRoomProcessor;
        }
    }
}
