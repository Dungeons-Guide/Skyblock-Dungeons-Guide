package kr.syeyoung.modapi.v1_21_11;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.EnumFacing;
import kr.syeyoung.modapi.data.Vector3D;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.entity.UEntityLiving;
import kr.syeyoung.modapi.entity.UEntityPlayer;
import kr.syeyoung.modapi.event.events.*;
import kr.syeyoung.modapi.v1_21_11.entity.UEntityDelegateFactory;
import kr.syeyoung.modapi.v1_21_11.paralleluniverse.scoreboard.ScoreboardManager;
import kr.syeyoung.modapi.v1_21_11.paralleluniverse.tab.TabList;
import kr.syeyoung.modapi.v1_21_11.paralleluniverse.teams.TeamManager;
import kr.syeyoung.modapi.v1_21_11.util.TextUtils;
import kr.syeyoung.modapi.v1_21_11.world.BlockStateRegistryImpl;
import kr.syeyoung.modapi.v1_21_11.world.UWorldImpl;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.*;
import net.kyori.adventure.text.Component;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.Window;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class EventListener {


    public void init() {
        ClientTickEvents.START_CLIENT_TICK.register(this::onTick);
        ClientEntityEvents.ENTITY_LOAD.register(this::onEntityLoad);
        ClientEntityEvents.ENTITY_UNLOAD.register(this::onEntityUnload);
        ServerLivingEntityEvents.AFTER_DEATH.register(this::onEntityKilled); // WAIT WHAT??? yes it works.
        UseBlockCallback.EVENT.register(this::onUseBlock);
        UseEntityCallback.EVENT.register(this::onUseEntity);
        UseItemCallback.EVENT.register(this::onUseItem);
        AttackBlockCallback.EVENT.register(this::onAttackBlock);
        AttackEntityCallback.EVENT.register(this::onAttackEntity);
        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register(this::onWorldChange);
        ScreenEvents.BEFORE_INIT.register((mc, screen, w, h) -> {
            ScreenKeyboardEvents.beforeKeyPress(screen).register(this::onKeyPress);
            ScreenKeyboardEvents.beforeKeyRelease(screen).register(this::onKeyRelease);
            ScreenEvents.beforeTick(screen).register(this::onScreenTick);
            ScreenMouseEvents.allowMouseClick(screen).register(this::onScreenClick);
            ScreenMouseEvents.allowMouseScroll(screen).register(this::onScreenScroll);
            ScreenMouseEvents.allowMouseRelease(screen).register(this::onScreenMouseRelease);
        });
        HudLayerRegistrationCallback.EVENT.register((a) -> {
            a.attachLayerAfter(IdentifiedLayer.DEBUG, IdentifiedLayer.of(Identifier.of("dungeonsguide", "hud"), (ctx, counter) -> {
//                ModAPI.getAPI().getEventBus().fireEvent(new )
//                ctx.ctx
//                EditBoxWidget
//                        welp i have to do 1.21 support for 1.8 mod... and i alrdy hate this rendering system
            }));
        });
        RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(null, null)
                .setPipeline(RenderPipeline.builder().build());
    }

    private void onScreenTick(Screen screen) {
        Mouse mouse = MinecraftClient.getInstance().mouse;
        Window window = MinecraftClient.getInstance().getWindow();
        double f = mouse.getScaledX(window);
        double g = mouse.getScaledY(window);

        ModAPI.getAPI().getEventBus().fireEvent(new ScreenMouseEvent.MouseMoved(
                f, g
        ));

        if (mouse.activeButton != -1 && mouse.glfwTime > 0.0) {
            double h = mouse.scaleX(window, mouse.cursorDeltaX);
            double i = mouse.scaleY(window, mouse.cursorDeltaY);

            ModAPI.getAPI().getEventBus().fireEvent(new ScreenMouseEvent.MouseDragged(
                    f,g, (int) h, (int) i, mouse.activeButton
            ));
        }
    }


    private boolean onScreenClick(Screen screen, double mouseX, double mouseY, int button) {
        return !ModAPI.getAPI().getEventBus().fireEvent(new ScreenMouseEvent.MouseClicked(mouseX, mouseY, button, false));
    }

    private boolean onScreenMouseRelease(Screen screen, double mouseX, double mouseY, int button) {
        ModAPI.getAPI().getEventBus().fireEvent(new ScreenMouseEvent.MouseReleased(mouseX, mouseY, button));
        return true;
    }

    private boolean onScreenScroll(Screen screen, double mouseX, double mouseY, double scrollX, double scrollY) {
        return !ModAPI.getAPI().getEventBus().fireEvent(new ScreenMouseEvent.MouseScrolled(mouseX, mouseY, scrollX, scrollY, false));
    }

    private void onKeyRelease(Screen screen, int i, int i1, int i2) {
        ModAPI.getAPI().getEventBus().fireEvent(new ScreenKeyboardEvent.KeyPressed(i, i1, i2));
    }

    private void onKeyPress(Screen screen, int keyCode, int scanCode, int modifier) {
        ModAPI.getAPI().getEventBus().fireEvent(new ScreenKeyboardEvent.KeyPressed(keyCode, scanCode, modifier));
    }

    private void onWorldChange(MinecraftClient minecraftClient, ClientWorld clientWorld) {
        TabList.INSTANCE.clear();
        TeamManager.INSTANCE.clear();
        ScoreboardManager.INSTANCE.clear();
        ModAPI.getAPI().getEventBus().fireEvent(new WorldUnloadEvent());
    }

    private ActionResult onAttackEntity(PlayerEntity playerEntity, World world, Hand hand, Entity entity, @Nullable EntityHitResult entityHitResult) {
        if (!world.isClient) return ActionResult.PASS;
        PlayerInteractEntityEvent interactEvent = new PlayerInteractEntityEvent(
                true, false, UEntityDelegateFactory.createEntityFor(entity)
        );
        ModAPI.getAPI().getEventBus().fireEvent(interactEvent);
        if (interactEvent.isCanceled()) return ActionResult.FAIL;
        return ActionResult.PASS;
    }

    private ActionResult onAttackBlock(PlayerEntity playerEntity, World world, Hand hand, BlockPos blockPos, Direction direction) {
        if (!world.isClient) return ActionResult.PASS;
        PlayerInteractEvent interactEvent = new PlayerInteractEvent(
                (UEntityPlayer) UEntityDelegateFactory.createEntityFor(playerEntity),
                PlayerInteractEvent.Action.LEFT_CLICK_BLOCK,
                new UWorldImpl(world, (BlockStateRegistryImpl) ModAPI.getAPI().getBlockRegistry()),
                blockPos == null ? null : new VectorI3D(blockPos.getX(), blockPos.getY(), blockPos.getZ()),
                direction == null ? null : EnumFacing.VALUES[direction.getIndex()],
                null
        );
        ModAPI.getAPI().getEventBus().fireEvent(interactEvent);
        if (interactEvent.isCanceled()) return ActionResult.FAIL;
        return ActionResult.PASS;
    }

    private ActionResult onUseItem(PlayerEntity playerEntity, World world, Hand hand) {
        if (!world.isClient) return ActionResult.PASS;
        PlayerInteractEvent interactEvent = new PlayerInteractEvent(
                (UEntityPlayer) UEntityDelegateFactory.createEntityFor(playerEntity),
                PlayerInteractEvent.Action.RIGHT_CLICK_AIR,
                new UWorldImpl(world, (BlockStateRegistryImpl) ModAPI.getAPI().getBlockRegistry()),
                null,

                null,
                null
        );
        ModAPI.getAPI().getEventBus().fireEvent(interactEvent);
        if (interactEvent.isCanceled()) return ActionResult.FAIL;
        return ActionResult.PASS;
    }

    private ActionResult onUseEntity(PlayerEntity playerEntity, World world, Hand hand, Entity entity, @Nullable EntityHitResult entityHitResult) {
        if (!world.isClient) return ActionResult.PASS;
        PlayerInteractEntityEvent interactEvent = new PlayerInteractEntityEvent(
                false, true, UEntityDelegateFactory.createEntityFor(entity)
        );
        ModAPI.getAPI().getEventBus().fireEvent(interactEvent);
        if (interactEvent.isCanceled()) return ActionResult.FAIL;
        return ActionResult.PASS;
    }

    private ActionResult onUseBlock(PlayerEntity playerEntity, World world, Hand hand, BlockHitResult blockHitResult) {
        if (!world.isClient) return ActionResult.PASS;
        PlayerInteractEvent interactEvent = new PlayerInteractEvent(
                (UEntityPlayer) UEntityDelegateFactory.createEntityFor(playerEntity),
                PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK,
                new UWorldImpl(world, (BlockStateRegistryImpl) ModAPI.getAPI().getBlockRegistry()),
                blockHitResult.getBlockPos() == null ? null : new VectorI3D(blockHitResult.getBlockPos().getX(), blockHitResult.getBlockPos().getY(), blockHitResult.getBlockPos().getZ()),
                blockHitResult.getSide() == null ? null : EnumFacing.VALUES[blockHitResult.getSide().getIndex()],
                blockHitResult.getPos() == null ? null : new Vector3D(blockHitResult.getPos().x, blockHitResult.getPos().y, blockHitResult.getPos().z)
        );
        ModAPI.getAPI().getEventBus().fireEvent(interactEvent);
        if (interactEvent.isCanceled()) return ActionResult.FAIL;
        return ActionResult.PASS;
    }

    private void onEntityKilled(LivingEntity livingEntity, DamageSource damageSource) {
        ModAPI.getAPI().getEventBus().fireEvent(new LivingEntityDeathEvent((UEntityLiving) UEntityDelegateFactory.createEntityFor(livingEntity)));
    }

    private void onEntityUnload(Entity entity, ClientWorld clientWorld) {
        ModAPI.getAPI().getEventBus().fireEvent(new EntityExitWorldEvent(new int[] {
                entity.getId()
        }));
    }

    private void onEntityLoad(Entity entity, ClientWorld clientWorld) {
        ModAPI.getAPI().getEventBus().fireEvent(new EntityEnterWorldEvent(UEntityDelegateFactory.createEntityFor(entity)));
    }



    private void onTick(MinecraftClient minecraftClient) {
        ModAPI.getAPI().getEventBus().fireEvent(new ClientTickEvent());

        if (MinecraftClient.getInstance().world != null) {
            for (Entity entity : MinecraftClient.getInstance().world.getEntities()) {
                if (entity instanceof LivingEntity)
                    ModAPI.getAPI().getEventBus().fireEvent(new LivingEntityTickEvent(
                        (UEntityLiving) UEntityDelegateFactory.createEntityFor(entity)));
            }
        }
    }


    public void onGameMessage(Text message, boolean actionBar, Operation<Void> original) {
        Component comp = TextUtils.fromText(message);
        if (actionBar) {
            ActionBarReceivedEvent event = new ActionBarReceivedEvent(
                    comp,
                    comp
            );
            ModAPI.getAPI().getEventBus().fireEvent(event);
            if (event.original != event.chat) {
                if (event.chat != null) original.call(event.chat, actionBar);
            } else {
                original.call(message, actionBar);
            }
            return;
        }

        ChatReceivedEvent event = new ChatReceivedEvent(comp,comp,false, false);
        ModAPI.getAPI().getEventBus().fireEvent(event);

        if (event.isCanceled()) {
            var inGameHud = MinecraftClient.getInstance().inGameHud;

            var chatHudLine = new ChatHudLine(inGameHud.getTicks(), message, null, MessageIndicator.system());
            inGameHud.getChatHud().logChatMessage(chatHudLine);

            ClientReceiveMessageEvents.ALLOW_GAME.invoker().allowReceiveGameMessage(message, actionBar);
            ClientReceiveMessageEvents.GAME_CANCELED.invoker().onReceiveGameMessageCanceled(message, actionBar);
            return;
        }
        if (event.chat == event.original)
            original.call(message, actionBar);
        else
            original.call(TextUtils.fromComponent(event.chat), actionBar);
    }


//    private ThreadLocal<Stack<PlayerNameFormatEvent>> nameFormatEvents = ThreadLocal.withInitial(Stack::new);
//    public void onNameFormat(PlayerEvent.NameFormat nameFormat, EventPriority priority) {
//        if (priority == EventPriority.HIGHEST) nameFormatEvents.get().push(new PlayerNameFormatEvent(
//                (UEntityPlayer) UEntityDelegateFactory.createEntityFor(nameFormat.entityPlayer),
//                nameFormat.displayname,
//                nameFormat.username
//        ));
//        try {
//            nameFormatEvents.get().peek().setDisplayName(nameFormat.displayname);
//            ModAPI.getAPI().getEventBus().fireEvent(nameFormatEvents.get().peek(), mapPriority(priority));
//
//            nameFormat.displayname = nameFormatEvents.get().peek().displayName;
//
//            if (priority == EventPriority.LOWEST) {
//                PlayerNameFormatEvent result = nameFormatEvents.get().peek();
//                nameFormat.entityPlayer.getPrefixes().removeIf(iChatComponent -> iChatComponent instanceof MarkedChatComponent);
//                for (Component prefix : result.getPrefix()) {
//                    nameFormat.entityPlayer.getPrefixes().add(
//                            new MarkedChatComponent("")
//                                    .appendSibling(IChatComponent.Serializer.jsonToComponent(GsonComponentSerializer.colorDownsamplingGson().serialize(prefix)))
//                    );
//                }
//            }
//        } finally {
//            if (priority == EventPriority.LOWEST) {
//                nameFormatEvents.get().pop();
//            }
//        }
//    }
//
//    public void onItemToolip(net.minecraftforge.event.entity.player.ItemTooltipEvent itemTooltipEvent, EventPriority eventPriority) {
//        ItemTooltipEvent itemTooltipEvent1 = new ItemTooltipEvent(
//                itemTooltipEvent.showAdvancedItemTooltips,
//                new UItemStackImpl(itemTooltipEvent.itemStack),
//                itemTooltipEvent.toolTip
//        );
//        ModAPI.getAPI().getEventBus().fireEvent(itemTooltipEvent1, mapPriority(eventPriority));
//    }
}
