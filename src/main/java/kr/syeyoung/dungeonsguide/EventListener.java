package kr.syeyoung.dungeonsguide;

import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class EventListener {
    private int timerTick = 0;
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.START) {
            timerTick ++;
            if (timerTick % 5 == 0) {
                SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();
                boolean isOnDungeon = skyblockStatus.isOnDungeon();
                skyblockStatus.updateStatus();
                if (!skyblockStatus.isOnDungeon()) {
                    skyblockStatus.setContext(null);
                    return;
                }
                if (isOnDungeon) skyblockStatus.getContext().tick();
                else skyblockStatus.setContext(new DungeonContext(Minecraft.getMinecraft().thePlayer.worldObj));
            }
        }
    }
}
