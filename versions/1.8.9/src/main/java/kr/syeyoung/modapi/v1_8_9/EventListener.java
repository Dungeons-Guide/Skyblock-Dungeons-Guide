package kr.syeyoung.modapi.v1_8_9;

import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.EnumFacing;
import kr.syeyoung.modapi.data.Vector3D;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.entity.UEntityLiving;
import kr.syeyoung.modapi.entity.UEntityPlayer;
import kr.syeyoung.modapi.event.events.ClientTickEvent;
import kr.syeyoung.modapi.event.events.EntityEnterWorldEvent;
import kr.syeyoung.modapi.event.events.LivingEntityDeathEvent;
import kr.syeyoung.modapi.event.events.LivingEntityTickEvent;
import kr.syeyoung.modapi.v1_8_9.entity.UEntityDelegateFactory;
import kr.syeyoung.modapi.v1_8_9.world.UWorldImpl;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
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
    public void onEntityDeath(LivingDeathEvent deathEvent) {

        ModAPI.getAPI().getEventBus().fireEvent(new LivingEntityDeathEvent(
                (UEntityLiving) UEntityDelegateFactory.createEntityFor(deathEvent.entityLiving)
        ));
    }
    @SubscribeEvent
    public void onEntityDeath(EntityJoinWorldEvent joinWorldEvent) {
        ModAPI.getAPI().getEventBus().fireEvent(new EntityEnterWorldEvent(
                UEntityDelegateFactory.createEntityFor(joinWorldEvent.entity)
        ));
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent ev) {
        if (ev.phase != TickEvent.Phase.START) return;
        ModAPI.getAPI().getEventBus().fireEvent(new ClientTickEvent());
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.world.isRemote) return;
        kr.syeyoung.modapi.event.events.PlayerInteractEvent interactEvent = new kr.syeyoung.modapi.event.events.PlayerInteractEvent(
                (UEntityPlayer) UEntityDelegateFactory.createEntityFor(event.entityPlayer),
                event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR ? kr.syeyoung.modapi.event.events.PlayerInteractEvent.Action.RIGHT_CLICK_AIR
                        : event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK ? kr.syeyoung.modapi.event.events.PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK :
                        kr.syeyoung.modapi.event.events.PlayerInteractEvent.Action.LEFT_CLICK_BLOCK,
                new UWorldImpl(event.world),
                event.pos == null ? null : new VectorI3D(event.pos.getX(), event.pos.getY(), event.pos.getZ()),
                event.face == null ? null : EnumFacing.VALUES[event.face.getIndex()],
                event.localPos == null ? null : new Vector3D(event.localPos.xCoord, event.localPos.yCoord, event.localPos.zCoord)
        );
        ModAPI.getAPI().getEventBus().fireEvent(interactEvent);
    }
}
