package kr.syeyoung.dungeonsguide.roomprocessor;

import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import net.minecraft.util.IChatComponent;

public class RoomProcessorRiddle extends GeneralRoomProcessor {

    public RoomProcessorRiddle(DungeonRoom dungeonRoom) {
        super(dungeonRoom);
    }

    @Override
    public void chatReceived(IChatComponent chat) {
        System.out.println("event! +"+chat);
        super.chatReceived(chat);
        String ch2 = chat.getUnformattedText();
        System.out.println("hey::"+ch2);

    }


    public static class Generator implements RoomProcessorGenerator<RoomProcessorRiddle> {
        @Override
        public RoomProcessorRiddle createNew(DungeonRoom dungeonRoom) {
            RoomProcessorRiddle defaultRoomProcessor = new RoomProcessorRiddle(dungeonRoom);
            return defaultRoomProcessor;
        }
    }
}
