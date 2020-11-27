package kr.syeyoung.dungeonsguide.roomprocessor;

import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.roomprocessor.waterpuzzle.RoomProcessorWaterPuzzle;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.IChatComponent;

public class GeneralRoomProcessor implements RoomProcessor {

    @Getter
    @Setter
    private DungeonRoom dungeonRoom;
    public GeneralRoomProcessor(DungeonRoom dungeonRoom) {
        this.dungeonRoom = dungeonRoom;
    }

    @Override
    public void tick() {

    }

    @Override
    public void drawScreen(float partialTicks) {

    }

    @Override
    public void drawWorld(float partialTicks) {

    }

    @Override
    public void chatReceived(IChatComponent chat) {

    }

    public static class Generator implements RoomProcessorGenerator<GeneralRoomProcessor> {
        @Override
        public GeneralRoomProcessor createNew(DungeonRoom dungeonRoom) {
            GeneralRoomProcessor defaultRoomProcessor = new GeneralRoomProcessor(dungeonRoom);
            return defaultRoomProcessor;
        }
    }
}
