package kr.syeyoung.dungeonsguide;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class EventListener {
    private int timerTick = 0;
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.START) {
            timerTick ++;
            if (timerTick % 5 == 0) {
                DungeonsGuide.getDungeonsGuide().getSkyblockStatus().updateStatus();
            }
        }
    }
}
