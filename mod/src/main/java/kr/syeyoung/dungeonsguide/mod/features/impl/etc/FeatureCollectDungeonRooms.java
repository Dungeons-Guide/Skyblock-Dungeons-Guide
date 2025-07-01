/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2024  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.features.impl.etc;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import kr.syeyoung.dungeonsguide.launcher.LetsEncrypt;
import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.launcher.auth.AuthManager;
import kr.syeyoung.dungeonsguide.launcher.gui.screen.GuiDisplayer;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.VersionInfo;
import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.config.types.TCBoolean;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.mod.dungeon.map.DungeonRoomScaffoldParser;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.DGChatReceivedEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.DungeonRoomDiscoveredEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.gui.GuiScreenAdapter;
import kr.syeyoung.dungeonsguide.mod.gui.elements.Scaler;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.party.PartyContext;
import kr.syeyoung.dungeonsguide.mod.party.PartyManager;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.*;
import kr.syeyoung.modapi.entity.EntityType;
import kr.syeyoung.modapi.entity.UEntity;
import kr.syeyoung.modapi.entity.UEntityArmorStand;
import kr.syeyoung.modapi.entity.UEntityPlayer;
import kr.syeyoung.modapi.event.events.*;
import kr.syeyoung.modapi.item.UItemStack;
import kr.syeyoung.modapi.world.BlockType;
import kr.syeyoung.modapi.world.UBlockState;
import kr.syeyoung.modapi.world.UChunk;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.text.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class FeatureCollectDungeonRooms extends SimpleFeature {
    public FeatureCollectDungeonRooms() {
        super("Misc", "Collect Dungeon Data", "Enable to allow sending anything inside dungeon to developers server\n\nThis option is to rebuild dungeon room data to implement better features\n\nDisable to opt out of it","etc.collectdungeon", false);
        addParameter("prompted", new FeatureParameter<Boolean>("prompted", "Was this prompted?", "Did this feature prompt for user apporval yet?", false, TCBoolean.INSTANCE));
    }


    public class WidgetUserApproval extends AnnotatedImportOnlyWidget {
        public WidgetUserApproval() {
            super(new ResourceIdentifier("dungeonsguide:gui/collect_rooms_approval.gui"));
        }

        @On(functionName = "approve")
        public void onApprove() {
            FeatureCollectDungeonRooms.this.<Boolean>getParameter("prompted").setValue(true);
            FeatureCollectDungeonRooms.this.setEnabled(true);
            Minecraft.getMinecraft().displayGuiScreen(null);
            ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);
        }

        @On(functionName = "deny")
        public void onDeny() {
            FeatureCollectDungeonRooms.this.<Boolean>getParameter("prompted").setValue(true);
            FeatureCollectDungeonRooms.this.setEnabled(false);
            Minecraft.getMinecraft().displayGuiScreen(null);
            ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);
        }
    }

    @Override
    public void init() {
        if (!this.<Boolean>getParameter("prompted").getValue()) {
            Scaler scaler = new Scaler();
            scaler.scale.setValue((double) new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor());
            scaler.child.setValue(new FeatureCollectDungeonRooms.WidgetUserApproval());
            GuiDisplayer.INSTANCE.displayGui(new GuiScreenAdapter(scaler, null, false));
        }
    }

    private Map<Integer, EntityData> entityDataMap = new HashMap<>();
    private Map<ChunkCoordIntPair, ChunkData> initialChunkDataMap = new HashMap<>();
    private Map<DungeonRoom, RoomInfo> roomInfoMap = new HashMap<>();

    @Data @Getter
    public static class EntityData {
        private int id;
        public String name;
        private String playerSkin;
        private String armorstand;
        private transient UItemStack[] armoritems = new UItemStack[5];
        private Map<String, Double> attributes = new HashMap<>();

        private String type;

        @AllArgsConstructor @Data
        public static class EntityTrajectory {
            private enum Type {
                ENTER, MOVE, EXIT, DEATH
            }
            private Type type;
            private Vector3D pos;
            private long time;
        }
        private LinkedList<EntityTrajectory> trajectory = new LinkedList<>();
    }

    @Data
    public static class ChunkData {
        private int x, z;
        private UChunk initialBlockStorages;
    }

    public static class RoomInfo {
        @AllArgsConstructor @Data
        public static class BlockUpdate {
            @AllArgsConstructor @Data
            public static class BlockUpdateData {
                private VectorI3D pos;
                private UBlockState block;
            }

            private List<BlockUpdateData> updatedBlocks = new ArrayList<>();
            private long time;
        }
        private List<BlockUpdate> blockUpdates = new ArrayList<>();

        private List<Interaction> interactions = new ArrayList<>();
        @Data @AllArgsConstructor
        public static class Interaction {
            private long time;
            private VectorI3D pos;
        }
        private List<ChatMessage> systemMessages = new ArrayList<>();
        @Data @AllArgsConstructor
        public static class ChatMessage {
            private long time;
            private Component chat;
        }


        private Map<Integer, EntityData> entityData = new HashMap<>();
        private LinkedList<EntityData.EntityTrajectory> playerTrajactory = new LinkedList<>();
        private int minX, minZ;
    }

    @DGEventHandler(triggerOutOfSkyblock = true, ignoreDisabled = true)
    public void onEntitySpawn(EntityEnterWorldEvent event) {
        if (event.getEntity() instanceof UEntityArmorStand) {
            return;
        }
        if (event.getEntity().getEntityType() == EntityType.ARROW) {
            return;
        }
        if (entityDataMap.get(event.getEntity().getEntityId()) != null) return;
        EntityData entityData = new EntityData();
        entityData.id = event.getEntity().getEntityId();
        entityData.trajectory.add(new EntityData.EntityTrajectory(
                EntityData.EntityTrajectory.Type.ENTER,
                event.getEntity().getPositionVector(),
                System.currentTimeMillis()
        ));
        entityData.type = event.getEntity().getClass().getSimpleName();
        entityDataMap.put(event.getEntity().getEntityId(), entityData);

        Vector3D posVector = event.getEntity().getPositionVector();
        Point roompt = Optional.ofNullable(DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext())
                .map(DungeonContext::getScaffoldParser)
                .map(DungeonRoomScaffoldParser::getDungeonMapLayout)
                .map(a -> a.worldPointToRoomPoint(
                        new Vector3D(posVector.x, posVector.y, posVector.z)
                )).orElse(null);
        if (roompt == null) return;;
        DungeonContext dungeonContext = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        DungeonRoom dungeonRoom = dungeonContext.getScaffoldParser().getRoomMap().get(roompt);
        if (dungeonRoom != null && roomInfoMap.containsKey(dungeonRoom)) {
            roomInfoMap.get(dungeonRoom).entityData.put(event.getEntity().getEntityId(), entityData);
        }
    }

    @DGEventHandler(triggerOutOfSkyblock = true, ignoreDisabled = true)
    public void entityDeadWelp(EntityExitWorldEvent event) {
        for (int entityId : event.getEntityIds()) {
            EntityData entityData = entityDataMap.get(entityId);
            if (entityData != null) {
                entityData.trajectory.add(new EntityData.EntityTrajectory(EntityData.EntityTrajectory.Type.EXIT, null, System.currentTimeMillis()));
            }
        }
    }


    @DGEventHandler(ignoreDisabled = true)
    public void playerInteract(PlayerInteractEvent event) {
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;
        UBlockState blockState = ModAPI.getAPI().getWorld().getBlockStateAt(event.pos);
        if (blockState == null) return;
        if (!(blockState.isOf(BlockType.LEVER, BlockType.CHEST, BlockType.TRAP_CHEST, BlockType.SKULL))) {
            return;
        }
        DungeonContext dungeonContext = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (dungeonContext == null|| dungeonContext.getScaffoldParser() == null) return;
        Point roompt = dungeonContext.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(event.pos);
        DungeonRoom dungeonRoom = dungeonContext.getScaffoldParser().getRoomMap().get(roompt);
        if (dungeonRoom == null) return;
        RoomInfo roomInfo = roomInfoMap.get(dungeonRoom);
        if (roomInfo == null) return;

        roomInfo.interactions.add(new RoomInfo.Interaction(System.currentTimeMillis(), event.pos));
    }

    @DGEventHandler(ignoreDisabled = true)
    public void onChat(DGChatReceivedEvent event) {
        if (!event.getOriginalFormattedText().contains(":")) {
            // this is not user message.
            if (Minecraft.getMinecraft().thePlayer == null) return;
            Vector3D pos = ModAPI.getAPI().getPlayer().getPositionVector();

            DungeonContext dungeonContext = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
            if (dungeonContext == null|| dungeonContext.getScaffoldParser() == null) return;
            Point roompt = dungeonContext.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(pos);
            DungeonRoom dungeonRoom = dungeonContext.getScaffoldParser().getRoomMap().get(roompt);
            if (dungeonRoom == null) return;
            RoomInfo roomInfo = roomInfoMap.get(dungeonRoom);
            if (roomInfo == null) return;

            roomInfo.systemMessages.add(new RoomInfo.ChatMessage(System.currentTimeMillis(), event.getOriginalComponent()));
        }
    }
    private int lastNo = 0;
    private int totalSecret = 0;
    @DGEventHandler(ignoreDisabled = true)
    public void onTick(ClientTickEvent tickEvent) {
        int secret = FeatureRegistry.DUNGEON_SECRETS_ROOM.getLatestCurrSecrets();
        int total = FeatureRegistry.DUNGEON_SECRETS_ROOM.getLatestTotalSecrets();
        if (secret != lastNo || total != totalSecret) {
            lastNo = secret;
            totalSecret = total;
            Vector3D pos = ModAPI.getAPI().getPlayer().getPositionVector();

            DungeonContext dungeonContext = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
            if (dungeonContext == null|| dungeonContext.getScaffoldParser() == null) return;
            Point roompt = dungeonContext.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(pos);
            DungeonRoom dungeonRoom = dungeonContext.getScaffoldParser().getRoomMap().get(roompt);
            if (dungeonRoom == null) return;
            RoomInfo roomInfo = roomInfoMap.get(dungeonRoom);
            if (roomInfo == null) return;

            roomInfo.systemMessages.add(new RoomInfo.ChatMessage(System.currentTimeMillis(), Component.text("SECRET UPDATE: "+secret+"/"+total)));
        }

        DungeonContext dungeonContext = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (dungeonContext == null || dungeonContext.getScaffoldParser() == null) return;
        Point roompt = dungeonContext.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(ModAPI.getAPI().getPlayer().getPositionVector());
        DungeonRoom dungeonRoom = dungeonContext.getScaffoldParser().getRoomMap().get(roompt);
        if (dungeonRoom == null) return;
        RoomInfo roomInfo = roomInfoMap.get(dungeonRoom);
        if (roomInfo == null) return;
        if (roomInfo.playerTrajactory.size() == 0 || roomInfo.playerTrajactory.getLast().getPos() == null || roomInfo.playerTrajactory.getLast().getPos().distanceSq(ModAPI.getAPI().getPlayer().getPositionVector()) > 0.1f) {
            roomInfo.playerTrajactory.add(new EntityData.EntityTrajectory(EntityData.EntityTrajectory.Type.MOVE, ModAPI.getAPI().getPlayer().getPositionVector(), System.currentTimeMillis()));
        }
    }

    @DGEventHandler(triggerOutOfSkyblock = true, ignoreDisabled = true)
    public void onEntityAttributeUpdate(LivingEntityTickEvent event) {
        if (event.getEntityLiving() instanceof UEntityArmorStand) {
            return;
        }
        if (event.getEntityLiving().getEntityType() == EntityType.ARROW) {
            return;
        }
        EntityData entityData = entityDataMap.get(event.getEntityLiving().getEntityId());
        if (entityData == null) {
//            System.out.println("WTFF??? it's not on map?? "+event.entity);
            return;
        }
        entityData.id = event.getEntityLiving().getEntityId();

        if (event.getEntityLiving() instanceof UEntityPlayer && entityData.playerSkin == null) {
            entityData.playerSkin = ((UEntityPlayer) event.getEntityLiving()).getSkinTexture();
        }

        List<UEntity> entityList = ModAPI.getAPI().getWorld().getEntitiesWithinAabb(EntityType.ARMOR_STAND, new AABB(-0.2,-0.2,-0.2,0.2,0.2,0.2)
                .addCoord(event.getEntityLiving().getPosX(), event.getEntityLiving().getPosY()+event.getEntityLiving().getHealth(), event.getEntityLiving().getPosZ()));
        UEntityArmorStand theEntity = (UEntityArmorStand) entityList.stream().min(Comparator.comparingDouble(a -> Math.abs(a.getPosX() - event.getEntityLiving().getPosX()) + Math.abs(a.getPosZ() - event.getEntityLiving().getPosZ()))).orElse(null);

        if (theEntity != null && entityData.armorstand == null)
            entityData.armorstand = theEntity.getName();

        entityData.name = event.getEntityLiving().getName();

        // TODO later...
        if (event.getEntityLiving().getHeldItem() != null)
            entityData.armoritems[4] = event.getEntityLiving().getHeldItem();
        if (event.getEntityLiving().getCurrentArmor(0) != null)
            entityData.armoritems[0] = event.getEntityLiving().getCurrentArmor(0);
        if (event.getEntityLiving().getCurrentArmor(1) != null)
            entityData.armoritems[1] = event.getEntityLiving().getCurrentArmor(1);
        if (event.getEntityLiving().getCurrentArmor(2) != null)
            entityData.armoritems[2] = event.getEntityLiving().getCurrentArmor(2);
        if (event.getEntityLiving().getCurrentArmor(3) != null)
            entityData.armoritems[3] = event.getEntityLiving().getCurrentArmor(3);
        if (entityData.trajectory.getLast() == null || entityData.trajectory.getLast().getPos() == null || entityData.trajectory.getLast().getPos().distanceSq(event.getEntityLiving().getPositionVector()) > 0.1f) {
            entityData.trajectory.add(new EntityData.EntityTrajectory(EntityData.EntityTrajectory.Type.MOVE, event.getEntityLiving().getPositionVector(), System.currentTimeMillis()));
        }
    }

    @DGEventHandler
    public void onEntityDespawn(LivingEntityDeathEvent event) {
//        System.out.println("Entity died!!:" +event.entity);
        EntityData entityData = entityDataMap.get(event.getEntityLiving().getEntityId());
        if (entityData != null) {
            entityData.trajectory.add(new EntityData.EntityTrajectory(EntityData.EntityTrajectory.Type.DEATH, event.getEntityLiving().getPositionVector(), System.currentTimeMillis()));
        }
    }

    @DGEventHandler(triggerOutOfSkyblock = true, ignoreDisabled = true)
    public void onChunkLoad(ChunkUpdateEvent.Pre chunkUpdateEvent) {
        Set<Pair<VectorI3D, UBlockState>> updates = new HashSet<>();
        for (UChunk updatedChunk : chunkUpdateEvent.getUpdatedChunks()) {
            if (updatedChunk.isEmpty()) continue;
            Pair<Integer, Integer> coordinate = new Pair<>(updatedChunk.getChunkZ(), updatedChunk.getChunkX());
            if (initialChunkDataMap.containsKey(coordinate)) {
                // that's block update!

                ChunkData prevChunk = initialChunkDataMap.get(coordinate);
                UChunk prev = prevChunk.getInitialBlockStorages();
                UChunk neu = updatedChunk;
                for (int y = 0; y < neu.getLenY(); y++) {
                    for (int x = 0; x < neu.getLenX(); x++) {
                        for (int z = 0; z < neu.getLenZ(); z++) {
                            UBlockState prevB = prev.getRelativeBlockAt(x,y,z);
                            UBlockState neuB = neu.getRelativeBlockAt(x,y,z);
                            if (neu != prev) {
                                updates.add(new Pair<>(new VectorI3D(x+neu.getMinX(),y+neu.getMinY(),z+neu.getMinZ()), neuB));
                            }
                        }
                    }
                }
            }
            ChunkData chunkData = new ChunkData();
            chunkData.x = updatedChunk.getChunkX();
            chunkData.z = updatedChunk.getChunkZ();
            chunkData.initialBlockStorages = updatedChunk;
            initialChunkDataMap.put(new ChunkCoordIntPair(chunkData.x, chunkData.z), chunkData);
        }
        if (!updates.isEmpty()) {
            BlockUpdateEvent.Pre pre = new BlockUpdateEvent.Pre();
            pre.setUpdatedBlocks(updates);
            onBlockUpdate(pre);
        }
    }

    @DGEventHandler(triggerOutOfSkyblock = true, ignoreDisabled = true)
    public void onBlockUpdate(BlockUpdateEvent.Pre blockUpdateEvent) {
        DungeonContext dungeonContext = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (dungeonContext == null|| dungeonContext.getScaffoldParser() == null) return;
        Map<DungeonRoom, List<Pair<VectorI3D, UBlockState>>> updatePerRoom = blockUpdateEvent.getUpdatedBlocks().stream()
                .filter(a -> {
                    Point roompt = dungeonContext.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(a.getFirst());
                    return dungeonContext.getScaffoldParser().getRoomMap().get(roompt) != null;
                })
                .collect(Collectors.groupingBy(a -> {
                            Point roompt = dungeonContext.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(a.getFirst());
                            return dungeonContext.getScaffoldParser().getRoomMap().get(roompt);
                        }));

        for (Map.Entry<DungeonRoom, List<Pair<VectorI3D, UBlockState>>> dungeonRoomListEntry : updatePerRoom.entrySet()) {
            if (dungeonRoomListEntry.getKey() == null) {
                System.out.println("WTF!!!");
            }
            RoomInfo roomInfo = roomInfoMap.get(dungeonRoomListEntry.getKey());
            if (roomInfo  == null) continue;
            roomInfo.blockUpdates.add(new RoomInfo.BlockUpdate(dungeonRoomListEntry.getValue().stream().map(it -> new RoomInfo.BlockUpdate.BlockUpdateData(it.getFirst(), it.getSecond())).collect(Collectors.toList()), System.currentTimeMillis()));
            roomInfo.minX = dungeonRoomListEntry.getKey().getRoomBounds().getMin().getX();
            roomInfo.minZ = dungeonRoomListEntry.getKey().getRoomBounds().getMin().getZ();
        }
    }
    @DGEventHandler(triggerOutOfSkyblock = true, ignoreDisabled = true)
    public void onDungeonRoomDiscover(DungeonRoomDiscoveredEvent discoveredEvent) {
        DungeonRoom dungeonRoom = discoveredEvent.getDungeonRoom();
        DungeonContext dungeonContext = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (dungeonContext != dungeonRoom.getContext()) return;

        RoomInfo roomInfo = new RoomInfo();
        for (EntityData value : entityDataMap.values()) {
            Vector3D vec3 = value.trajectory.getFirst().pos;
            if (dungeonRoom.getUnitPoints().contains(dungeonContext.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(new Vector3D(vec3.x, vec3.y, vec3.z)))) {
                roomInfo.entityData.put(value.id, value);
            }
        }

        roomInfoMap.put(dungeonRoom, roomInfo);
    }

    @DGEventHandler(triggerOutOfSkyblock = true, ignoreDisabled = true)
    public void onWorldLoad(WorldUnloadEvent event) {
        try {
            Gson gson = new GsonBuilder()
                    .disableHtmlEscaping()
                    .registerTypeAdapter(UItemStack.class, new TypeAdapter<UItemStack>() {
                        @Override
                        public void write(JsonWriter out, UItemStack value) throws IOException {
                            if (value == null) {
                                out.nullValue();
                            } else {
                                out.value(nbttostring("item", value.serialize()));
                            }
                        }

                        @Override
                        public UItemStack read(JsonReader in) throws IOException {
                            return null;
                        }
                    })
                    .registerTypeAdapter(UBlockState.class, new TypeAdapter<UBlockState>() {
                        @Override
                        public void write(JsonWriter out, UBlockState value) throws IOException {
                            out.value(value.serialize());
                        }

                        @Override
                        public UBlockState read(JsonReader in) throws IOException {
                            return null;
                        }
                    })
                    .registerTypeAdapter(Vec3.class, new TypeAdapter<Vec3>() {
                        @Override
                        public void write(JsonWriter out, Vec3 value) throws IOException {
                            if (value == null) {
                                out.nullValue();
                                return;
                            }
                            out.beginArray().value(value.xCoord).value(value.yCoord).value(value.zCoord).endArray();
                        }

                        @Override
                        public Vec3 read(JsonReader in) throws IOException {
                            return null;
                        }
                    }).registerTypeAdapter(RoomInfo.BlockUpdate.BlockUpdateData.class, new TypeAdapter<RoomInfo.BlockUpdate.BlockUpdateData>() {
                        @Override
                        public void write(JsonWriter out, RoomInfo.BlockUpdate.BlockUpdateData value) throws IOException {
//                            int id = Block.getIdFromBlock(value.getBlock().getBlock());
//                            int meta = value.getBlock().getBlock().getMetaFromState(value.getBlock());
                            out.beginArray().value(value.getPos().getX()).value(value.getPos().getY()).value(value.getPos().getZ()).value(value.getBlock().serialize()).endArray();
                        }

                        @Override
                        public RoomInfo.BlockUpdate.BlockUpdateData read(JsonReader in) throws IOException {
                            return null;
                        }
                    })
                    .registerTypeAdapter(IChatComponent.class, new IChatComponent.Serializer())
                    .create();
            String correlationId = Optional.ofNullable(PartyManager.INSTANCE.getPartyContext())
                    .map(PartyContext::getPartyID)
                    .orElse(UUID.randomUUID().toString());
            for (Map.Entry<DungeonRoom, RoomInfo> dungeonRoomRoomInfoEntry : roomInfoMap.entrySet()) {
                JsonObject jsonObject = new JsonObject();
                DungeonRoomInfo dri = dungeonRoomRoomInfoEntry.getKey().getDungeonRoomInfo();
                if (dri != null) {
                    jsonObject.addProperty("uuid", dri.getUuid().toString());
                    jsonObject.addProperty("name", dri.getName());
                }
                jsonObject.addProperty("rot", dungeonRoomRoomInfoEntry.getKey().getRoomMatcher().getRotation());
                RoomInfo roomInfo = dungeonRoomRoomInfoEntry.getValue();
                jsonObject.addProperty("dungeon", dungeonRoomRoomInfoEntry.getKey().getContext().getDungeonName());
                jsonObject.addProperty("correlation", correlationId);
                jsonObject.addProperty("minX", roomInfo.minX);
                jsonObject.addProperty("minZ", roomInfo.minZ);
                jsonObject.addProperty("shape", dungeonRoomRoomInfoEntry.getKey().getRoomBounds().getShape());
                jsonObject.addProperty("color", dungeonRoomRoomInfoEntry.getKey().getColor());
                jsonObject.addProperty("secrets", dungeonRoomRoomInfoEntry.getKey().getTotalSecrets());
                jsonObject.add("entities", gson.toJsonTree(roomInfo.entityData));
                jsonObject.add("blockupdates", gson.toJsonTree(roomInfo.blockUpdates));
                jsonObject.add("chats", gson.toJsonTree(roomInfo.systemMessages));
                jsonObject.add("interactions", gson.toJsonTree(roomInfo.interactions));
                jsonObject.add("player", gson.toJsonTree(roomInfo.playerTrajactory));
                jsonObject.addProperty("version", "2");



                CompoundBinaryTag nbtTagCompound2 = createNBT(roomInfo, dungeonRoomRoomInfoEntry.getKey());
                jsonObject.addProperty("schematic", nbttostring("Schematic", nbtTagCompound2));

                try {
                    String str = gson.toJson(jsonObject);
                    queueSendLogAsync(str);

                    // if debug
                    if (FeatureRegistry.DEBUG.isEnabled()) {
                        new File(Main.getConfigDir(), "runs").mkdirs();
                        FileOutputStream fos = new FileOutputStream(new File(Main.getConfigDir(), "runs/" + UUID.randomUUID() + ".dgroom"));
                        JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(fos));
                        gson.toJson(jsonObject, jsonWriter);
                        jsonWriter.flush();
                        jsonWriter.close();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } finally {
            entityDataMap.clear();
            roomInfoMap.clear();
            initialChunkDataMap.clear();
        }
    }


    public static final Executor executorService = Executors
            .newSingleThreadExecutor(new ThreadFactoryBuilder()
                    .setThreadFactory(DungeonsGuide.THREAD_FACTORY)
                    .setNameFormat("DG-Error-Reporter-%d").build());

    public void queueSendLogAsync(String t) {
        executorService.execute(() -> {
            try {
                sendLogActually(t);
            } catch (Exception ignored) {ignored.printStackTrace();} // not ignored at all lol
        });
    }

    private void sendLogActually(String t) throws IOException {
        if (!isEnabled()) return;
        String token = AuthManager.getInstance().getWorkingTokenOrThrow(); // this require privacy policy.

        HttpsURLConnection urlConnection = (HttpsURLConnection) new URL(Main.DOMAIN+"/logging/dgrun").openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setDoOutput(true);
        urlConnection.setDoInput(true);
        urlConnection.setSSLSocketFactory(LetsEncrypt.LETS_ENCRYPT);
        urlConnection.setRequestProperty("User-Agent", "DungeonsGuide/"+ VersionInfo.VERSION);
        urlConnection.setConnectTimeout(10000);
        urlConnection.setReadTimeout(10000);
        urlConnection.setRequestProperty("Authorization", "Bearer "+token);
        urlConnection.getOutputStream().write(t.getBytes(StandardCharsets.UTF_8));
        int code = urlConnection.getResponseCode(); // make sure to send req actually
    }


    public static String nbttostring(String name, CompoundBinaryTag compound) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            BinaryTagIO.writer().writeNamed(new AbstractMap.SimpleEntry<>(name, compound), byteArrayOutputStream, BinaryTagIO.Compression.GZIP);
            byte[] arr = byteArrayOutputStream.toByteArray();
            return Base64.getEncoder().encodeToString(arr);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private CompoundBinaryTag createNBT(RoomInfo roomInfo, DungeonRoom dungeonRoom) {
        if (1==1) throw new UnsupportedOperationException("NOOO"); // TODO:

        CompoundBinaryTag.Builder compound = CompoundBinaryTag.builder();
        short width =  (short) (dungeonRoom.getRoomBounds().getMax().getX() - dungeonRoom.getRoomBounds().getMin().getX() + 1);
        compound.putShort("Width", width);
        short height  = 255;
        compound.putShort("Height", height);
        short length = (short) (dungeonRoom.getRoomBounds().getMax().getZ() - dungeonRoom.getRoomBounds().getMin().getZ() + 1);
        compound.putShort("Length", length);
        int size = width * height * length;

        byte[] blocks = new byte[size];
        byte[] meta = new byte[size];
        byte[] extra = new byte[size];
        byte[] extraNibble = new byte[(int) Math.ceil(size / 2.0)];

        boolean extraEx = false;
        ListBinaryTag.Builder<CompoundBinaryTag> tileEntitiesList = ListBinaryTag.builder(BinaryTagTypes.COMPOUND);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    int index = x + (y * length + z) * width;
                    VectorI3D pos = dungeonRoom.getRelativeBlockPosAt(x,y - 70,z);
                    ChunkData chunkData = initialChunkDataMap.get(new ChunkCoordIntPair(pos.getX() >> 4, pos.getZ() >> 4));

                    UBlockState blockState = chunkData.initialBlockStorages.getRelativeBlockAt(x&0xF, y, z&0xF);

                    blocks[index] = (byte) blockState.getLegacyId();
                    meta[index] =  (byte) blockState.getLegacyMeta();
                    if ((extra[index] = (byte) ((blockState.getLegacyId()) >> 8)) > 0) {
                        extraEx = true;
                    }
                }
            }
        }
        for (int i = 0; i < extraNibble.length; i++) {
            if (i * 2 + 1 < extra.length) {
                extraNibble[i] = (byte) ((extra[i * 2 + 0] << 4) | extra[i * 2 + 1]);
            } else {
                extraNibble[i] = (byte) (extra[i * 2 + 0] << 4);
            }
        }


        compound.putByteArray("Blocks", blocks);
        compound.putByteArray("Data", meta);
        compound.putString("Materials", "Alpha");
        if (extraEx) {
            compound.putByteArray("AddBlocks", extraNibble);
        }
        compound.put("Entities", ListBinaryTag.empty());
        compound.put("TileEntities", tileEntitiesList.build());

        return compound.build();
    }


    @DGEventHandler
    public void onRender(RenderWorldLastEvent event) {
        if (!FeatureRegistry.DEBUG.isEnabled()) return;
        if (DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() == null) return;

        Entity hovered = Minecraft.getMinecraft().pointedEntity;
        if (hovered == null) return;
        EntityData entityData = entityDataMap.get(hovered.getEntityId());
        if (entityData == null) {
            RenderUtils.drawTextAtWorld("??Unknown??", (float) hovered.posX, (float) hovered.posY+3, (float) hovered.posZ, 0xFF000000, 0.02f, false, true, event.partialTicks);
        } else {
            if (entityData.getArmorstand() != null)
                RenderUtils.drawTextAtWorld(entityData.getArmorstand(), (float) hovered.posX, (float) hovered.posY+3, (float) hovered.posZ, 0xFF000000, 0.02f, false, true, event.partialTicks);
            RenderUtils.drawTextAtWorld(entityData.getType(), (float) hovered.posX, (float) hovered.posY+3.2f, (float) hovered.posZ, 0xFF00FF00, 0.02f, false, true, event.partialTicks);
            Vector3D pos = entityData.getTrajectory().getFirst().getPos();
            RenderUtils.renderBeaconBeam(
                    pos.x,
                    pos.y,
                    pos.z,
                    new AColor(0, 255, 0, 255),
                    event.partialTicks
            );
            List<Vector3D> lines = new ArrayList<>();
            for (EntityData.EntityTrajectory entityTrajectory : entityData.getTrajectory()) {
                if (entityTrajectory.getPos() == null) {
                    RenderUtils.drawLinesVec3(lines, new AColor(0,255,0,255), 1.0f, event.partialTicks, false);
                    lines.clear();
                    continue;
                }
                lines.add(entityTrajectory.getPos());
            }
            RenderUtils.drawLinesVec3(lines, new AColor(0,255,0,255), 1.0f, event.partialTicks, false);
        }
    }
}
