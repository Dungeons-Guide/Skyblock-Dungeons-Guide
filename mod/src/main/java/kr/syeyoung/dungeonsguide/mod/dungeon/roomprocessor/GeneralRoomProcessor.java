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


import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.chat.ChatProcessor;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonActionContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.DungeonMechanicState;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.dunegonmechanic.ISecret;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.events.impl.DGChatReceivedEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.KeyBindPressedEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.pathfinding.abilitysetting.AlgorithmSetting;
import kr.syeyoung.dungeonsguide.mod.pathfinding.preset.RoomPreset;
import kr.syeyoung.dungeonsguide.mod.pathfinding.world.CoordinateMapBackedPathfindWorld;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.EnumFacing;
import kr.syeyoung.modapi.data.Pair;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.entity.EntityType;
import kr.syeyoung.modapi.entity.UEntity;
import kr.syeyoung.modapi.event.events.*;
import kr.syeyoung.modapi.rendering.UWorldRenderContext;
import kr.syeyoung.modapi.util.RaycastResult;
import kr.syeyoung.modapi.world.BlockType;
import kr.syeyoung.modapi.world.UBlockState;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.util.HashSet;

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
        HashSet<VectorI3D> poses = new HashSet<>();
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
    public void drawScreen(float partialTicks, RenderingContext context) {
        if (FeatureRegistry.ADVANCED_ROOMEDIT.isEnabled() && FeatureRegistry.DEBUG.isEnabled()) {

            RaycastResult result = ModAPI.getAPI().getObjectMouseOver();
            UEntity en = result.getEntityHit();
            if (en == null) return;

//            ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

            if (DungeonActionContext.getSpawnLocation().containsKey(en.getEntityId())) {
//                GlStateManager.enableBlend();
//                GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
//                GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
                int x = (int) (ModAPI.getAPI().getDisplayWidth() / ModAPI.getAPI().getScaleFactor());
                int y = (int) (ModAPI.getAPI().getDisplayHeight() / ModAPI.getAPI().getScaleFactor());

                context.drawString("Spawned at " + DungeonActionContext.getSpawnLocation().get(en.getEntityId()), x / 2, y / 2, 0xFFFFFFFF);
            }
        }
    }

    @Override
    public void drawWorld(UWorldRenderContext context, float partialTicks) {
//        if (FeatureRegistry.DEBUG.isEnabled() && (EditingContext.getEditingContext() != null && EditingContext.getEditingContext().getCurrent() instanceof GuiDungeonRoomEdit)) { $$ ROOMEDIT
//            for (Map.Entry<String, DungeonMechanicState> value : dungeonRoom.getMechanics().entrySet()) {
//                if (value.getValue() == null) continue;
//                value.getValue().highlight(new Color(0,255,255,50), value.getKey(), partialTicks);
//            }
//        }
    }

    @Override
    public void chatReceived(DGChatReceivedEvent chat) {
        String format = chat.getOriginalFormattedText();
        if (lastChest != null && format.equals("§cThis chest has already been searched!")) {
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
        if (TextUtils.compareString(format, "§aYou found a Secret Redstone Key!")) {
            for (DungeonMechanicState value : getDungeonRoom().getMechanics().values()) {
                if (value instanceof DungeonRedstoneKeyState) {
                    ((DungeonRedstoneKeyState) value).setDidClickOnRedstoneKey(true);
                }
            }
        }
        if (TextUtils.compareString(format, "§e[NPC] Wizard§f: Oh my lovely crystal ball, mi so happy")) {
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
    public void actionbarReceived(ActionBarReceivedEvent chat) {
        if (!SkyblockStatus.isOnDungeon()) return;
        String format = TextUtils.getNearestFormattedText(chat.chat);
        if (dungeonRoom.getTotalSecrets() == -1) {
            ChatTransmitter.sendDebugChat(format.replace('§', '&') + " - received");
        }
        if (!format.contains("/")) return;
        VectorI3D pos = ModAPI.getAPI().getPlayer().getPosition();

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        Point pt1 = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(pos.add(2, 0, 2));
        Point pt2 = context.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(pos.add(-2, 0, -2));
        if (!pt1.equals(pt2)) {
            stack = 0;
            secrets2 = -1;
            return;
        }
        VectorI3D pos2 = dungeonRoom.getRoomBounds().getMin().add(5, 0, 5);

        int secretsIndex = format.indexOf("Secrets");
        int secrets = 0;
        if (secretsIndex != -1) {
            int theIndex = 0;
            for (int i = secretsIndex; i >= 0; i--) {
                if (format.startsWith("§7", i)) {
                    theIndex = i;
                }
            }
            String it = format.substring(theIndex + 2, secretsIndex - 1);
     
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
                ChatProcessor.INSTANCE.addToChatQueue("/pchat $DG-Comm " + pos2.getX() + "/" + pos2.getZ() + " " + secrets, null, false);
        }
    }

    @Override
    public boolean readGlobalChat() {
        return false;
    }

    @Override
    public void onEntityUpdate(LivingEntityTickEvent updateEvent) {
        if (updateEvent.getEntityLiving().getEntityType() == EntityType.ARMOR_STAND &&
                updateEvent.getEntityLiving().getName() != null &&
                updateEvent.getEntityLiving().getName().contains("Mimic") &&
                !dungeonRoom.getContext().isGotMimic()) {
            dungeonRoom.getContext().setGotMimic(true);
//            Minecraft.getMinecraft().thePlayer.sendChatMessage("/pc $DG-Mimic");
        }
    }


    private boolean last = false;
    private VectorI3D lastChest;

    @Override
    public void onInteractBlock(PlayerInteractEvent event) {
        VectorI3D ePos = event.pos == null ? null : new VectorI3D(event.pos.getX(), event.pos.getY(), event.pos.getZ());
        EnumFacing eFacing = event.face;

        if (ePos != null) {
            UBlockState iBlockState = event.world.getBlockStateAt(event.pos);
            if (iBlockState.isOf(BlockType.CHEST, BlockType.TRAP_CHEST))
                lastChest = ePos;
        }

//        if (event.player.getHeldItem() != null && $$ ROOMEDIT
//            event.player.getHeldItem().getItem() == Item.STICK &&
//                FeatureRegistry.ADVANCED_ROOMEDIT.isEnabled() &&
//                FeatureRegistry.DEBUG.isEnabled()) {
//            EditingContext ec = EditingContext.getEditingContext();
//            if (ec != null && ec.getCurrent() instanceof GuiDungeonAddSet) {
//                GuiDungeonAddSet gdas = (GuiDungeonAddSet) ec.getCurrent();
//                if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
//                    if (last)
//                        gdas.getEnd().setPosInWorld(getDungeonRoom(), ePos);
//                    else
//                        gdas.getStart().setPosInWorld(getDungeonRoom(), ePos);
//
//                    last = !last;
//                }
//            }
//            if (ec != null && ec.getCurrent() instanceof GuiDungeonValueEdit) {
//                GuiDungeonValueEdit gdas = (GuiDungeonValueEdit) ec.getCurrent();
//                if (gdas.getValueEdit() instanceof ValueEditOffsetPoint) {
//                    ValueEditOffsetPoint offsetPoint = (ValueEditOffsetPoint) gdas.getValueEdit();
//
//                    if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
//                        OffsetPoint offsetPoint1 = (OffsetPoint) offsetPoint.getParameter().getNewData();
//                        offsetPoint1.setPosInWorld(getDungeonRoom(), ePos);
//                    }
//                }
//            }
//            try {
//                if (ec != null && ePos != null && event.world.getBlockStateAt(event.pos).isOf(BlockType.SPONGE)) {
//                    int nextId = 1;
//                    while (ec.getRoom().getDungeonRoomInfo().getMechanics().containsKey("ent-" + nextId)) nextId++;
//                    DungeonRoomDoor2State.DungeonRoomDoor2Data door2 = new DungeonRoomDoor2State.DungeonRoomDoor2Data();
//                    ec.getRoom().getDungeonRoomInfo().getMechanics().put("ent-" + nextId, door2);
//                    EnumFacing enumFacing = event.face;
//                    enumFacing = enumFacing.rotateY();
//                    for (int x = -1; x <= 1; x++) {
//                        for (int y = 0; y < 4; y++) {
//                            VectorI3D pos = ePos.add(enumFacing.getFrontOffsetX() * x, y, enumFacing.getFrontOffsetZ() * x);
//                            door2.getBlocks().getOffsetPointList().add(new OffsetPoint(dungeonRoom, pos));
//                        }
//                    }
//                    door2.getPfPoint().setPosInWorld(dungeonRoom, ePos
//                            .add(eFacing.getDirectionVec())
//                            .add(eFacing.getDirectionVec()));
//                    if (ec.getCurrent() instanceof GuiDungeonRoomEdit) {
//                        ((GuiDungeonRoomEdit) ec.getCurrent()).getSep().buildElements();
//                    }
//
//
//                }
//            } catch (Exception  e) {e.printStackTrace();}
//        }
    }

    @Override
    public void onEntityDeath(LivingEntityDeathEvent deathEvent) {
//        if (EditingContext.getEditingContext() != null && EditingContext.getEditingContext().getRoom() == getDungeonRoom()) { $$ ROOMEDIT
//            if (deathEvent.getEntityLiving().getEntityType() == EntityType.BAT) {
//                for (GuiScreen screen : EditingContext.getEditingContext().getGuiStack()) {
//                    if (screen instanceof GuiDungeonRoomEdit) {
//                        DungeonSecretBatState.DungeonSecretBatData secret = new DungeonSecretBatState.DungeonSecretBatData();
//                        secret.setSecretPoint(new OffsetPoint(dungeonRoom,
//                                DungeonActionContext.getSpawnLocation().get(deathEvent.getEntityLiving().getEntityId())
//                        ));
//                        ((GuiDungeonRoomEdit) screen).getSep().createNewMechanic("BAT-"+ UUID.randomUUID(), secret);
//                        return;
//                    }
//                }
//                if (EditingContext.getEditingContext().getCurrent() instanceof GuiDungeonRoomEdit) {
//                    DungeonSecretBatState.DungeonSecretBatData secret = new DungeonSecretBatState.DungeonSecretBatData();
//                    secret.setSecretPoint(new OffsetPoint(dungeonRoom,
//                            DungeonActionContext.getSpawnLocation().get(deathEvent.getEntityLiving().getEntityId())
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

//    public static final IBlockState STONE = Blocks.stone.getStateFromMeta(2);
    @Override
    public void onBlockUpdate(BlockUpdateEvent blockUpdateEvent) {
        for (Pair<VectorI3D, UBlockState> updatedBlock : blockUpdateEvent.getUpdatedBlocks()) {
//            if (updatedBlock.getSecond().equals(STONE)) continue;
            pathfinderWorld.resetBlock(updatedBlock.getFirst());
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
