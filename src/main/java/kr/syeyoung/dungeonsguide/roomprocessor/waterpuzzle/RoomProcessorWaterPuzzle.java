package kr.syeyoung.dungeonsguide.roomprocessor.waterpuzzle;

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.roomprocessor.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.roomprocessor.RoomProcessor;
import kr.syeyoung.dungeonsguide.roomprocessor.RoomProcessorBlazeSolver;
import kr.syeyoung.dungeonsguide.roomprocessor.RoomProcessorGenerator;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.BlockPos;

import java.awt.*;

public class RoomProcessorWaterPuzzle extends GeneralRoomProcessor {

    private boolean argumentsFulfilled = false;

    private WaterBoard waterBoard;
    private OffsetPointSet doorsClosed;
    private OffsetPointSet levers;
    private OffsetPointSet frontBoard;
    private OffsetPointSet backBoard;
    private OffsetPoint water_lever;

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
        if (!argumentsFulfilled) return;
        if (waterBoard == null) return;

        Route route = waterBoard.getCurrentRoute();
        if (route != null) {
            for (WaterCondition condition : route.getConditionList()) {
                if (condition == null) continue;
                SwitchData switchData = waterBoard.getValidSwitches().get(condition.getBlockId());
                if (switchData.getCurrentState(getDungeonRoom().getContext().getWorld()) != condition.isRequiredState()) {
                    RenderUtils.highlightBlock(switchData.getSwitchLoc(), new Color(0,255,0,50), partialTicks);
                    RenderUtils.drawTextAtWorld(condition.isRequiredState() ? "on":"off",switchData.getSwitchLoc().getX(), switchData.getSwitchLoc().getY(), switchData.getSwitchLoc().getZ(),  0xFF000000,0.1f, false, false, partialTicks);
                }
            }
            for (WaterNode node : route.getNodes()) {
                RenderUtils.highlightBlock(node.getBlockPos(), new Color(0,255,255,50), partialTicks);
            }
        }
        BlockPos target = waterBoard.getTarget();
        if (target != null) {
            RenderUtils.highlightBlock(target, new Color(0,255,255,255), partialTicks);
            RenderUtils.highlightBlock(waterBoard.getToggleableMap().get("mainStream").getBlockPos(), new Color(0,255,0,255), partialTicks);
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
