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

import kr.syeyoung.dungeonsguide.mod.dungeon.data.LegacyBlock;
import kr.syeyoung.dungeonsguide.mod.events.impl.LivingTickEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.LivingDeathEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.ScreenRenderEvent;

import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicState;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonActionContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.ISecret;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSetting;
import kr.syeyoung.dungeonsguide.mod.pathfinding.preset.RoomPreset;
import kr.syeyoung.dungeonsguide.mod.pathfinding.world.CoordinateMapBackedPathfindWorld;
// import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.EditingContext;
// import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.gui.GuiDungeonAddSet;
// import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.gui.GuiDungeonRoomEdit;
// import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.gui.GuiDungeonValueEdit;
// import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.valueedit.ValueEditOffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.events.impl.BlockUpdateEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.KeyBindPressedEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.PlayerInteractEntityEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.Items;
import net.minecraft.util.*;
import kr.syeyoung.dungeonsguide.mod.events.impl.PlayerInteractEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.util.*;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import net.minecraft.network.chat.Component;
import kr.syeyoung.dungeonsguide.mod.chat.ChatProcessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

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

        // TODO: move pathfiner world to somewhere outside later.
        setupPathfinderWorld();
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
        // TODO: room editor is not in the first 26.1 build
//        if (FeatureRegistry.ADVANCED_ROOMEDIT.isEnabled() && FeatureRegistry.DEBUG.isEnabled()) {
//            FontRenderer fr = Minecraft.getInstance().font;

//            if (Minecraft.getInstance().hitResult == null) return;
//            Entity en = Minecraft.getInstance().hitResult.entityHit;
//            if (en == null) return;

