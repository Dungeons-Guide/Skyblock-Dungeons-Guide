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


import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.*;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
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
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.NodeProcessorDungeonRoom;
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
import java.util.*;

public class GeneralRoomProcessor implements RoomProcessor {

    @Getter
    @Setter
    private DungeonRoom dungeonRoom;
    public GeneralRoomProcessor(DungeonRoom dungeonRoom) {
        this.dungeonRoom = dungeonRoom;
    }
    private boolean ticked = false;


    @Override
    public void tick() {
        if (!ticked && FeatureRegistry.SECRET_AUTO_START.isEnabled()) {
            searchForNextTarget();
        }
        if (!ticked && FeatureRegistry.SECRET_SMART_AUTO_START.isEnabled()) {
            ActionDAGBuilder actionDAGBuilder = new ActionDAGBuilder(dungeonRoom);
            for (Map.Entry<String, DungeonMechanic> value : getDungeonRoom().getDungeonRoomInfo().getMechanics().entrySet()) {
                if (value.getValue() instanceof ISecret && !((ISecret) value.getValue()).isFound(getDungeonRoom())) {
                    try {
                        actionDAGBuilder.requires(new ActionChangeState(value.getKey(), "found"));
                    } catch (PathfindImpossibleException e) {
                        ChatTransmitter.addToQueue("Dungeons Guide :: Pathfind to "+value.getKey()+":found failed due to "+e.getMessage());
                        e.printStackTrace();
                        continue;
                    }
                } else if (value.getValue() instanceof DungeonRedstoneKey && ((DungeonRedstoneKey) value.getValue()).getCurrentState(dungeonRoom).equalsIgnoreCase("unobtained")) {
                    try {
                        actionDAGBuilder.requires(new ActionChangeState(value.getKey(), "obtained-self"));
                    } catch (PathfindImpossibleException e) {
                        ChatTransmitter.addToQueue("Dungeons Guide :: Pathfind to "+value.getKey()+":found failed due to "+e.getMessage());
                        e.printStackTrace();
                        continue;
                    }
                }
            }

            try {
                ActionDAG dag = actionDAGBuilder.build();
//                if (dag.getActionDAGNode().getPotentialRequires().size() != 0) {
                    ActionRoute actionRoute = new ActionRoute("Smart Route", dungeonRoom, dag,
                            FeatureRegistry.SECRET_LINE_PROPERTIES_SMART_ROUTE.getRouteProperties());
                    path.put(UUID.randomUUID().toString(), actionRoute);
//                }
            } catch (PathfindImpossibleException e) {
                ChatTransmitter.addToQueue("Dungeons Guide :: Pathfind to everything failed due to "+e.getMessage());
                e.printStackTrace();
            }
        }
        if (!ticked && FeatureRegistry.SECRET_PATHFIND_ALL.isEnabled()) {
            for (Map.Entry<String, DungeonMechanic> value : getDungeonRoom().getDungeonRoomInfo().getMechanics().entrySet()) {
                if (value.getValue() instanceof ISecret && !((ISecret) value.getValue()).isFound(getDungeonRoom())) {
                    ISecret secret = (ISecret) value.getValue();
                    try {
                        if (FeatureRegistry.SECRET_PATHFIND_ALL.isBat() && secret instanceof DungeonSecretBat)
                            pathfind(value.getKey(), "found", FeatureRegistry.SECRET_LINE_PROPERTIES_PATHFINDALL_BAT.getRouteProperties());
                        if (FeatureRegistry.SECRET_PATHFIND_ALL.isChest() && secret instanceof DungeonSecretChest)
                            pathfind(value.getKey(), "found", FeatureRegistry.SECRET_LINE_PROPERTIES_PATHFINDALL_CHEST.getRouteProperties());
                        if (FeatureRegistry.SECRET_PATHFIND_ALL.isEssence() && secret instanceof DungeonSecretEssence)
                            pathfind(value.getKey(), "found", FeatureRegistry.SECRET_LINE_PROPERTIES_PATHFINDALL_ESSENCE.getRouteProperties());
                        if (FeatureRegistry.SECRET_PATHFIND_ALL.isItemdrop() && secret instanceof DungeonSecretItemDrop)
                            pathfind(value.getKey(), "found", FeatureRegistry.SECRET_LINE_PROPERTIES_PATHFINDALL_ITEM_DROP.getRouteProperties());
                    } catch (Exception e) {
                        ChatTransmitter.addToQueue("Dungeons Guide :: Pathfind to "+value.getKey()+":found failed due to "+e.getMessage());
                    }
                }
            }
        }
        if (!ticked && FeatureRegistry.SECRET_BLOOD_RUSH.isEnabled()) {
            for (Map.Entry<String, DungeonMechanic> value : getDungeonRoom().getMechanics().entrySet()) {
                if (value.getValue() instanceof DungeonRoomDoor) {
                    DungeonRoomDoor dungeonDoor = (DungeonRoomDoor) value.getValue();
                    if (dungeonDoor.getDoorfinder().getType().isHeadToBlood()) {
                        try {
                            pathfind(value.getKey(), "navigate", FeatureRegistry.SECRET_BLOOD_RUSH_LINE_PROPERTIES.getRouteProperties());
                        } catch (Exception e) {
                            ChatTransmitter.addToQueue("Dungeons Guide :: Pathfind to "+value.getKey()+":found failed due to "+e.getMessage());
                        }
                    }
                }
            }
        }
        ticked = true;

        Set<String> toRemove = new HashSet<>();
        path.entrySet().forEach(a -> {
            a.getValue().onTick();
            if (a.getValue().getCurrentAction() instanceof ActionComplete)
                toRemove.add(a.getKey());
        });
        toRemove.forEach(path::remove);


        for (DungeonMechanic value : dungeonRoom.getMechanics().values()) {
            if (value instanceof ISecret) ((ISecret) value).tick(dungeonRoom);
        }

        if (toRemove.contains("AUTO-BROWSE") && FeatureRegistry.SECRET_AUTO_BROWSE_NEXT.isEnabled()) {
            searchForNextTarget();
        }
    }
    private final Set<String> visited = new HashSet<String>();

