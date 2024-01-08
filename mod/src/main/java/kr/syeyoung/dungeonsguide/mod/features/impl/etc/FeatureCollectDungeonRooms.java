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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import jdk.nashorn.internal.ir.debug.JSONWriter;
import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.launcher.gui.screen.GuiDisplayer;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.config.types.TCBoolean;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.map.DungeonMapLayout;
import kr.syeyoung.dungeonsguide.mod.dungeon.map.DungeonRoomScaffoldParser;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.*;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.guiv2.GuiScreenAdapter;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Scaler;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.world.WorldEvent;

import java.awt.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

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
        private String armorstand;
        private transient ItemStack[] armoritems = new ItemStack[5];
        private Map<String, Double> attributes = new HashMap<>();
        private List<DataWatcher.WatchableObject> metadata;

        private String type;
        private boolean isSelf;

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
        private Map<Integer, EntityData> entityData = new HashMap<>();
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
            System.out.println("WTFF??? it's not on map?? "+event.entity);
            return;
        }
        entityData.id = event.entity.getEntityId();


        List<Entity> entityList = event.entity.worldObj.getEntitiesInAABBexcluding(event.entity, new AxisAlignedBB(-0.2,-0.2,-0.2,0.2,0.2,0.2).offset(event.entity.posX, event.entity.posY+event.entity.height, event.entity.posZ), e -> e instanceof EntityArmorStand);
        Entity theEntity =entityList.stream().min(Comparator.comparingDouble(a -> Math.abs(a.posX - event.entityLiving.posX) + Math.abs(a.posZ - event.entityLiving.posZ))).orElse(null);
//EntityPigZombie
        if (theEntity != null)
            entityData.armorstand = theEntity.getDisplayName().getFormattedText();

        entityData.metadata = event.entityLiving.getDataWatcher().getAllWatched();
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
        if (entityData.trajectory.getLast().getPos() == null || entityData.trajectory.getLast().getPos().squareDistanceTo(event.entity.getPositionVector()) > 0.1f) {
            entityData.trajectory.add(new EntityData.EntityTrajectory(EntityData.EntityTrajectory.Type.MOVE, event.entity.getPositionVector(), System.currentTimeMillis()));
        }
    }

    @DGEventHandler
    public void onEntityDespawn(LivingDeathEvent event) {
        System.out.println("Entity died!!:" +event.entity);
        EntityData entityData = entityDataMap.get(event.entity.getEntityId());
        if (entityData != null) {
            entityData.trajectory.add(new EntityData.EntityTrajectory(EntityData.EntityTrajectory.Type.DEATH, event.entity.getPositionVector(), System.currentTimeMillis()));
        }
    }

    @DGEventHandler(triggerOutOfSkyblock = true, ignoreDisabled = true)
    public void onChunkLoad(ChunkUpdateEvent chunkUpdateEvent) {
        for (Chunk updatedChunk : chunkUpdateEvent.getUpdatedChunks()) {
            if (initialChunkDataMap.containsKey(updatedChunk.getChunkCoordIntPair())) {
                System.out.println("got it again??");
//                return;
            }
            ChunkData chunkData = new ChunkData();
            chunkData.x = updatedChunk.xPosition;
            chunkData.z = updatedChunk.zPosition;
            chunkData.initialBlockStorages = updatedChunk.getBlockStorageArray();
            initialChunkDataMap.put(new ChunkCoordIntPair(chunkData.x, chunkData.z), chunkData);
        }
    }

    @DGEventHandler(triggerOutOfSkyblock = true, ignoreDisabled = true)
    public void onBlockUpdate(BlockUpdateEvent.Pre blockUpdateEvent) {
        DungeonContext dungeonContext = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        if (dungeonContext == null) return;
        if (dungeonContext.getScaffoldParser() == null) return;
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
                    .create();
            for (Map.Entry<DungeonRoom, RoomInfo> dungeonRoomRoomInfoEntry : roomInfoMap.entrySet()) {
                JsonObject jsonObject = new JsonObject();
                DungeonRoomInfo dri = dungeonRoomRoomInfoEntry.getKey().getDungeonRoomInfo();
                if (dri != null) {
                    jsonObject.addProperty("uuid", dri.getUuid().toString());
                    jsonObject.addProperty("name", dri.getName());
                }
                jsonObject.addProperty("rot", dungeonRoomRoomInfoEntry.getKey().getRoomMatcher().getRotation());
                RoomInfo roomInfo = dungeonRoomRoomInfoEntry.getValue();
                jsonObject.addProperty("minX", roomInfo.minX);
                jsonObject.addProperty("minZ", roomInfo.minZ);
                jsonObject.add("entities", gson.toJsonTree(roomInfo.entityData));
                jsonObject.add("blockupdates", gson.toJsonTree(roomInfo.blockUpdates));



                NBTTagCompound nbtTagCompound2 = createNBT(roomInfo, dungeonRoomRoomInfoEntry.getKey());
                jsonObject.addProperty("schematic", nbttostring("Schematic", nbtTagCompound2));

                try {
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

    private String nbttostring(String name, NBTTagCompound compound) {

        try {
            Method method = null;
            method = NBTTagCompound.class.getDeclaredMethod("write", DataOutput.class);
            method.setAccessible(true);

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

    @DGEventHandler
    public void onRender(RenderGameOverlayEvent.Post postRender) {
        if (DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext() == null) return;

    }
}
