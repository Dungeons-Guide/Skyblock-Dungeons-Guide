package kr.syeyoung.dungeonsguide.roomprocessor;

import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import net.minecraft.util.IChatComponent;

public interface RoomProcessor {
    void tick();
    void drawScreen(float partialTicks);
    void drawWorld(float partialTicks);
    void chatReceived(IChatComponent chat);

    boolean readGlobalChat();
}