//            ScaledResolution sr = new ScaledResolution(Minecraft.getInstance());
//            if (DungeonActionContext.getSpawnLocation().containsKey(en.getEntityId())) {
//                GlStateManager.enableBlend();
//                GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
//                GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
//                fr.drawString("Spawned at " + DungeonActionContext.getSpawnLocation().get(en.getEntityId()), sr.getScaledWidth() / 2, sr.getScaledHeight() / 2, 0xFFFFFFFF);
//            }
//        }
    }

    @Override
    public void drawWorld(float partialTicks) {
        // TODO: room editor is not in the first 26.1 build
//        if (FeatureRegistry.DEBUG.isEnabled() && (EditingContext.getEditingContext() != null && EditingContext.getEditingContext().getCurrent() instanceof GuiDungeonRoomEdit)) {
//            for (Map.Entry<String, DungeonMechanicState> value : dungeonRoom.getMechanics().entrySet()) {
//                if (value.getValue() == null) continue;
//                value.getValue().highlight(new Color(0,255,255,50), value.getKey(), partialTicks);
//            }
//        }
    }

    @Override
    public void chatReceived(Component chat) {
        if (lastChest != null && TextUtils.getFormattedText(chat).equals("§r§cThis chest has already been searched!§r")) {
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
        if (TextUtils.getFormattedText(chat).equals("§r§aYou found a Secret Redstone Key!§r")) {
            for (DungeonMechanicState value : getDungeonRoom().getMechanics().values()) {
                if (value instanceof DungeonRedstoneKeyState) {
                    ((DungeonRedstoneKeyState) value).setDidClickOnRedstoneKey(true);
                }
            }
        }
        if (TextUtils.getFormattedText(chat).equals("§e[NPC] Wizard§f: §rOh my lovely crystal ball, mi so happy§r")) {
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
    public void actionbarReceived(Component chat) {
        if (!SkyblockStatus.isOnDungeon()) return;
        if (dungeonRoom.getTotalSecrets() == -1) {
            ChatTransmitter.sendDebugChat(Component.literal(TextUtils.getFormattedText(chat).replace('§', '&') + " - received"));
        }
        if (!TextUtils.getFormattedText(chat).contains("/")) return;
        BlockPos pos = Minecraft.getInstance().player.blockPosition();

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        Point pt1 = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(pos.offset(2, 0, 2));
        Point pt2 = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(pos.offset(-2, 0, -2));
        if (!pt1.equals(pt2)) {
            stack = 0;
            secrets2 = -1;
            return;
        }
        BlockPos pos2 = dungeonRoom.getRoomBounds().getMin().add(5, 0, 5);

        String text = TextUtils.getFormattedText(chat);
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
                ChatProcessor.sendChatMessage("/pchat $DG-Comm " + pos2.getX() + "/" + pos2.getZ() + " " + secrets);
        }
    }

    @Override
    public boolean readGlobalChat() {
        return false;
    }


    @Override
    public void onPostGuiRender(ScreenRenderEvent.Post event) {

    }

    @Override
    public void onEntityUpdate(LivingTickEvent updateEvent) {
        if (updateEvent.entityLiving instanceof ArmorStand &&
                updateEvent.entityLiving.getName() != null &&
                // TODO: D5 entity name stopgap (D116)
                TextUtils.getFormattedText(updateEvent.entityLiving.getName()).contains("Mimic") &&
                !dungeonRoom.getContext().isGotMimic()) {
            dungeonRoom.getContext().setGotMimic(true);
//            ChatProcessor.sendChatMessage("/pc $DG-Mimic");
        }
    }


    private boolean last = false;
    private BlockPos lastChest;

    @Override
    public void onInteractBlock(PlayerInteractEvent event) {

        if (event.pos != null) {
            BlockState iBlockState = event.world.getBlockState(event.pos);
            if (LegacyBlock.CHEST.is(iBlockState) || LegacyBlock.TRAPPED_CHEST.is(iBlockState))
                lastChest = event.pos;
        }

        // TODO: room editor is not in the first 26.1 build
//        if (event.entityPlayer.getHeldItem() != null &&
//            event.entityPlayer.getHeldItem().getItem() == Items.stick &&
//                FeatureRegistry.ADVANCED_ROOMEDIT.isEnabled() &&
//                FeatureRegistry.DEBUG.isEnabled()) {
//            EditingContext ec = EditingContext.getEditingContext();
//            if (ec != null && ec.getCurrent() instanceof GuiDungeonAddSet) {
//                GuiDungeonAddSet gdas = (GuiDungeonAddSet) ec.getCurrent();
//                if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
//                    if (last)
//                        gdas.getEnd().setPosInWorld(getDungeonRoom(), event.pos);
//                    else
//                        gdas.getStart().setPosInWorld(getDungeonRoom(), event.pos);

//                    last = !last;
//                }
//            }
//            if (ec != null && ec.getCurrent() instanceof GuiDungeonValueEdit) {
//                GuiDungeonValueEdit gdas = (GuiDungeonValueEdit) ec.getCurrent();
//                if (gdas.getValueEdit() instanceof ValueEditOffsetPoint) {
//                    ValueEditOffsetPoint offsetPoint = (ValueEditOffsetPoint) gdas.getValueEdit();

//                    if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
//                        OffsetPoint offsetPoint1 = (OffsetPoint) offsetPoint.getParameter().getNewData();
//                        offsetPoint1.setPosInWorld(getDungeonRoom(), event.pos);
//                    }
//                }
//            }
//            try {
//                if (ec != null && event.pos != null && LegacyBlock.SPONGE.is(Minecraft.getInstance().level.getBlockState(event.pos))) {
//                    int nextId = 1;
//                    while (ec.getRoom().getDungeonRoomInfo().getMechanics().containsKey("ent-" + nextId)) nextId++;
//                    DungeonRoomDoor2State.DungeonRoomDoor2Data door2 = new DungeonRoomDoor2State.DungeonRoomDoor2Data();
//                    ec.getRoom().getDungeonRoomInfo().getMechanics().put("ent-" + nextId, door2);
//                    Direction enumFacing = event.face;
//                    enumFacing = enumFacing.rotateY();
//                    for (int x = -1; x <= 1; x++) {
//                        for (int y = 0; y < 4; y++) {
//                            BlockPos pos = event.pos.add(enumFacing.getFrontOffsetX() * x, y, enumFacing.getFrontOffsetZ() * x);
//                            door2.getBlocks().getOffsetPointList().add(new OffsetPoint(dungeonRoom, pos));
//                        }
//                    }
//                    door2.getPfPoint().setPosInWorld(dungeonRoom, event.pos.add(event.face.getDirectionVec()).add(event.face.getDirectionVec()));
//                    if (ec.getCurrent() instanceof GuiDungeonRoomEdit) {
//                        ((GuiDungeonRoomEdit) ec.getCurrent()).getSep().buildElements();
//                    }


//                }
//            } catch (Exception  e) {e.printStackTrace();}
//        }
    }

    @Override
    public void onEntityDeath(LivingDeathEvent deathEvent) {
        // TODO: room editor is not in the first 26.1 build
//        if (EditingContext.getEditingContext() != null && EditingContext.getEditingContext().getRoom() == getDungeonRoom()) {
//            if (deathEvent.entity instanceof Bat) {
//                for (GuiScreen screen : EditingContext.getEditingContext().getGuiStack()) {
//                    if (screen instanceof GuiDungeonRoomEdit) {
//                        DungeonSecretBatState.DungeonSecretBatData secret = new DungeonSecretBatState.DungeonSecretBatData();
//                        secret.setSecretPoint(new OffsetPoint(dungeonRoom,
//                                DungeonActionContext.getSpawnLocation().get(deathEvent.entity.getEntityId())
//                        ));
//                        ((GuiDungeonRoomEdit) screen).getSep().createNewMechanic("BAT-"+ UUID.randomUUID(), secret);
//                        return;
//                    }
//                }
//                if (EditingContext.getEditingContext().getCurrent() instanceof GuiDungeonRoomEdit) {
//                    DungeonSecretBatState.DungeonSecretBatData secret = new DungeonSecretBatState.DungeonSecretBatData();
//                    secret.setSecretPoint(new OffsetPoint(dungeonRoom,
//                            DungeonActionContext.getSpawnLocation().get(deathEvent.entity.getEntityId())
//                    ));
//                    ((GuiDungeonRoomEdit) EditingContext.getEditingContext().getCurrent()).getSep().createNewMechanic("BAT-"+ UUID.randomUUID(),
//                            secret);
//                }
//            }
//        }
    }

    @Override
    public void onKeybindPress(KeyBindPressedEvent keyInputEvent) {

    }

    @Override
    public void onInteract(PlayerInteractEntityEvent event) {

    }

    public static final BlockState STONE = LegacyBlock.STONE.state(2);
    @Override
    public void onBlockUpdate(BlockUpdateEvent blockUpdateEvent) {
        for (Tuple<BlockPos, BlockState> updatedBlock : blockUpdateEvent.getUpdatedBlocks()) {
            if (updatedBlock.getB().equals(STONE)) continue;
            pathfinderWorld.resetBlock(updatedBlock.getA());
        }
    }

    @Override
    public void chunkUpdate(int chunkX, int chunkZ) {
        pathfinderWorld.resetChunk(chunkX, chunkZ);
    }



    @Getter
    private AlgorithmSetting algorithmSetting;
    @Getter
    private RoomPreset roomPreset;


    public static class Generator implements RoomProcessorGenerator<GeneralRoomProcessor> {
        @Override
        public GeneralRoomProcessor createNew(DungeonRoom dungeonRoom) {
            GeneralRoomProcessor defaultRoomProcessor = new GeneralRoomProcessor(dungeonRoom);
            return defaultRoomProcessor;
        }
    }
}
