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
import com.mojang.authlib.properties.Property;
import jdk.nashorn.internal.ir.debug.JSONWriter;
import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.launcher.LetsEncrypt;
import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.launcher.auth.AuthManager;
import kr.syeyoung.dungeonsguide.launcher.gui.screen.GuiDisplayer;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.VersionInfo;
import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.config.types.TCBoolean;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.map.DungeonMapLayout;
import kr.syeyoung.dungeonsguide.mod.dungeon.map.DungeonRoomScaffoldParser;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.*;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.guiv2.GuiScreenAdapter;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Scaler;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.party.PartyContext;
import kr.syeyoung.dungeonsguide.mod.party.PartyManager;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipOutputStream;

public class FeatureCollectDungeonRooms extends SimpleFeature {
    public FeatureCollectDungeonRooms() {
        super("Misc", "Collect Dungeon Data", "Enable to allow sending anything inside dungeon to developers server\n\nThis option is to rebuild dungeon room data to implement better features\n\nDisable to opt out of it","etc.collectdungeon", false);
        addParameter("prompted", new FeatureParameter<Boolean>("prompted", "Was this prompted?", "Did this feature prompt for user apporval yet?", false, TCBoolean.INSTANCE));
    }


    public class WidgetUserApproval extends AnnotatedImportOnlyWidget {
        public WidgetUserApproval() {
            super(new ResourceLocation("dungeonsguide:gui/collect_rooms_approval.gui"));
        }

        @On(functionName = "approve")
        public void onApprove() {
            FeatureCollectDungeonRooms.this.<Boolean>getParameter("prompted").setValue(true);
            FeatureCollectDungeonRooms.this.setEnabled(true);
            Minecraft.getMinecraft().displayGuiScreen(null);
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        }

        @On(functionName = "deny")
        public void onDeny() {
            FeatureCollectDungeonRooms.this.<Boolean>getParameter("prompted").setValue(true);
            FeatureCollectDungeonRooms.this.setEnabled(false);
            Minecraft.getMinecraft().displayGuiScreen(null);
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        }
    }

    @Override
    public void loadConfig(JsonObject jsonObject) {
        super.loadConfig(jsonObject);
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
        private IChatComponent armorstand;
        private transient ItemStack[] armoritems = new ItemStack[5];
        private Map<String, Double> attributes = new HashMap<>();

        private String type;

        @AllArgsConstructor @Data
        public static class EntityTrajectory {
            private enum Type {
                ENTER, MOVE, EXIT, DEATH
            }
            private Type type;
            private Vec3 pos;
            private long time;
        }
        private LinkedList<EntityTrajectory> trajectory = new LinkedList<>();
    }

    @Data
    public static class ChunkData {
        private int x, z;
        ExtendedBlockStorage[] initialBlockStorages;
    }

    public static class RoomInfo {
        @AllArgsConstructor @Data
        public static class BlockUpdate {
            @AllArgsConstructor @Data
            public static class BlockUpdateData {
                private BlockPos pos;
                private IBlockState block;
            }

            private List<BlockUpdateData> updatedBlocks = new ArrayList<>();
            private long time;
        }
        private List<BlockUpdate> blockUpdates = new ArrayList<>();

        private List<Interaction> interactions = new ArrayList<>();
        @Data @AllArgsConstructor
        public static class Interaction {
            private long time;
            private BlockPos pos;
        }
        private List<ChatMessage> systemMessages = new ArrayList<>();
        @Data @AllArgsConstructor
        public static class ChatMessage {
            private long time;
            private IChatComponent chat;
        }


        private Map<Integer, EntityData> entityData = new HashMap<>();
        private LinkedList<EntityData.EntityTrajectory> playerTrajactory = new LinkedList<>();
        private int minX, minZ;
    }