    public void searchForNextTarget() {
        if (getDungeonRoom().getCurrentState() == DungeonRoom.RoomState.FINISHED) {
            cancelAll();
            return;
        }

        BlockPos pos = Minecraft.getMinecraft().thePlayer.getPosition();

        double lowestCost = 99999999999999.0;
        Map.Entry<String, DungeonMechanic> lowestWeightMechanic = null;
        for (Map.Entry<String, DungeonMechanic> mech: dungeonRoom.getMechanics().entrySet()) {
            if (!(mech.getValue() instanceof ISecret)) continue;
            if (visited.contains(mech.getKey())) continue;
            if (!((ISecret) mech.getValue()).isFound(getDungeonRoom())) {
                double cost = 0;
                if (mech.getValue() instanceof DungeonSecretBat &&
                        ((ISecret)mech.getValue()).getPreRequisite().size() == 0) {
                    cost += -100000000;
                }
                if (mech.getValue().getRepresentingPoint(getDungeonRoom()) == null) continue;
                BlockPos blockpos = mech.getValue().getRepresentingPoint(getDungeonRoom()).getBlockPos(getDungeonRoom());

                cost += blockpos.distanceSq(pos);
                cost += ((ISecret) mech.getValue()).getPreRequisite().size() * 100;

                if (cost < lowestCost) {
                    lowestCost = cost;
                    lowestWeightMechanic = mech;
                }
            }
        }
        if (lowestWeightMechanic != null) {
            visited.add(lowestWeightMechanic.getKey());
            try {
                pathfind("AUTO-BROWSE", lowestWeightMechanic.getKey(), "found", FeatureRegistry.SECRET_LINE_PROPERTIES_AUTOPATHFIND.getRouteProperties());
            } catch (Exception e) {
                ChatTransmitter.addToQueue("Dungeons Guide :: Pathfind to "+lowestWeightMechanic.getKey()+":found failed due to "+e.getMessage());
                e.printStackTrace();
            }
        } else {
            visited.clear();
        }
    }

