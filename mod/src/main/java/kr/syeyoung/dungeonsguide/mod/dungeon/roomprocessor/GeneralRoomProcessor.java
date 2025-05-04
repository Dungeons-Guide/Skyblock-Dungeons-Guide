/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor;


import com.google.common.util.concurrent.ThreadFactoryBuilder;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGNode;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetVec3;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicState;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonActionContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.route.ActionRoute;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.route.ActionRouteProperties;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAG;
import kr.syeyoung.dungeonsguide.mod.dungeon.actions.tree.ActionDAGBuilder;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.ISecret;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.PathfindLineProperties;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.lineproperties.styles.ClassicPathDisplayEngine;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.lineproperties.styles.IPathDisplayEngine;
import kr.syeyoung.dungeonsguide.mod.pathfinding.BoundingBox;
import kr.syeyoung.dungeonsguide.mod.pathfinding.TSPCache;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSetting;
import kr.syeyoung.dungeonsguide.mod.pathfinding.pathfinder.FineGridStonkingBFS;
import kr.syeyoung.dungeonsguide.mod.pathfinding.pathfinder.IPathfinder;
import kr.syeyoung.dungeonsguide.mod.pathfinding.pathfinder.PathfinderExecutor;
import kr.syeyoung.dungeonsguide.mod.pathfinding.precalculation.PathfindPrecalculation;
import kr.syeyoung.dungeonsguide.mod.pathfinding.precalculation.PathfindPrecalculationRegistry;
import kr.syeyoung.dungeonsguide.mod.pathfinding.precalculation.RoomPreset;
import kr.syeyoung.dungeonsguide.mod.pathfinding.world.CoordinateMapBackedPathfindWorld;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.gui.GuiDungeonAddSet;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.gui.GuiDungeonRoomEdit;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.gui.GuiDungeonValueEdit;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.valueedit.ValueEditOffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.events.impl.BlockUpdateEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.KeyBindPressedEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.PlayerInteractEntityEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCollectDiagnostics;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.FeaturePathfindStrategy;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.precalclist.AdditionalInfoCaculatedDungeonRoomInfo;
import kr.syeyoung.dungeonsguide.mod.utils.VectorUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.*;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GeneralRoomProcessor implements RoomProcessor {

    @Getter
    private CoordinateMapBackedPathfindWorld pathfinderWorld;

    @Getter
    @Setter
    private DungeonRoom dungeonRoom;
    public GeneralRoomProcessor(DungeonRoom dungeonRoom) {
        this.dungeonRoom = dungeonRoom;

        roomPreset = dungeonRoom.getContext().getPreset().getRoomPreset(dungeonRoom.getDungeonRoomInfo().getUuid());
        algorithmSetting = roomPreset.getEffectiveAlgorithmSetting(dungeonRoom.getDungeonRoomInfo());

        setupPathfinderWorld();

        pathfindLoaderThread.submit(() -> {
            try {
                this.loadPrecalculations();
            } catch (Exception e) {
                if (e.getMessage() == null || !e.getMessage().contains("Chunk not loaded")) {
                    FeatureCollectDiagnostics.queueSendLogAsync(e);
                    e.printStackTrace();
                }
            }
        });
    }

    private void setupPathfinderWorld() {
        HashSet<BlockPos> poses = new HashSet<>();
        for (DungeonMechanicState value : dungeonRoom.getMechanics().values()) {
            if (value instanceof DungeonTombState) {
                for (OffsetPoint offsetPoint : ((DungeonTombState) value).blockedPoints()) {
                    poses.add(offsetPoint.getBlockPos(dungeonRoom));
                }
            } else if (value instanceof DungeonBreakableWallState) {
                for (OffsetPoint offsetPoint : ((DungeonBreakableWallState) value).blockedPoints()) {
                    poses.add(offsetPoint.getBlockPos(dungeonRoom));
                }
            }
        }
        pathfinderWorld = new CoordinateMapBackedPathfindWorld(dungeonRoom.getCoordinateMap(), algorithmSetting, dungeonRoom.getRoomBounds(), poses);
    }


    @Override
    public void tick() {
        for (DungeonMechanicState value : dungeonRoom.getMechanics().values()) {
            if (value instanceof ISecret) ((ISecret) value).tick(dungeonRoom);
        }
    }

    @Override
    public void drawScreen(float partialTicks) {
        if (FeatureRegistry.ADVANCED_ROOMEDIT.isEnabled() && FeatureRegistry.DEBUG.isEnabled()) {
            FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

            if (Minecraft.getMinecraft().objectMouseOver == null) return;
            Entity en = Minecraft.getMinecraft().objectMouseOver.entityHit;
            if (en == null) return;

            ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
            if (DungeonActionContext.getSpawnLocation().containsKey(en.getEntityId())) {
                GlStateManager.enableBlend();
                GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
                fr.drawString("Spawned at " + DungeonActionContext.getSpawnLocation().get(en.getEntityId()), sr.getScaledWidth() / 2, sr.getScaledHeight() / 2, 0xFFFFFFFF);
            }
        }
    }

    @Override
    public void drawWorld(float partialTicks) {
        if (FeatureRegistry.DEBUG.isEnabled() && (EditingContext.getEditingContext() != null && EditingContext.getEditingContext().getCurrent() instanceof GuiDungeonRoomEdit)) {
            for (Map.Entry<String, DungeonMechanicState> value : dungeonRoom.getMechanics().entrySet()) {
                if (value.getValue() == null) continue;
                value.getValue().highlight(new Color(0,255,255,50), value.getKey(), partialTicks);
            }
        }
    }

    @Override
    public void chatReceived(IChatComponent chat) {
        if (lastChest != null && chat.getFormattedText().equals("§r§cThis chest has already been searched!§r")) {
            for (DungeonMechanicState mechanic : getDungeonRoom().getMechanics().values()) {
                if (mechanic instanceof DungeonSecretChestState) {
                    DungeonSecretChestState chest = (DungeonSecretChestState) mechanic;
                    if (chest.getSecretPoint().getBlockPos(getDungeonRoom()).equals(lastChest)) {
                        chest.markFound();
                    }
                } else if (mechanic instanceof DungeonSecretDoubleChestState) {
                    DungeonSecretDoubleChestState chest = (DungeonSecretDoubleChestState) mechanic;
                    if (chest.getData().getSecretPoint().getBlockPos(dungeonRoom).equals(lastChest)) {
                        chest.markFound();
                    } else if (chest.getData().getSecretPoint2().getBlockPos(dungeonRoom).equals(lastChest)) {
                        chest.markFound();
                    }
                }
            }
            lastChest = null;
        }
        if (chat.getFormattedText().equals("§r§aYou found a Secret Redstone Key!§r")) {
            for (DungeonMechanicState value : getDungeonRoom().getMechanics().values()) {
                if (value instanceof DungeonRedstoneKeyState) {
                    ((DungeonRedstoneKeyState) value).setDidClickOnRedstoneKey(true);
                }
            }
        }
        if (chat.getFormattedText().equals("§e[NPC] Wizard§f: §rOh my lovely crystal ball, mi so happy§r")) {
            for (DungeonMechanicState value : getDungeonRoom().getMechanics().values()) {
                if (value instanceof DungeonWizardState) {
                    ((DungeonWizardState) value).setDidCompleteQuest(true);
                }
            }
        }
    }

    private int stack = 0;
    private long secrets2 = 0;
    @Override
    public void actionbarReceived(IChatComponent chat) {
        if (!SkyblockStatus.isOnDungeon()) return;
        if (dungeonRoom.getTotalSecrets() == -1) {
            ChatTransmitter.sendDebugChat(new ChatComponentText(chat.getFormattedText().replace('§', '&') + " - received"));
        }
        if (!chat.getFormattedText().contains("/")) return;
        BlockPos pos = Minecraft.getMinecraft().thePlayer.getPosition();

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        Point pt1 = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(pos.add(2, 0, 2));
        Point pt2 = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(pos.add(-2, 0, -2));
        if (!pt1.equals(pt2)) {
            stack = 0;
            secrets2 = -1;
            return;
        }
        BlockPos pos2 = dungeonRoom.getRoomBounds().getMin().add(5, 0, 5);

        String text = chat.getFormattedText();
        int secretsIndex = text.indexOf("Secrets");
        int secrets = 0;
        if (secretsIndex != -1) {
            int theIndex = 0;
            for (int i = secretsIndex; i >= 0; i--) {
                if (text.startsWith("§7", i)) {
                    theIndex = i;
                }
            }
            String it = text.substring(theIndex + 2, secretsIndex - 1);
     
            secrets = Integer.parseInt(it.split("/")[1]);
        }

        if (secrets2 == secrets) stack++;
        else {
            stack = 0;
            secrets2 = secrets;
        }

        if (stack == 4 && dungeonRoom.getTotalSecrets() != secrets) {
            dungeonRoom.setTotalSecrets(secrets);
            if (FeatureRegistry.DUNGEON_INTERMODCOMM.isEnabled())
                Minecraft.getMinecraft().thePlayer.sendChatMessage("/pchat $DG-Comm " + pos2.getX() + "/" + pos2.getZ() + " " + secrets);
        }
    }

    @Override
    public boolean readGlobalChat() {
        return false;
    }


    @Override
    public void onPostGuiRender(GuiScreenEvent.DrawScreenEvent.Post event) {

    }

    @Override
    public void onEntityUpdate(LivingEvent.LivingUpdateEvent updateEvent) {
        if (updateEvent.entityLiving instanceof EntityArmorStand &&
                updateEvent.entityLiving.getName() != null &&
                updateEvent.entityLiving.getName().contains("Mimic") &&
                !dungeonRoom.getContext().isGotMimic()) {
            dungeonRoom.getContext().setGotMimic(true);
//            Minecraft.getMinecraft().thePlayer.sendChatMessage("/pc $DG-Mimic");
        }
    }


    private boolean last = false;
    private BlockPos lastChest;

    @Override
    public void onInteractBlock(PlayerInteractEvent event) {

        if (event.pos != null) {
            IBlockState iBlockState = event.world.getBlockState(event.pos);
            if (iBlockState.getBlock() == Blocks.chest || iBlockState.getBlock() == Blocks.trapped_chest)
                lastChest = event.pos;
        }

        if (event.entityPlayer.getHeldItem() != null &&
            event.entityPlayer.getHeldItem().getItem() == Items.stick &&
                FeatureRegistry.ADVANCED_ROOMEDIT.isEnabled() &&
                FeatureRegistry.DEBUG.isEnabled()) {
            EditingContext ec = EditingContext.getEditingContext();
            if (ec != null && ec.getCurrent() instanceof GuiDungeonAddSet) {
                GuiDungeonAddSet gdas = (GuiDungeonAddSet) ec.getCurrent();
                if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
                    if (last)
                        gdas.getEnd().setPosInWorld(getDungeonRoom(), event.pos);
                    else
                        gdas.getStart().setPosInWorld(getDungeonRoom(), event.pos);

                    last = !last;
                }
            }
            if (ec != null && ec.getCurrent() instanceof GuiDungeonValueEdit) {
                GuiDungeonValueEdit gdas = (GuiDungeonValueEdit) ec.getCurrent();
                if (gdas.getValueEdit() instanceof ValueEditOffsetPoint) {
                    ValueEditOffsetPoint offsetPoint = (ValueEditOffsetPoint) gdas.getValueEdit();

                    if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
                        OffsetPoint offsetPoint1 = (OffsetPoint) offsetPoint.getParameter().getNewData();
                        offsetPoint1.setPosInWorld(getDungeonRoom(), event.pos);
                    }
                }
            }
            try {
                if (ec != null && event.pos != null && Minecraft.getMinecraft().theWorld.getBlockState(event.pos).getBlock() == Blocks.sponge) {
                    int nextId = 1;
                    while (ec.getRoom().getDungeonRoomInfo().getMechanics().containsKey("ent-" + nextId)) nextId++;
                    DungeonRoomDoor2State.DungeonRoomDoor2Data door2 = new DungeonRoomDoor2State.DungeonRoomDoor2Data();
                    ec.getRoom().getDungeonRoomInfo().getMechanics().put("ent-" + nextId, door2);
                    EnumFacing enumFacing = event.face;
                    enumFacing = enumFacing.rotateY();
                    for (int x = -1; x <= 1; x++) {
                        for (int y = 0; y < 4; y++) {
                            BlockPos pos = event.pos.add(enumFacing.getFrontOffsetX() * x, y, enumFacing.getFrontOffsetZ() * x);
                            door2.getBlocks().getOffsetPointList().add(new OffsetPoint(dungeonRoom, pos));
                        }
                    }
                    door2.getPfPoint().setPosInWorld(dungeonRoom, event.pos.add(event.face.getDirectionVec()).add(event.face.getDirectionVec()));
                    if (ec.getCurrent() instanceof GuiDungeonRoomEdit) {
                        ((GuiDungeonRoomEdit) ec.getCurrent()).getSep().buildElements();
                    }


                }
            } catch (Exception  e) {e.printStackTrace();}
        }
    }

    @Override
    public void onEntityDeath(LivingDeathEvent deathEvent) {
        if (EditingContext.getEditingContext() != null && EditingContext.getEditingContext().getRoom() == getDungeonRoom()) {
            if (deathEvent.entity instanceof EntityBat) {
                for (GuiScreen screen : EditingContext.getEditingContext().getGuiStack()) {
                    if (screen instanceof GuiDungeonRoomEdit) {
                        DungeonSecretBatState.DungeonSecretBatData secret = new DungeonSecretBatState.DungeonSecretBatData();
                        secret.setSecretPoint(new OffsetPoint(dungeonRoom,
                                DungeonActionContext.getSpawnLocation().get(deathEvent.entity.getEntityId())
                        ));
                        ((GuiDungeonRoomEdit) screen).getSep().createNewMechanic("BAT-"+ UUID.randomUUID(), secret);
                        return;
                    }
                }
                if (EditingContext.getEditingContext().getCurrent() instanceof GuiDungeonRoomEdit) {
                    DungeonSecretBatState.DungeonSecretBatData secret = new DungeonSecretBatState.DungeonSecretBatData();
                    secret.setSecretPoint(new OffsetPoint(dungeonRoom,
                            DungeonActionContext.getSpawnLocation().get(deathEvent.entity.getEntityId())
                    ));
                    ((GuiDungeonRoomEdit) EditingContext.getEditingContext().getCurrent()).getSep().createNewMechanic("BAT-"+ UUID.randomUUID(),
                            secret);
                }
            }
        }
    }

    @Override
    public void onKeybindPress(KeyBindPressedEvent keyInputEvent) {

    }

    @Override
    public void onInteract(PlayerInteractEntityEvent event) {

    }

    public static final IBlockState STONE = Blocks.stone.getStateFromMeta(2);
    @Override
    public void onBlockUpdate(BlockUpdateEvent blockUpdateEvent) {
        for (Tuple<BlockPos, IBlockState> updatedBlock : blockUpdateEvent.getUpdatedBlocks()) {
            if (updatedBlock.getSecond().equals(STONE)) continue;
            pathfinderWorld.resetBlock(updatedBlock.getFirst());
        }
    }

    @Override
    public void chunkUpdate(int chunkX, int chunkZ) {
        pathfinderWorld.resetChunk(chunkX, chunkZ);
    }

    private static final ExecutorService pathfindLoaderThread = DungeonsGuide.getDungeonsGuide().registerExecutorService(Executors.newFixedThreadPool(8,
            new ThreadFactoryBuilder()
                    .setThreadFactory(DungeonsGuide.THREAD_FACTORY)
                    .setNameFormat("DG-PathfindLoader-%d").build()));


    private final Map<Vec3, WeakReference<PathfinderExecutor>> activePathfind = new HashMap<>();


    public PathfinderExecutor createEntityPathTo(BoundingBox pos) {
        if (activePathfind.containsKey(pos.center())) {
            WeakReference<PathfinderExecutor> executorWeakReference = activePathfind.get(pos.center());
            PathfinderExecutor executor = executorWeakReference.get();
            if (executor != null) {
                return executor;
            }
        }
        if (true)
            return null;
        FeaturePathfindStrategy.PathfindStrategy pathfindStrategy = FeatureRegistry.SECRET_PATHFIND_STRATEGY.getPathfindStrat();
        PathfinderExecutor executor;
        if (pathfindStrategy == FeaturePathfindStrategy.PathfindStrategy.A_STAR_FINE_GRID_SMART) {
            executor = new PathfinderExecutor(new FineGridStonkingBFS(algorithmSetting), pos, pathfinderWorld);
        } else {
            return  null;
        }
        activePathfind.put(pos.center(), new WeakReference<>(executor));
        dungeonRoom.getContext().getExecutor().registerExecutor(executor);

        return executor;
    }

    @Getter
    private AlgorithmSetting algorithmSetting;
    @Getter
    private RoomPreset roomPreset;

    private void loadPrecalculations() {
        Set<String> pathfinders = roomPreset.getPrecalculations();
        if (pathfinders != null) {
            for (String precalcId : pathfinders) {
                loadPrecalculated(precalcId);
            }
        }
    }

    private final Map<String, WeakReference<PathfinderExecutor>> idExecutor = new HashMap<>();
    private final Map<String, PathfindPrecalculation> idCalculation = new HashMap<>();
    public void loadPrecalculated(String id) {
        PathfindPrecalculation cachedPathfinder = PathfindPrecalculationRegistry.getINSTANCE().getById(id);
        if (cachedPathfinder == null) return;
        if (idCalculation.containsKey(id)) return;
        idCalculation.put(cachedPathfinder.getTargetHash(), cachedPathfinder);
    }

    public PathfindPrecalculation loadPrecalculatedUnloadedByHash(String hash) {
        if (!idCalculation.containsKey(hash)) {
            if (nextShowedWarning < System.currentTimeMillis()) {
                ChatTransmitter.addToQueue("§eDungeons Guide §7:: §cPrecalculation "+hash+" in room "+dungeonRoom.getDungeonRoomInfo().getName()+" is §4§lMISSING §cin currently applied preset §e"+roomPreset.getParent().getPresetName()+"§c. There may be some problems in pathfinding. Please add precalculations at /dg -> Pathfinding & Secrets -> Precalculations");
                nextShowedWarning = System.currentTimeMillis() + 30000L;
            }
            return null;
        }

        return idCalculation.get(hash);
    }

    private long nextShowedWarning = 0;
    public synchronized PathfinderExecutor loadPrecalculatedByHash(String hash) {
        if (!idCalculation.containsKey(hash)) {
            if (nextShowedWarning < System.currentTimeMillis()) {
                ChatTransmitter.addToQueue("§eDungeons Guide §7:: §cPrecalculation "+hash+" in room "+dungeonRoom.getDungeonRoomInfo().getName()+" is §4§lMISSING §cin currently applied preset §e"+roomPreset.getParent().getPresetName()+"§c. There may be some problems in pathfinding. Please add precalculations at /dg -> Pathfinding & Secrets -> Precalculations");
                nextShowedWarning = System.currentTimeMillis() + 30000L;
            }
            return null;
        }

        if (idExecutor.containsKey(hash)) {
            WeakReference<PathfinderExecutor> executorSoftReference = idExecutor.get(hash);
            PathfinderExecutor executor = executorSoftReference.get();
            if (executor != null) return executor;
            idExecutor.remove(hash);
        };

        System.out.println("LOADING:: "+hash);
        PathfindPrecalculation precalculation = idCalculation.get(hash);

        try {
            IPathfinder pathfinder = precalculation.createPathfinder(dungeonRoom.getRoomMatcher().getRotation());
            PathfinderExecutor executor1 = new PathfinderExecutor(pathfinder, BoundingBox.of(AxisAlignedBB.fromBounds(0,0,0,0,0,0)), pathfinderWorld);
            idExecutor.put(precalculation.getTargetHash(), new WeakReference<>(executor1));
            executor1.doStep();
            return executor1;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static class Generator implements RoomProcessorGenerator<GeneralRoomProcessor> {
        @Override
        public GeneralRoomProcessor createNew(DungeonRoom dungeonRoom) {
            GeneralRoomProcessor defaultRoomProcessor = new GeneralRoomProcessor(dungeonRoom);
            return defaultRoomProcessor;
        }
    }
}
