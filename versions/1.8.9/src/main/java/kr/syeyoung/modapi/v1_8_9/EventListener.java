package kr.syeyoung.modapi.v1_8_9;

import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.EnumFacing;
import kr.syeyoung.modapi.data.Vector3D;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.entity.UEntityLiving;
import kr.syeyoung.modapi.entity.UEntityPlayer;
import kr.syeyoung.modapi.event.ListenerPriority;
import kr.syeyoung.modapi.event.events.ClientTickEvent;
import kr.syeyoung.modapi.event.events.EntityEnterWorldEvent;
import kr.syeyoung.modapi.event.events.LivingEntityDeathEvent;
import kr.syeyoung.modapi.event.events.LivingEntityTickEvent;
import kr.syeyoung.modapi.v1_8_9.entity.UEntityDelegateFactory;
import kr.syeyoung.modapi.v1_8_9.world.BlockStateRegistryImpl;
import kr.syeyoung.modapi.v1_8_9.world.UWorldImpl;
import lombok.AllArgsConstructor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class EventListener {

    public void onEntityTick(LivingEvent.LivingUpdateEvent updateEvent, EventPriority priority) {
        ModAPI.getAPI().getEventBus().fireEvent(new LivingEntityTickEvent(
                (UEntityLiving) UEntityDelegateFactory.createEntityFor(updateEvent.entityLiving)
        ), mapPriority(priority));
    }

    public void onEntityDeath(LivingDeathEvent deathEvent, EventPriority priority) {

        ModAPI.getAPI().getEventBus().fireEvent(new LivingEntityDeathEvent(
                (UEntityLiving) UEntityDelegateFactory.createEntityFor(deathEvent.entityLiving)
        ), mapPriority(priority));
    }

    public void onEntityJoinWorld(EntityJoinWorldEvent joinWorldEvent, EventPriority priority) {
        ModAPI.getAPI().getEventBus().fireEvent(new EntityEnterWorldEvent(
                UEntityDelegateFactory.createEntityFor(joinWorldEvent.entity)
        ), mapPriority(priority));
    }

    public void onClientTick(TickEvent.ClientTickEvent ev, EventPriority priority) {
        if (ev.phase != TickEvent.Phase.START) return;
        ModAPI.getAPI().getEventBus().fireEvent(new ClientTickEvent(), mapPriority(priority));
    }

    public void onPlayerInteract(PlayerInteractEvent event, EventPriority priority) {
        if (!event.world.isRemote) return;
        kr.syeyoung.modapi.event.events.PlayerInteractEvent interactEvent = new kr.syeyoung.modapi.event.events.PlayerInteractEvent(
                (UEntityPlayer) UEntityDelegateFactory.createEntityFor(event.entityPlayer),
                event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR ? kr.syeyoung.modapi.event.events.PlayerInteractEvent.Action.RIGHT_CLICK_AIR
                        : event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK ? kr.syeyoung.modapi.event.events.PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK :
                        kr.syeyoung.modapi.event.events.PlayerInteractEvent.Action.LEFT_CLICK_BLOCK,
                new UWorldImpl(event.world, (BlockStateRegistryImpl) ModAPI.getAPI().getBlockRegistry()),
                event.pos == null ? null : new VectorI3D(event.pos.getX(), event.pos.getY(), event.pos.getZ()),
                event.face == null ? null : EnumFacing.VALUES[event.face.getIndex()],
                event.localPos == null ? null : new Vector3D(event.localPos.xCoord, event.localPos.yCoord, event.localPos.zCoord)
        );
        ModAPI.getAPI().getEventBus().fireEvent(interactEvent, mapPriority(priority));
    }

    private ListenerPriority mapPriority(EventPriority priority) {
        switch (priority) {
            case HIGHEST: return ListenerPriority.FIRST;
            case HIGH: return ListenerPriority.SECOND;
            case NORMAL: return ListenerPriority.THIRD;
            case LOW: return ListenerPriority.FOURTH;
            case LOWEST: return ListenerPriority.LAST;
        }
        return null;
    }


    private static final int busID;
    static {
        try {
            Field f = EventBus.class.getDeclaredField("busID");
            f.setAccessible(true);
            busID = (int) f.get(MinecraftForge.EVENT_BUS);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @AllArgsConstructor
    public static class DGBridgeEventListener<T extends Event> implements IEventListener {
        private BiConsumer<T, EventPriority> runner;
        private EventPriority priority;
        private ListenerList listenerList;

        @Override
        public void invoke(Event event) {
            runner.accept((T)event, priority);
        }
    }

    private List<DGBridgeEventListener<?>> listeners = new ArrayList<>();

    public <T extends Event> void registerEvents(Class<T> clazz, BiConsumer<T, EventPriority> runner) {
        try {
            Event ev = clazz.getConstructor().newInstance();
            for (EventPriority value : EventPriority.values()) {
                DGBridgeEventListener<?> bridgeEventListener = new DGBridgeEventListener<>(runner, value, ev.getListenerList());
                ev.getListenerList().register(busID, value, bridgeEventListener);
                listeners.add(bridgeEventListener);
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public void register() {
        registerEvents(LivingEvent.LivingUpdateEvent.class, this::onEntityTick);
        registerEvents(LivingDeathEvent.class, this::onEntityDeath);
        registerEvents(EntityJoinWorldEvent.class, this::onEntityJoinWorld);
        registerEvents(TickEvent.ClientTickEvent.class, this::onClientTick);
        registerEvents(PlayerInteractEvent.class, this::onPlayerInteract);
    }

    public void unregister() {
        for (DGBridgeEventListener<?> listener : listeners) {
            listener.listenerList.unregister(busID, listener);
        }
        listeners.clear();
    }
}
