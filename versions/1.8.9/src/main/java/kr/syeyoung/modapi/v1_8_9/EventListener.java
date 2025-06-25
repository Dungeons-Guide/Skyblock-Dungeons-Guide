package kr.syeyoung.modapi.v1_8_9;

import kr.syeyoung.modapi.event.events.ClientTickEvent;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.entity.UEntityLiving;
import kr.syeyoung.modapi.event.events.LivingEntityTickEvent;
import kr.syeyoung.modapi.v1_8_9.entity.UEntityDelegateFactory;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class EventListener {

    @SubscribeEvent
    public void onEntityTick(LivingEvent.LivingUpdateEvent updateEvent) {
        ModAPI.getAPI().getEventBus().fireEvent(new LivingEntityTickEvent(
                (UEntityLiving) UEntityDelegateFactory.createEntityFor(updateEvent.entityLiving)
        ));
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent ev) {
        if (ev.phase != TickEvent.Phase.START) return;
        ModAPI.getAPI().getEventBus().fireEvent(new ClientTickEvent());
    }
}