    @DGEventHandler(triggerOutOfSkyblock = true, ignoreDisabled = true)
    public void onEntitySpawn(EntityJoinWorldEvent event) {
        if (event.entity instanceof EntityArmorStand) {
            return;
        }
        if (event.entity instanceof EntityArrow) {
            return;
        }
        if (entityDataMap.get(event.entity.getEntityId()) != null) return;
        EntityData entityData = new EntityData();
        entityData.id = event.entity.getEntityId();
        entityData.trajectory.add(new EntityData.EntityTrajectory(
                EntityData.EntityTrajectory.Type.ENTER,
                event.entity.getPositionVector(),
                System.currentTimeMillis()
        ));
        entityData.type = event.entity.getClass().getSimpleName();
        entityDataMap.put(event.entity.getEntityId(), entityData);

        Point roompt = Optional.ofNullable(DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext())
                .map(DungeonContext::getScaffoldParser)
                .map(DungeonRoomScaffoldParser::getDungeonMapLayout)
                .map(a -> a.worldPointToRoomPoint(event.entity.getPosition())).orElse(null);
        if (roompt == null) return;;
        DungeonContext dungeonContext = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        DungeonRoom dungeonRoom = dungeonContext.getScaffoldParser().getRoomMap().get(roompt);
        if (dungeonRoom != null) {
            roomInfoMap.get(dungeonRoom).entityData.put(event.entity.getEntityId(), entityData);
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
        IBlockState blockState = Minecraft.getMinecraft().theWorld.getBlockState(event.pos);
        if (blockState == null) return;
        if (!(blockState.getBlock() == Blocks.lever || blockState.getBlock() == Blocks.chest || blockState.getBlock() == Blocks.trapped_chest || blockState.getBlock() == Blocks.skull)) {
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
    public void onChat(ClientChatReceivedEvent event) {
        if (event.type == 2) return;
        if (!event.message.getFormattedText().contains(":")) {
            // this is not user message.
            BlockPos pos = Minecraft.getMinecraft().thePlayer.getPosition();

            DungeonContext dungeonContext = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
            if (dungeonContext == null|| dungeonContext.getScaffoldParser() == null) return;
            Point roompt = dungeonContext.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(pos);
            DungeonRoom dungeonRoom = dungeonContext.getScaffoldParser().getRoomMap().get(roompt);
            if (dungeonRoom == null) return;
            RoomInfo roomInfo = roomInfoMap.get(dungeonRoom);
            if (roomInfo == null) return;

            roomInfo.systemMessages.add(new RoomInfo.ChatMessage(System.currentTimeMillis(), event.message));
        }
    }
    private int lastNo = 0;
    private int totalSecret = 0;
    @DGEventHandler(ignoreDisabled = true)
    public void onTick(DGTickEvent tickEvent) {
        int secret = FeatureRegistry.DUNGEON_SECRETS_ROOM.getLatestCurrSecrets();
        int total = FeatureRegistry.DUNGEON_SECRETS_ROOM.getLatestTotalSecrets();
        if (secret != lastNo || total != totalSecret) {
            lastNo = secret;
            totalSecret = total;
            BlockPos pos = Minecraft.getMinecraft().thePlayer.getPosition();

            DungeonContext dungeonContext = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
            if (dungeonContext == null|| dungeonContext.getScaffoldParser() == null) return;
            Point roompt = dungeonContext.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(pos);
            DungeonRoom dungeonRoom = dungeonContext.getScaffoldParser().getRoomMap().get(roompt);
            if (dungeonRoom == null) return;
            RoomInfo roomInfo = roomInfoMap.get(dungeonRoom);
            if (roomInfo == null) return;

            roomInfo.systemMessages.add(new RoomInfo.ChatMessage(System.currentTimeMillis(), new ChatComponentText("SECRET UPDATE: "+secret+"/"+total)));
        }

        DungeonContext dungeonContext = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (dungeonContext == null || dungeonContext.getScaffoldParser() == null) return;
        Point roompt = dungeonContext.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(Minecraft.getMinecraft().thePlayer.getPosition());
        DungeonRoom dungeonRoom = dungeonContext.getScaffoldParser().getRoomMap().get(roompt);
        if (dungeonRoom == null) return;
        RoomInfo roomInfo = roomInfoMap.get(dungeonRoom);
        if (roomInfo == null) return;
        if (roomInfo.playerTrajactory.size() == 0 || roomInfo.playerTrajactory.getLast().getPos() == null || roomInfo.playerTrajactory.getLast().getPos().squareDistanceTo(Minecraft.getMinecraft().thePlayer.getPositionVector()) > 0.1f) {
            roomInfo.playerTrajactory.add(new EntityData.EntityTrajectory(EntityData.EntityTrajectory.Type.MOVE, Minecraft.getMinecraft().thePlayer.getPositionVector(), System.currentTimeMillis()));
        }
    }

    @DGEventHandler(triggerOutOfSkyblock = true, ignoreDisabled = true)
    public void onEntityAttributeUpdate(LivingEvent.LivingUpdateEvent event) {
        if (event.entityLiving instanceof EntityArmorStand) {
            return;
        }
        if (event.entity instanceof EntityArrow) {
            return;
        }
        EntityData entityData = entityDataMap.get(event.entity.getEntityId());
        if (entityData == null) {
//            System.out.println("WTFF??? it's not on map?? "+event.entity);
            return;
        }
        entityData.id = event.entity.getEntityId();

        if (event.entity instanceof EntityOtherPlayerMP && entityData.playerSkin == null) {
            entityData.playerSkin = ((EntityOtherPlayerMP) event.entity).getGameProfile().getProperties().get("textures").stream().findFirst().map(Property::getValue).orElse(null);
        }

        List<Entity> entityList = event.entity.worldObj.getEntitiesInAABBexcluding(event.entity, new AxisAlignedBB(-0.2,-0.2,-0.2,0.2,0.2,0.2).offset(event.entity.posX, event.entity.posY+event.entity.height, event.entity.posZ), e -> e instanceof EntityArmorStand);
        Entity theEntity =entityList.stream().min(Comparator.comparingDouble(a -> Math.abs(a.posX - event.entityLiving.posX) + Math.abs(a.posZ - event.entityLiving.posZ))).orElse(null);

        if (theEntity != null && entityData.armorstand == null)
            entityData.armorstand = theEntity.getDisplayName();

        entityData.name = event.entityLiving.getName();

        if (event.entityLiving.getHeldItem() != null)
            entityData.armoritems[4] = event.entityLiving.getHeldItem();
        if (event.entityLiving.getCurrentArmor(0) != null)
            entityData.armoritems[0] = event.entityLiving.getCurrentArmor(0);
        if (event.entityLiving.getCurrentArmor(1) != null)
            entityData.armoritems[1] = event.entityLiving.getCurrentArmor(1);
        if (event.entityLiving.getCurrentArmor(2) != null)
            entityData.armoritems[2] = event.entityLiving.getCurrentArmor(2);
        if (event.entityLiving.getCurrentArmor(3) != null)
            entityData.armoritems[3] = event.entityLiving.getCurrentArmor(3);
        if (entityData.trajectory.getLast() == null || entityData.trajectory.getLast().getPos() == null || entityData.trajectory.getLast().getPos().squareDistanceTo(event.entity.getPositionVector()) > 0.1f) {
            entityData.trajectory.add(new EntityData.EntityTrajectory(EntityData.EntityTrajectory.Type.MOVE, event.entity.getPositionVector(), System.currentTimeMillis()));
        }
    }

    @DGEventHandler
    public void onEntityDespawn(LivingDeathEvent event) {
//        System.out.println("Entity died!!:" +event.entity);
        EntityData entityData = entityDataMap.get(event.entity.getEntityId());
        if (entityData != null) {
            entityData.trajectory.add(new EntityData.EntityTrajectory(EntityData.EntityTrajectory.Type.DEATH, event.entity.getPositionVector(), System.currentTimeMillis()));
        }
    }

    @DGEventHandler(triggerOutOfSkyblock = true, ignoreDisabled = true)
    public void onChunkLoad(ChunkUpdateEvent chunkUpdateEvent) {
        Set<Tuple<BlockPos, IBlockState>> updates = new HashSet<>();
        for (Chunk updatedChunk : chunkUpdateEvent.getUpdatedChunks()) {
            if (updatedChunk.isEmpty()) continue;
            if (initialChunkDataMap.containsKey(updatedChunk.getChunkCoordIntPair())) {
                // that's block update!

                ChunkData prevChunk = initialChunkDataMap.get(updatedChunk.getChunkCoordIntPair());
                ExtendedBlockStorage[] prev = prevChunk.getInitialBlockStorages();
                ExtendedBlockStorage[] neu = updatedChunk.getBlockStorageArray();
                for (int i = 0; i < prev.length; i++) {
                    ExtendedBlockStorage prevSt = prev[i];
                    ExtendedBlockStorage neuSt = neu[i];
                    if (prevSt == null && neuSt != null) {
                        for (int x = 0; x < 16; x++) {
                            for (int y = 0; y < 16; y++) {
                                for (int z = 0; z < 16; z++) {
                                    IBlockState blockState = neuSt.get(x,y,z);
                                    BlockPos pos = new BlockPos(prevChunk.x * 16 + x,  i * 16 + y, prevChunk.z * 16 + z);
                                    updates.add(new Tuple<>(pos, blockState));
                                }
                            }
                        }
                    } else if (prevSt != null && neuSt == null) {
                        IBlockState air = Blocks.air.getDefaultState();
                        for (int x = 0; x < 16; x++) {
                            for (int y = 0; y < 16; y++) {
                                for (int z = 0; z < 16; z++) {
                                    BlockPos pos = new BlockPos(prevChunk.x * 16 + x, i * 16 + y, prevChunk.z * 16 + z);
                                    updates.add(new Tuple<>(pos, air));
                                }
                            }
                        }
                    } else if (prevSt == null && neuSt == null) {
                    } else {
                        for (int x = 0; x < 16; x++) {
                            for (int y = 0; y < 16; y++) {
                                for (int z = 0; z < 16; z++) {
                                    BlockPos pos = new BlockPos(prevChunk.x * 16 + x, i * 16 + y, prevChunk.z * 16 + z);
                                    IBlockState prevState = prevSt.get(x,y,z);
                                    IBlockState neuState = neuSt.get(x,y,z);
                                    if (!neuState.equals(prevState)) {
                                        updates.add(new Tuple<>(pos, neuState));
                                    }
                                }
                            }
                        }
                    }
                }

                continue;
            }
            ChunkData chunkData = new ChunkData();
            chunkData.x = updatedChunk.xPosition;
            chunkData.z = updatedChunk.zPosition;
            chunkData.initialBlockStorages = updatedChunk.getBlockStorageArray();
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
        Map<DungeonRoom, List<Tuple<BlockPos, IBlockState>>> updatePerRoom = blockUpdateEvent.getUpdatedBlocks().stream()
                .filter(a -> {
                    Point roompt = dungeonContext.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(a.getFirst());
                    return dungeonContext.getScaffoldParser().getRoomMap().get(roompt) != null;
                })
                .collect(Collectors.groupingBy(a -> {
                            Point roompt = dungeonContext.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(a.getFirst());
                            return dungeonContext.getScaffoldParser().getRoomMap().get(roompt);
                        }));

        for (Map.Entry<DungeonRoom, List<Tuple<BlockPos, IBlockState>>> dungeonRoomListEntry : updatePerRoom.entrySet()) {
            if (dungeonRoomListEntry.getKey() == null) {
                System.out.println("WTF!!!");
            }
            RoomInfo roomInfo = roomInfoMap.get(dungeonRoomListEntry.getKey());
            roomInfo.blockUpdates.add(new RoomInfo.BlockUpdate(dungeonRoomListEntry.getValue().stream().map(it -> new RoomInfo.BlockUpdate.BlockUpdateData(it.getFirst(), it.getSecond())).collect(Collectors.toList()), System.currentTimeMillis()));
            roomInfo.minX = dungeonRoomListEntry.getKey().getMin().getX();
            roomInfo.minZ = dungeonRoomListEntry.getKey().getMin().getZ();

        }
    }
    @DGEventHandler(triggerOutOfSkyblock = true, ignoreDisabled = true)
    public void onDungeonRoomDiscover(DungeonRoomDiscoveredEvent discoveredEvent) {
        DungeonRoom dungeonRoom = discoveredEvent.getDungeonRoom();
        DungeonContext dungeonContext = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        RoomInfo roomInfo = new RoomInfo();
        for (EntityData value : entityDataMap.values()) {
            if (dungeonRoom.getUnitPoints().contains(dungeonContext.getScaffoldParser().getDungeonMapLayout().worldPointToRoomPoint(new BlockPos(value.trajectory.getFirst().pos)))) {
                roomInfo.entityData.put(value.id, value);
            }
        }

        roomInfoMap.put(dungeonRoom, roomInfo);
    }

    @DGEventHandler(triggerOutOfSkyblock = true, ignoreDisabled = true)
    public void onWorldLoad(WorldEvent.Unload event) {
        try {
            Gson gson = new GsonBuilder()
                    .disableHtmlEscaping()
                    .registerTypeAdapter(ItemStack.class, new TypeAdapter<ItemStack>() {
                        @Override
                        public void write(JsonWriter out, ItemStack value) throws IOException {
                            if (value == null) {
                                out.nullValue();
                            } else {
                                out.value(nbttostring("item", value.getTagCompound()));
                            }
                        }

                        @Override
                        public ItemStack read(JsonReader in) throws IOException {
                            return null;
                        }
                    })
                    .registerTypeAdapter(IBlockState.class, new TypeAdapter<IBlockState>() {
                        @Override
                        public void write(JsonWriter out, IBlockState value) throws IOException {
                            int id = Block.getIdFromBlock(value.getBlock());
                            int meta = value.getBlock().getMetaFromState(value);
                            out.value(id+":"+meta);
                        }

                        @Override
                        public IBlockState read(JsonReader in) throws IOException {
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
                            int id = Block.getIdFromBlock(value.getBlock().getBlock());
                            int meta = value.getBlock().getBlock().getMetaFromState(value.getBlock());
                            out.beginArray().value(value.getPos().getX()).value(value.getPos().getY()).value(value.getPos().getZ()).value(id+":"+meta).endArray();
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
                jsonObject.addProperty("shape", dungeonRoomRoomInfoEntry.getKey().getShape());
                jsonObject.addProperty("color", dungeonRoomRoomInfoEntry.getKey().getColor());
                jsonObject.addProperty("secrets", dungeonRoomRoomInfoEntry.getKey().getTotalSecrets());
                jsonObject.add("entities", gson.toJsonTree(roomInfo.entityData));
                jsonObject.add("blockupdates", gson.toJsonTree(roomInfo.blockUpdates));
                jsonObject.add("chats", gson.toJsonTree(roomInfo.systemMessages));
                jsonObject.add("interactions", gson.toJsonTree(roomInfo.interactions));
                jsonObject.add("player", gson.toJsonTree(roomInfo.playerTrajactory));
                jsonObject.addProperty("version", "2");



                NBTTagCompound nbtTagCompound2 = createNBT(roomInfo, dungeonRoomRoomInfoEntry.getKey());
                jsonObject.addProperty("schematic", nbttostring("Schematic", nbtTagCompound2));

                try {
                    String str = gson.toJson(jsonObject);
                    queueSendLogAsync(str);

                    // if is dev
                    FileOutputStream fos = new FileOutputStream(new File(Main.getConfigDir(), "runs/"+UUID.randomUUID()+".dgroom"));
                    JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(fos));
                    gson.toJson(jsonObject, jsonWriter);
                    jsonWriter.flush(); jsonWriter.close();
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


    private String nbttostring(String name, NBTTagCompound compound) {

        try {
            Method method = ReflectionHelper.findMethod(NBTTagCompound.class, compound, new String[] {"write", "method_5062", "a"}, DataOutput.class);


            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream dataoutputstream = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(byteArrayOutputStream)));

            dataoutputstream.writeByte(compound.getId());

            dataoutputstream.writeUTF(name);
            method.invoke(compound, dataoutputstream);
            dataoutputstream.close();
            byte[] arr = byteArrayOutputStream.toByteArray();
            return Base64.getEncoder().encodeToString(arr);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private NBTTagCompound createNBT(RoomInfo roomInfo, DungeonRoom dungeonRoom) {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setShort("Width", (short) (dungeonRoom.getMax().getX() - dungeonRoom.getMin().getX() + 1));
        compound.setShort("Height", (short) 255);
        compound.setShort("Length", (short) (dungeonRoom.getMax().getZ() - dungeonRoom.getMin().getZ() + 1));
        int size =compound.getShort("Width") * compound.getShort("Height") * compound.getShort("Length");

        byte[] blocks = new byte[size];
        byte[] meta = new byte[size];
        byte[] extra = new byte[size];
        byte[] extraNibble = new byte[(int) Math.ceil(size / 2.0)];

        boolean extraEx = false;
        NBTTagList tileEntitiesList = new NBTTagList();
        for (int x = 0; x < compound.getShort("Width"); x++) {
            for (int y = 0; y <  compound.getShort("Height"); y++) {
                for (int z = 0; z < compound.getShort("Length"); z++) {
                    int index = x + (y * compound.getShort("Length") + z) * compound.getShort("Width");
                    BlockPos pos = dungeonRoom.getRelativeBlockPosAt(x,y - 70,z);
                    ChunkData chunkData = initialChunkDataMap.get(new ChunkCoordIntPair(pos.getX() >> 4, pos.getZ() >> 4));

                    IBlockState blockState;
                    if (chunkData != null) {

                        ExtendedBlockStorage[] blockStorage = chunkData.initialBlockStorages;

                            if (pos.getY() >= 0 && pos.getY() >> 4 < blockStorage.length) {
                                ExtendedBlockStorage extendedblockstorage = blockStorage[pos.getY() >> 4];
                                if (extendedblockstorage != null) {
                                    int j = pos.getX() & 15;
                                    int k = pos.getY() & 15;
                                    int i = pos.getZ() & 15;
                                    blockState = extendedblockstorage.get(j, k, i);
                                } else {
                                    blockState = Blocks.air.getDefaultState();
                                }
                            } else {
                                blockState = Blocks.air.getDefaultState();
                            }

                    } else {
                        blockState = Blocks.air.getDefaultState();
                    }

                    int id = Block.getIdFromBlock(blockState.getBlock());
                    blocks[index] = (byte) id;
                    meta[index] =  (byte) blockState.getBlock().getMetaFromState(blockState);
                    if ((extra[index] = (byte) ((id) >> 8)) > 0) {
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


        compound.setByteArray("Blocks", blocks);
        compound.setByteArray("Data", meta);
        compound.setString("Materials", "Alpha");
        if (extraEx) {
            compound.setByteArray("AddBlocks", extraNibble);
        }
        compound.setTag("Entities", new NBTTagList());
        compound.setTag("TileEntities", tileEntitiesList);

        return compound;
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
                RenderUtils.drawTextAtWorld(entityData.getArmorstand().getFormattedText(), (float) hovered.posX, (float) hovered.posY+3, (float) hovered.posZ, 0xFF000000, 0.02f, false, true, event.partialTicks);
            RenderUtils.drawTextAtWorld(entityData.getType(), (float) hovered.posX, (float) hovered.posY+3.2f, (float) hovered.posZ, 0xFF00FF00, 0.02f, false, true, event.partialTicks);
            Vec3 pos = entityData.getTrajectory().getFirst().getPos();
            RenderUtils.renderBeaconBeam(
                    pos.xCoord,
                    pos.yCoord,
                    pos.zCoord,
                    new AColor(0, 255, 0, 255),
                    event.partialTicks
            );
            List<Vec3> lines = new ArrayList<>();
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