    @Override
    public void drawScreen(float partialTicks) {
        path.values().forEach(a -> {
            a.onRenderScreen(partialTicks);
        });

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
            for (Map.Entry<String, DungeonMechanic> value : dungeonRoom.getMechanics().entrySet()) {
                if (value.getValue() == null) continue;
                value.getValue().highlight(new Color(0,255,255,50), value.getKey(), dungeonRoom, partialTicks);
            }
        }


        ActionRoute finalSmallest = getBestFit(partialTicks);
        path.values().forEach(a -> {
            a.onRenderWorld(partialTicks, finalSmallest == a);
        });
    }

    private ActionRoute getBestFit(float partialTicks) {

        ActionRoute smallest = null;
        double smallestTan = 0.002;
        for (ActionRoute value : path.values()) {
            BlockPos target;
            if (value.getCurrentAction() instanceof ActionMove) {
                target = ((ActionMove) value.getCurrentAction()).getTarget().getBlockPos(dungeonRoom);
            } else if (value.getCurrentAction() instanceof ActionMoveNearestAir) {
                target = ((ActionMoveNearestAir) value.getCurrentAction()).getTarget().getBlockPos(dungeonRoom);
            } else if (value.getCurrent() >= 1 && value.getActions().get(value.getCurrent()-1) instanceof ActionMove) {
                target = ((ActionMove)value.getActions().get(value.getCurrent()-1)).getTarget().getBlockPos(dungeonRoom);
            } else if (value.getCurrent() >= 1 && value.getActions().get(value.getCurrent()-1) instanceof ActionMoveNearestAir) {
                target = ((ActionMoveNearestAir)value.getActions().get(value.getCurrent()-1)).getTarget().getBlockPos(dungeonRoom);
            } else continue;

            if (value.getActionRouteProperties().getLineRefreshRate() != -1 && value.getActionRouteProperties().isPathfind() && !FeatureRegistry.SECRET_FREEZE_LINES.isEnabled()) continue;

            Entity e = Minecraft.getMinecraft().getRenderViewEntity();

            double vectorV = VectorUtils.distSquared(e.getLook(partialTicks), e.getPositionEyes(partialTicks), new Vec3(target).addVector(0.5,0.5,0.5));

            if (vectorV < smallestTan) {
                smallest = value;
                smallestTan = vectorV;
            }
        }
        return smallest;
    }

    @Override
    public void chatReceived(IChatComponent chat) {
        if (lastChest != null && chat.getFormattedText().equals("§r§cThis chest has already been searched!§r")) {
            getDungeonRoom().getRoomContext().put("c-"+lastChest.toString(), 2);
            lastChest = null;
        }
        if (chat.getFormattedText().equals("§r§aYou found a Secret Redstone Key!§r")) {
            getDungeonRoom().getRoomContext().put("redstonekey", true);
        }
        if (chat.getFormattedText().equals("§e[NPC] Wizard§f: §rOh my lovely crystal ball, mi so happy§r")) {
            getDungeonRoom().getRoomContext().put("wizardcrystal", true);
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
        BlockPos pos2 = dungeonRoom.getMin().add(5, 0, 5);

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

    @Getter
    private Map<String, ActionRoute> path = new HashMap<>();

    public ActionRoute getPath(String id){
        return path.get(id);
    }

    public String pathfind(String mechanic, String state, ActionRouteProperties actionRouteProperties)throws PathfindImpossibleException  {
        String str = UUID.randomUUID().toString();
        pathfind(str, mechanic, state, actionRouteProperties);
        return str;
    }
    public void pathfind(String id, String mechanic, String state, ActionRouteProperties actionRouteProperties)throws PathfindImpossibleException {
        path.put(id, new ActionRoute(getDungeonRoom(), mechanic, state, actionRouteProperties));
    }
    public void cancelAll() {
        path.clear();
    }
    public void cancel(String id) {
        path.remove(id);
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

    @Override
    public void onKeybindPress(KeyBindPressedEvent keyInputEvent) {
        if (FeatureRegistry.SECRET_NEXT_KEY.isEnabled() && FeatureRegistry.SECRET_NEXT_KEY.<Integer>getParameter("key").getValue() == keyInputEvent.getKey()) {
            searchForNextTarget();
        } else if (FeatureRegistry.SECRET_CREATE_REFRESH_LINE.getKeybind() == keyInputEvent.getKey() && FeatureRegistry.SECRET_CREATE_REFRESH_LINE.isEnabled()) {
            ActionRoute actionRoute = getBestFit(0);
            // Because no route found!
            if (actionRoute == null) return;
            // actually do force refresh because of force freeze pathfind
            if (actionRoute.getCurrentAction() instanceof ActionMove) {
                ActionMove ac = (ActionMove) actionRoute.getCurrentAction();
                ac.forceRefresh(getDungeonRoom());
            } else if (actionRoute.getCurrentAction() instanceof ActionMoveNearestAir) {
                ActionMoveNearestAir ac = (ActionMoveNearestAir) actionRoute.getCurrentAction();
                ac.forceRefresh(getDungeonRoom());
            } else if (actionRoute.getCurrent() >= 1 && actionRoute.getActions().get(actionRoute.getCurrent()-1) instanceof ActionMove) {
                ((ActionMove)actionRoute.getActions().get(actionRoute.getCurrent()-1)).forceRefresh(dungeonRoom);
            } else if (actionRoute.getCurrent() >= 1 && actionRoute.getActions().get(actionRoute.getCurrent()-1) instanceof ActionMoveNearestAir) {
                ((ActionMoveNearestAir)actionRoute.getActions().get(actionRoute.getCurrent()-1)).forceRefresh(dungeonRoom);
            }

            if (FeatureRegistry.SECRET_CREATE_REFRESH_LINE.isPathfind() && !actionRoute.getActionRouteProperties().isPathfind()) {
                actionRoute.getActionRouteProperties().setPathfind(true);
                actionRoute.getActionRouteProperties().setLineRefreshRate(FeatureRegistry.SECRET_CREATE_REFRESH_LINE.getRefreshRate());
            }
        }
    }

    @Override
    public void onInteract(PlayerInteractEntityEvent event) {
        path.values().forEach(a -> {
            a.onLivingInteract(event);
        });
    }

    private boolean last = false;
    private BlockPos lastChest;
    @Override
    public void onInteractBlock(PlayerInteractEvent event) {
        path.values().forEach(a -> {
            a.onPlayerInteract(event);
        });

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
                    DungeonRoomDoor2 door2 = new DungeonRoomDoor2();
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
        path.values().forEach(a -> {
            a.onLivingDeath(deathEvent);
        });
        if (EditingContext.getEditingContext() != null && EditingContext.getEditingContext().getRoom() == getDungeonRoom()) {
            if (deathEvent.entity instanceof EntityBat) {
                for (GuiScreen screen : EditingContext.getEditingContext().getGuiStack()) {
                    if (screen instanceof GuiDungeonRoomEdit) {
                        DungeonSecretBat secret = new DungeonSecretBat();
                        secret.setSecretPoint(new OffsetPoint(dungeonRoom,
                                DungeonActionContext.getSpawnLocation().get(deathEvent.entity.getEntityId())
                        ));
                        ((GuiDungeonRoomEdit) screen).getSep().createNewMechanic("BAT-"+ UUID.randomUUID(),
                                secret);
                        return;
                    }
                }
                if (EditingContext.getEditingContext().getCurrent() instanceof GuiDungeonRoomEdit) {
                    DungeonSecretBat secret = new DungeonSecretBat();
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
    public void onBlockUpdate(BlockUpdateEvent blockUpdateEvent) {
        for (Tuple<BlockPos, IBlockState> updatedBlock : blockUpdateEvent.getUpdatedBlocks()) {
            if (updatedBlock.getSecond().equals(NodeProcessorDungeonRoom.preBuilt)) continue;
            dungeonRoom.resetBlock(updatedBlock.getFirst());
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
