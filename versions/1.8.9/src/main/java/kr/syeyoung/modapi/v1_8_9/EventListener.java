package kr.syeyoung.modapi.v1_8_9;

import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCollectDiagnostics;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.EnumFacing;
import kr.syeyoung.modapi.data.ResourceIdentifier;
import kr.syeyoung.modapi.data.Vector3D;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.entity.UEntityLiving;
import kr.syeyoung.modapi.entity.UEntityPlayer;
import kr.syeyoung.modapi.event.ListenerPriority;
import kr.syeyoung.modapi.event.events.*;
import kr.syeyoung.modapi.gui.UCustomGuiScreen;
import kr.syeyoung.modapi.v1_8_9.entity.UEntityDelegateFactory;
import kr.syeyoung.modapi.v1_8_9.gui.UGuiScreenAdapter;
import kr.syeyoung.modapi.v1_8_9.gui.UNativeGuiScreen;
import kr.syeyoung.modapi.v1_8_9.item.UItemStackImpl;
import kr.syeyoung.modapi.v1_8_9.paralleluniverse.scoreboard.ScoreboardManager;
import kr.syeyoung.modapi.v1_8_9.paralleluniverse.tab.TabList;
import kr.syeyoung.modapi.v1_8_9.paralleluniverse.teams.TeamManager;
import kr.syeyoung.modapi.v1_8_9.render.UGuiRenderContextImpl;
import kr.syeyoung.modapi.v1_8_9.render.UWorldRenderContextImpl;
import kr.syeyoung.modapi.v1_8_9.util.KeyboardModernizer;
import kr.syeyoung.modapi.v1_8_9.util.MarkedChatComponent;
import kr.syeyoung.modapi.v1_8_9.util.RenderUtils;
import kr.syeyoung.modapi.v1_8_9.world.BlockStateRegistryImpl;
import kr.syeyoung.modapi.v1_8_9.world.UWorldImpl;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
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
        interactEvent.setCanceled(event.isCanceled());
        ModAPI.getAPI().getEventBus().fireEvent(interactEvent, mapPriority(priority));
        event.setCanceled(interactEvent.isCanceled());
    }

    public void onWorldUnload(WorldEvent.Unload event, EventPriority priority) {
        if (priority == EventPriority.HIGHEST) {
            TabList.INSTANCE.clear();
            TeamManager.INSTANCE.clear();
            ScoreboardManager.INSTANCE.clear();
        }
        ModAPI.getAPI().getEventBus().fireEvent(new WorldUnloadEvent(), mapPriority(priority));
    }

    public void onChat(ClientChatReceivedEvent receivedEvent, EventPriority priority) {
        Component c = GsonComponentSerializer.colorDownsamplingGson().deserialize(IChatComponent.Serializer.componentToJson(receivedEvent.message));
        if (receivedEvent.type == 2) {
            ActionBarReceivedEvent event = new ActionBarReceivedEvent(c,c);
            ModAPI.getAPI().getEventBus().fireEvent(event, mapPriority(priority));
            if (event.chat != event.original) {
                receivedEvent.message = IChatComponent.Serializer.jsonToComponent(GsonComponentSerializer.colorDownsamplingGson().serialize(event.chat));
            }
        } else {
            ChatReceivedEvent event = new ChatReceivedEvent(c,c, receivedEvent.type == 1, receivedEvent.isCanceled());
            ModAPI.getAPI().getEventBus().fireEvent(event, mapPriority(priority));
            receivedEvent.setCanceled(event.isCanceled());
            if (event.chat != event.original) {
                receivedEvent.message = IChatComponent.Serializer.jsonToComponent(GsonComponentSerializer.colorDownsamplingGson().serialize(event.chat));
            }
        }
    }


    private ThreadLocal<Stack<PlayerNameFormatEvent>> nameFormatEvents = ThreadLocal.withInitial(Stack::new);

    public void onNameFormat(PlayerEvent.NameFormat nameFormat, EventPriority priority) {
        if (priority == EventPriority.HIGHEST) nameFormatEvents.get().push(new PlayerNameFormatEvent(
                (UEntityPlayer) UEntityDelegateFactory.createEntityFor(nameFormat.entityPlayer),
                nameFormat.displayname,
                nameFormat.username
        ));
        try {
            nameFormatEvents.get().peek().setDisplayName(nameFormat.displayname);
            ModAPI.getAPI().getEventBus().fireEvent(nameFormatEvents.get().peek(), mapPriority(priority));

            nameFormat.displayname = nameFormatEvents.get().peek().displayName;

            if (priority == EventPriority.LOWEST) {
                PlayerNameFormatEvent result = nameFormatEvents.get().peek();
                nameFormat.entityPlayer.getPrefixes().removeIf(iChatComponent -> iChatComponent instanceof MarkedChatComponent);
                for (Component prefix : result.getPrefix()) {
                    nameFormat.entityPlayer.getPrefixes().add(
                            new MarkedChatComponent("")
                                    .appendSibling(IChatComponent.Serializer.jsonToComponent(GsonComponentSerializer.colorDownsamplingGson().serialize(prefix)))
                    );
                }
            }
        } finally {
            if (priority == EventPriority.LOWEST) {
                nameFormatEvents.get().pop();
            }
        }
    }

    public void onItemToolip(net.minecraftforge.event.entity.player.ItemTooltipEvent itemTooltipEvent, EventPriority eventPriority) {
        ItemTooltipEvent itemTooltipEvent1 = new ItemTooltipEvent(
                itemTooltipEvent.showAdvancedItemTooltips,
                new UItemStackImpl(itemTooltipEvent.itemStack),
                itemTooltipEvent.toolTip
        );
        ModAPI.getAPI().getEventBus().fireEvent(itemTooltipEvent1, mapPriority(eventPriority));
    }

    public void onGuiOpen(net.minecraftforge.client.event.GuiOpenEvent guiOpenEvent, EventPriority eventPriority) {
        GuiOpenEvent openEvent = new GuiOpenEvent(
                guiOpenEvent.gui instanceof UGuiScreenAdapter ? ((UGuiScreenAdapter) guiOpenEvent.gui).getDelegate() : UNativeGuiScreen.getUScreen(guiOpenEvent.gui)
        );


        ModAPI.getAPI().getEventBus().fireEvent(openEvent, mapPriority(eventPriority));
        if (openEvent.getGui() instanceof UNativeGuiScreen) {
            guiOpenEvent.gui = ((UNativeGuiScreen) openEvent.getGui()).getHandle();
        } else if (openEvent.getGui() instanceof UCustomGuiScreen) {
            if (Minecraft.getMinecraft().currentScreen instanceof UGuiScreenAdapter) {
                if (((UGuiScreenAdapter) Minecraft.getMinecraft().currentScreen).getDelegate() == openEvent.getGui()) {
                    guiOpenEvent.gui = Minecraft.getMinecraft().currentScreen; // don't change ref
                    return;
                }
            }
            guiOpenEvent.gui = new UGuiScreenAdapter((UCustomGuiScreen) openEvent.getGui());
        } else if (openEvent.getGui() == null) {
            guiOpenEvent.gui = null;
        }
    }

    public void onKeyboardInputEvent(GuiScreenEvent.KeyboardInputEvent inputEvent, EventPriority eventPriority) {
        if (Keyboard.getEventKeyState()) {
            if (!Keyboard.isRepeatEvent()) {
                ModAPI.getAPI().getEventBus().fireEvent(new ScreenKeyboardEvent.KeyPressed(
                        KeyboardModernizer.getKeyCode(),
                        KeyboardModernizer.getScanCode(),
                        KeyboardModernizer.getModifiers()
                ), mapPriority(eventPriority));
            }
        } else {
            ModAPI.getAPI().getEventBus().fireEvent(new ScreenKeyboardEvent.KeyReleased(
                    KeyboardModernizer.getKeyCode(),
                    KeyboardModernizer.getScanCode(),
                    KeyboardModernizer.getModifiers()
            ), mapPriority(eventPriority));
        }
    }


    private int lastMouseEventButton;
    private long lastMouseEventTime;
    private int lastX, lastY;
    public void onMouseInputEvent(GuiScreenEvent.MouseInputEvent.Pre mouseInputEvent, EventPriority eventPriority) {
        int i = Mouse.getEventX();

        int j = mouseInputEvent.gui.mc.displayHeight - Mouse.getEventY();
        int k = Mouse.getEventButton();

        if (Mouse.getEventButtonState()) {
            boolean isCanceled = ModAPI.getAPI().getEventBus().fireEvent(new ScreenMouseEvent.MouseClicked(
                    i, j, k, mouseInputEvent.isCanceled()
            ));
            mouseInputEvent.setCanceled(isCanceled);

            this.lastMouseEventButton = k;
            this.lastMouseEventTime = Minecraft.getSystemTime();
        } else if (k != -1) {
            this.lastMouseEventButton = -1;
            ModAPI.getAPI().getEventBus().fireEvent(new ScreenMouseEvent.MouseReleased(
                    i, j, k
            ));
        } else if (this.lastMouseEventButton != -1 && this.lastMouseEventTime > 0L) {
            long l = Minecraft.getSystemTime() - this.lastMouseEventTime;
            ModAPI.getAPI().getEventBus().fireEvent(new ScreenMouseEvent.MouseDragged(
                    i,j, lastX - i, lastY - j, k
            ));
        }
        if (lastX != i || lastY != j) {
            ModAPI.getAPI().getEventBus().fireEvent(new ScreenMouseEvent.MouseMoved(
                    i, j
            ));
        }

        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            try {
                boolean cancel = ModAPI.getAPI().getEventBus().fireEvent(new ScreenMouseEvent.MouseScrolled(
                        i, j, wheel, wheel, mouseInputEvent.isCanceled()
                ));
                mouseInputEvent.setCanceled(cancel);
            } catch (Exception e) {
                FeatureCollectDiagnostics.queueSendLogAsync(e);
                e.printStackTrace();
            }
        }
        lastX = i;
        lastY = j;
    }

    public void onOverlayRender(RenderGameOverlayEvent.Post event, EventPriority priority) {
        if (!(event.type == RenderGameOverlayEvent.ElementType.ALL))
            return;

        RenderUtils.preRenderGui();
        ModAPI.getAPI().getEventBus().fireEvent(new OverlayRenderEvent(event.partialTicks, new UGuiRenderContextImpl()), mapPriority(priority));
        RenderUtils.postRenderGui();

        GlStateManager.enableBlend();
        GlStateManager.color(1, 1, 1, 1);
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        Minecraft.getMinecraft().entityRenderer.setupOverlayRendering();
        GlStateManager.enableAlpha();
    }
    public void onScreenRenderPre(GuiScreenEvent.DrawScreenEvent.Pre event, EventPriority priority) {
        RenderUtils.preRenderGui();
        ModAPI.getAPI().getEventBus().fireEvent(new ScreenRenderEvent.Pre(
                event.gui instanceof UGuiScreenAdapter ? ((UGuiScreenAdapter) event.gui).getDelegate() :
                UNativeGuiScreen.getUScreen(event.gui), event.renderPartialTicks, new UGuiRenderContextImpl()), mapPriority(priority));
        RenderUtils.postRenderGui();
    }

    public void onScreenRenderPost(GuiScreenEvent.DrawScreenEvent.Post event, EventPriority priority) {
        RenderUtils.preRenderGui();
        ModAPI.getAPI().getEventBus().fireEvent(new ScreenRenderEvent.Post(
                event.gui instanceof UGuiScreenAdapter ? ((UGuiScreenAdapter) event.gui).getDelegate() :
                UNativeGuiScreen.getUScreen(event.gui),event.renderPartialTicks, new UGuiRenderContextImpl()), mapPriority(priority));
        RenderUtils.postRenderGui();
    }
    public void onScreenInitPost(GuiScreenEvent.InitGuiEvent.Post event, EventPriority priority) {
        ModAPI.getAPI().getEventBus().fireEvent(new ScreenInitEvent(
                event.gui instanceof UGuiScreenAdapter ? ((UGuiScreenAdapter) event.gui).getDelegate() :
                        UNativeGuiScreen.getUScreen(event.gui)), mapPriority(priority));
    }

    public void onRenderLiving(net.minecraftforge.client.event.RenderLivingEvent.Pre event, EventPriority priority) {
        boolean canceled = ModAPI.getAPI().getEventBus().fireEvent(new RenderLivingEvent(
                (UEntityLiving) UEntityDelegateFactory.createEntityFor(event.entity),
                event.isCanceled()
        ), mapPriority(priority));
        event.setCanceled(canceled);
    }

    public void onRenderWorldLast(RenderWorldLastEvent event, EventPriority priority) {
        ModAPI.getAPI().getEventBus().fireEvent(new RenderWorldEvent(
                event.partialTicks,
                UWorldRenderContextImpl.INSTANCE
        ), mapPriority(priority));
    }

    public void onPlaySound(net.minecraftforge.client.event.sound.PlaySoundEvent event, EventPriority priority) {
        PlaySoundEvent event1 = new PlaySoundEvent(new PlaySoundEvent.Sound(
                new ResourceIdentifier(event.sound.getSoundLocation().getResourceDomain(), event.sound.getSoundLocation().getResourcePath()),
                event.sound.getXPosF(),
                event.sound.getYPosF(),
                event.sound.getZPosF(),
                event.sound.getPitch(),
                event.sound.getVolume()
        ), event.result == null ? null : new PlaySoundEvent.Sound(
                new ResourceIdentifier(event.result.getSoundLocation().getResourceDomain(), event.result.getSoundLocation().getResourcePath()),
                event.result.getXPosF(),
                event.result.getYPosF(),
                event.result.getZPosF(),
                event.result.getPitch(),
                event.result.getVolume()
        ));
        ModAPI.getAPI().getEventBus().fireEvent(event1, mapPriority(priority));

        if (event1.getResult() != null) {
            event.result = new PositionedSoundRecord(
                    new ResourceLocation(event1.getResult().getSoundName().getMod(), event1.getResult().getSoundName().getLocation()),
                    event1.getResult().getVolume(),
                    event1.getResult().getPitch(),
                    event1.getResult().getXPos(),
                    event1.getResult().getYPos(),
                    event1.getResult().getZPos()
            );
        }
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
        registerEvents(WorldEvent.Unload.class, this::onWorldUnload);
        registerEvents(ClientChatReceivedEvent.class, this::onChat);
        registerEvents(PlayerEvent.NameFormat.class, this::onNameFormat);
        registerEvents(net.minecraftforge.event.entity.player.ItemTooltipEvent.class, this::onItemToolip);
        registerEvents(net.minecraftforge.client.event.GuiOpenEvent.class, this::onGuiOpen);
        registerEvents(GuiScreenEvent.KeyboardInputEvent.class, this::onKeyboardInputEvent);
        registerEvents(GuiScreenEvent.MouseInputEvent.Pre.class, this::onMouseInputEvent);
        registerEvents(RenderGameOverlayEvent.Post.class, this::onOverlayRender);
        registerEvents(GuiScreenEvent.DrawScreenEvent.Pre.class, this::onScreenRenderPre);
        registerEvents(GuiScreenEvent.DrawScreenEvent.Post.class, this::onScreenRenderPost);
        registerEvents(GuiScreenEvent.InitGuiEvent.Post.class, this::onScreenInitPost);
        registerEvents(net.minecraftforge.client.event.RenderLivingEvent.Pre.class, this::onRenderLiving);
        registerEvents(net.minecraftforge.client.event.sound.PlaySoundEvent.class, this::onPlaySound);
        registerEvents(RenderWorldLastEvent.class, this::onRenderWorldLast);
    }

    public void unregister() {
        for (DGBridgeEventListener<?> listener : listeners) {
            listener.listenerList.unregister(busID, listener);
        }
        listeners.clear();


        List<ListenerList> all = ReflectionHelper.getPrivateValue(ListenerList.class, null, "allLists");
        int busId = ReflectionHelper.getPrivateValue(EventBus.class, MinecraftForge.EVENT_BUS, "busID");
        for (ListenerList listenerList : all) {
            Object[] list = ReflectionHelper.getPrivateValue(ListenerList.class, listenerList, "lists");
            Object inst = list[busId];
            try {
                Method m = inst.getClass().getDeclaredMethod("buildCache"); // refresh cache
                m.setAccessible(true);
                m.invoke(inst);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
