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

package kr.syeyoung.dungeonsguide.mod.features.impl.advanced;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonFairySoul;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.dunegonmechanic.DungeonMechanic;
import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.config.types.TCKeybind;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.doorfinder.DungeonSpecificDataProvider;
import kr.syeyoung.dungeonsguide.mod.dungeon.doorfinder.DungeonSpecificDataProviderRegistry;
import kr.syeyoung.dungeonsguide.mod.dungeon.map.DungeonMapLayout;
import kr.syeyoung.dungeonsguide.mod.dungeon.map.DungeonRoomScaffoldParser;
import kr.syeyoung.dungeonsguide.mod.dungeon.mocking.TESTDGProvider;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.gui.GuiDungeonRoomEdit;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoomInfoRegistry;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bossfight.BossfightProcessor;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.DGTickEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.KeyBindPressedEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCollectDungeonRooms;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.FeatureSoulRoomWarning;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.GuiScreenAdapter;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Placeholder;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.WidgetList;
import kr.syeyoung.dungeonsguide.mod.utils.ArrayUtils;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.mod.utils.ShortUtils;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.realms.RealmsBridge;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.io.IOUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import javax.swing.*;
import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FeatureRoomEdit  extends SimpleFeature {
    public FeatureRoomEdit() {
        super("Debug", "Room Edit", "Allow editing dungeon rooms\n\nWarning: using this feature can break or freeze your Minecraft\nThis is for DEVELOPERS WHO KNOW WHAT THEY ARE DOING only", "advanced.roomedit", false);

        addParameter("key", new FeatureParameter<Integer>("key", "Key", "Press to edit room", Keyboard.KEY_R, TCKeybind.INSTANCE));
    }

    @Override
    public void setupConfigureWidget(List<Widget> widgets) {
        super.setupConfigureWidget(widgets);
        widgets.add(new RoomConfiguration());
    }

    public void overwrite(boolean ignoreAir) {
        if (!flag) return;
        if (schematic == null) return;

        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonFacade().getContext();
        DungeonRoom dungeonRoom = context.getScaffoldParser().getDungeonRoomList().get(0);
        DungeonRoomInfo info = dungeonRoom.getDungeonRoomInfo();

        OffsetPoint offsetPoint = new OffsetPoint(dungeonRoom, new BlockPos(0,0,0));

        NBTTagCompound compound = schematic;
        int w = compound.getShort("Width");
        int l = compound.getShort("Length");
        if (dungeonRoom.getRoomMatcher().getRotation() % 2 == 1) {
            int temp = l;
            l = w;
            w = temp;
        }
        if (!info.hasSchematic())
            info.setSize(w,l,256);

        byte[] blocks = compound.getByteArray("Blocks");
        byte[] meta = compound.getByteArray("Data");
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        for (int x = 1; x < compound.getShort("Width"); x++) {
            for (int y = 0; y < compound.getShort("Height"); y++) {
                for (int z = 1; z < compound.getShort("Length"); z++) {
                    int index = x + (y * compound.getShort("Length") + z) * compound.getShort("Width");
                    mpos.set(x,y,z);
                    offsetPoint.setPosInWorld(dungeonRoom, mpos);

                    Block b = Block.getBlockById(blocks[index] & 0xFF);
                    Optional<PropertyDirection> propertyDirection = b.getDefaultState().getPropertyNames().stream()
                            .filter(a -> a instanceof PropertyDirection)
                            .map(PropertyDirection.class::cast).findFirst();

                    if (!dungeonRoom.canAccessRelative(x,z)) {
                        continue;
                    }


                    IBlockState blockState = b.getStateFromMeta(meta[index] & 0xFF);
                    if (propertyDirection.isPresent()) {
                        EnumFacing enumFacing = blockState.getValue(propertyDirection.get());
                        if (!(enumFacing == EnumFacing.UP || enumFacing == EnumFacing.DOWN)) {
                            for (int i = 0; i < dungeonRoom.getRoomMatcher().getRotation(); i++)
                                enumFacing = enumFacing.rotateY();
                            blockState = blockState.withProperty(propertyDirection.get(), enumFacing);
                        }
                    }

                    if ((blocks[index] & 0xFF) != 0 || !ignoreAir)
                        info.setBlock(offsetPoint, blockState);
                }
            }
        }
    }


    private File f;
    private void load(File f) {
        this.f = f;

        Gson gson = new Gson();
        JsonObject jsonObject;
        try {
            jsonObject = gson.fromJson(IOUtils.toString(f.toURI()), JsonObject.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (Minecraft.getMinecraft().isSingleplayer() && MinecraftServer.getServer().getFolderName().equals("dungeonsguide") && schematic != null) {
            NBTTagCompound compound = schematic;
            for (int x = 0; x < compound.getShort("Width") + 16; x+= 1) {
                for (int y = 0; y < compound.getShort("Height"); y++) {
                    for (int z = 0; z < compound.getShort("Length") + 16; z+=1) {
                        BlockPos pos = new BlockPos(x, 0, z);
                        World w = MinecraftServer.getServer().getEntityWorld();
                        if (x % 16 == 0 && z % 16 == 0 && y == 0) {
                            for (int i = 0; i < w.getChunkFromBlockCoords(pos).getBlockStorageArray().length; i++) {
                                w.getChunkFromBlockCoords(pos).getBlockStorageArray()[i] = null;
                            }
                        }
                        w.markBlockForUpdate(pos);
                    }
                }
            }
            onWorldUnload(null);
        } else {
            if (Minecraft.getMinecraft().theWorld != null) {
                boolean flag = Minecraft.getMinecraft().isIntegratedServerRunning();
                boolean flag1 = Minecraft.getMinecraft().isConnectedToRealms();
                Minecraft.getMinecraft().theWorld.sendQuittingDisconnectingPacket();
                Minecraft.getMinecraft().loadWorld((WorldClient) null);
            }


            ISaveFormat isaveformat = Minecraft.getMinecraft().getSaveLoader();
            isaveformat.flushCache();
            isaveformat.deleteWorldDirectory("dungeonsguide");



            WorldType.FLAT.onGUICreateWorldPress();
            WorldSettings.GameType worldsettings$gametype = WorldSettings.GameType.CREATIVE;
            WorldSettings worldsettings = new WorldSettings(0, worldsettings$gametype, false, false, WorldType.FLAT);
            worldsettings.setWorldName("3;minecraft:air");
            worldsettings.enableCommands();

            Minecraft.getMinecraft().launchIntegratedServer("dungeonsguide", "dungeonsguide", worldsettings);
        }
        blockUpdates = new ArrayList<>();
        int minX = jsonObject.get("minX").getAsInt(), minZ = jsonObject.get("minZ").getAsInt();
        for (JsonElement updates : jsonObject.get("blockupdates").getAsJsonArray()) {
            List<FeatureCollectDungeonRooms.RoomInfo.BlockUpdate.BlockUpdateData> list = new ArrayList<>();
            for (JsonElement updatedBlocks : updates.getAsJsonObject().get("updatedBlocks").getAsJsonArray()) {
                JsonArray pos = updatedBlocks.getAsJsonArray();
                BlockPos bPos = new BlockPos(pos.get(0).getAsInt()-minX, pos.get(1).getAsInt(), pos.get(2).getAsInt()-minZ);
                String[] block = pos.get(3).getAsString().split(":");
                Block b = Block.getBlockById(Integer.parseInt(block[0]));
                IBlockState blockState = b.getStateFromMeta(Integer.parseInt(block[1]));
                FeatureCollectDungeonRooms.RoomInfo.BlockUpdate.BlockUpdateData data = new FeatureCollectDungeonRooms.RoomInfo.BlockUpdate.BlockUpdateData(bPos, blockState);
                list.add(data);
            }
            long time = updates.getAsJsonObject().get("time").getAsLong();

            blockUpdates.add(new FeatureCollectDungeonRooms.RoomInfo.BlockUpdate(list, time));
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        MinecraftServer.getServer().getEntityWorld().setSpawnPoint(new BlockPos(0, 100, 0));
        MinecraftServer.getServer().getEntityWorld().getGameRules().setOrCreateGameRule("doMobSpawning", "false");
        MinecraftServer.getServer().getEntityWorld().getGameRules().setOrCreateGameRule("doDaylightCycle", "false");
        MinecraftServer.getServer().getEntityWorld().getGameRules().setOrCreateGameRule("randomTickSpeed", "0");

        shape = jsonObject.get("shape").getAsShort();
        color = jsonObject.get("color").getAsByte();

        NBTTagCompound compound;
        try {
            compound = CompressedStreamTools.readCompressed(new ByteArrayInputStream(Base64.getDecoder().decode(
                    jsonObject.get("schematic").getAsString()
            )));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        schematic = compound;
        byte[] blocks = compound.getByteArray("Blocks");
        byte[] meta = compound.getByteArray("Data");
        List<FeatureCollectDungeonRooms.RoomInfo.BlockUpdate.BlockUpdateData> datas = new ArrayList<>();
        for (int x = 0; x < compound.getShort("Width"); x++) {
            for (int y = 0; y < compound.getShort("Height"); y++) {
                for (int z = 0; z < compound.getShort("Length"); z++) {
                    if (!( (shape >>((z/32) *4 +(x/32)) & 0x1) > 0)) {
                        continue;
                    }

                    int index = x + (y * compound.getShort("Length") + z) * compound.getShort("Width");
                    BlockPos pos = new BlockPos(x, y, z);
                    World w = MinecraftServer.getServer().getEntityWorld();
                    Chunk c = w.getChunkFromBlockCoords(pos);
                    w.markBlockForUpdate(pos);
                    ExtendedBlockStorage[] storage= c.getBlockStorageArray();
                    ExtendedBlockStorage extendedblockstorage = storage[y >> 4];
                    if (extendedblockstorage == null) {
                        if ((blocks[index] & 0xFF) == 0) {
                            continue;
                        }
                        extendedblockstorage = storage[y >> 4] = new ExtendedBlockStorage(y >> 4 << 4, true);
                    }
                    extendedblockstorage.set(x & 0xF, y & 15, z & 0xF, Block.getBlockById(blocks[index] & 0xFF).getStateFromMeta(meta[index] & 0xFF));
                    if ((blocks[index] & 0xFF) == 23) {
                        datas.add(new FeatureCollectDungeonRooms.RoomInfo.BlockUpdate.BlockUpdateData(new BlockPos(x, y, z),  Blocks.dropper.getStateFromMeta(meta[index] & 0xFF)));
                    }
                }
            }
        }
        blockUpdates.add(new FeatureCollectDungeonRooms.RoomInfo.BlockUpdate(datas, System.currentTimeMillis()));
        xWid = (compound.getShort("Width") + 5) / 32;
        zWid = (compound.getShort("Length") + 5) / 32;

        FeatureRoomEdit.this.flag = true;
        FeatureRoomEdit.this.setup = false;
    }

    public class RoomConfiguration extends AnnotatedImportOnlyWidget {

        @Bind(
                variableName = "rooms"
        )
        public final BindableAttribute rooms = new BindableAttribute(WidgetList.class);

        private List<FeatureRoomEdit.RoomSwitch> switches;
        public RoomConfiguration() {
            super(new ResourceLocation("dungeonsguide:gui/features/roomedit/roomconfiguration.gui"));


            rooms.setValue(switches = buildRooms());
        }

        public List<FeatureRoomEdit.RoomSwitch> buildRooms() {
            List<FeatureRoomEdit.RoomSwitch> switches1 = new LinkedList<>();
            for (DungeonRoomInfo dungeonRoomInfo : DungeonRoomInfoRegistry.getRegistered()) {
                FeatureRoomEdit.RoomSwitch roomSwitch = new FeatureRoomEdit.RoomSwitch(dungeonRoomInfo);
                switches1.add(roomSwitch);
            }
            return switches1;
        }

        @On(functionName = "next")
        public void next() {

            try {
                List<Path> files = Files.list(Paths.get(f.getParent()))
                        .sorted()
                        .collect(Collectors.toList());
                System.out.println(files);
                int nextFile = files.indexOf(Paths.get(f.toURI())) + 1;
                ChatTransmitter.sendDebugChat("Loading " + files.get(nextFile));
                load(new File(files.get(nextFile).toUri()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @On(functionName = "reload")
        public void reload() {
            setup = false;
        }

        @On(functionName = "prev")
        public void prev() {

            try {
                List<Path> files = Files.list(Paths.get(f.getParent()))
                        .sorted()
                        .collect(Collectors.toList());
                System.out.println(files);
                int nextFile = files.indexOf(Paths.get(f.toURI())) - 1;
                ChatTransmitter.sendDebugChat("Loading " + files.get(nextFile));
                load(new File(files.get(nextFile).toUri()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        @On(functionName = "loaddgrun")
        public void loadDGRun() {

            final JFileChooser[] fc = new JFileChooser[1];
            final int[] returnVal = new int[1];
            try {
                EventQueue.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        fc[0] = new JFileChooser(Main.getConfigDir());
                        returnVal[0] = fc[0].showOpenDialog(null);
                    }
                });
            } catch (InterruptedException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            if (returnVal[0] != JFileChooser.APPROVE_OPTION) return;

            File f = fc[0].getSelectedFile();
            try {
                load(f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private boolean flag;
    private short shape;
    private byte color;
    private int xWid, zWid;
    private boolean setup = false;
    private NBTTagCompound schematic;

    @Getter
    private List<FeatureCollectDungeonRooms.RoomInfo.BlockUpdate> blockUpdates;

    @DGEventHandler
    public void onKey(KeyBindPressedEvent event) {
        if (event.getKey() == 68) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiScreenAdapter(new RoomConfiguration()));
        }
    }
    @DGEventHandler()
    public void showBlockUpdates(RenderWorldLastEvent event) {
        if (blockUpdates != null) {
            GlStateManager.enableDepth();
            GlStateManager.enableCull();
            for (FeatureCollectDungeonRooms.RoomInfo.BlockUpdate blockUpdate : blockUpdates) {
                for (FeatureCollectDungeonRooms.RoomInfo.BlockUpdate.BlockUpdateData updatedBlock : blockUpdate.getUpdatedBlocks()) {
                    if (Minecraft.getMinecraft().thePlayer.getDistanceSq(updatedBlock.getPos()) > 100)
                        RenderUtils.highlightBlock(updatedBlock.getPos(), new Color(0x33FFFF00, true), event.partialTicks, false);
                    int meta1 = updatedBlock.getBlock().getBlock().getMetaFromState(updatedBlock.getBlock());
                    Block block1 = updatedBlock.getBlock().getBlock();
                    IBlockState blockstate2 = Minecraft.getMinecraft().theWorld.getBlockState(updatedBlock.getPos());
                    int meta2 = blockstate2.getBlock().getMetaFromState(blockstate2);
                    Block block2 = blockstate2.getBlock();
                    if (block1 == block2 && meta2 == meta1)
                        continue;

//                    GlStateManager.enableCull();
                    if (updatedBlock.getBlock().getBlock() != Blocks.air && updatedBlock.getBlock().getBlock() != Blocks.barrier) {
                        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
                        float partialTicks = event.partialTicks;
                        Entity viewing_from = Minecraft.getMinecraft().getRenderViewEntity();

                        double x_fix = viewing_from.lastTickPosX + ((viewing_from.posX - viewing_from.lastTickPosX) * partialTicks);
                        double y_fix = viewing_from.lastTickPosY + ((viewing_from.posY - viewing_from.lastTickPosY) * partialTicks);
                        double z_fix = viewing_from.lastTickPosZ + ((viewing_from.posZ - viewing_from.lastTickPosZ) * partialTicks);

                        GlStateManager.pushMatrix();
                        GlStateManager.translate(-x_fix, -y_fix, -z_fix);
                        GlStateManager.disableLighting();
                        GlStateManager.enableAlpha();
//                        GlStateManager.disableDepth();
//                        GlStateManager.depthMask(false);
                        GlStateManager.enableBlend();

                        Tessellator tessellator = Tessellator.getInstance();
                        WorldRenderer vertexBuffer = tessellator.getWorldRenderer();
                        vertexBuffer.begin(7, DefaultVertexFormats.BLOCK);
                        BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
//                        GlStateManager.color(1.0f,1.0f,1.0f,0.1f);
                        blockrendererdispatcher.getBlockModelRenderer().renderModel(Minecraft.getMinecraft().theWorld,
                                blockrendererdispatcher.getBlockModelShapes().getModelForState(updatedBlock.getBlock()),
                                updatedBlock.getBlock(), updatedBlock.getPos(), vertexBuffer, false);
                        tessellator.draw();

                        GlStateManager.enableLighting();
                        GlStateManager.popMatrix();
                    } else {
                        RenderUtils.highlightBlock(updatedBlock.getPos(),
                                updatedBlock.getBlock().getBlock() == Blocks.air ? new Color(0x50FF00FF, true)
                                :  new Color(0x500000FF, true), event.partialTicks, true);
                    }
                }
            }
        }
    }

    @DGEventHandler(triggerOutOfSkyblock = true)
    public void onWorldLoad(DGTickEvent event) {
        if (flag && !setup) {
            setup = true;
            System.out.println(Minecraft.getMinecraft().theWorld);
            Minecraft.getMinecraft().thePlayer.setPosition(0, 70, 0);
            Minecraft.getMinecraft().thePlayer.inventory.mainInventory[0] = new ItemStack(Items.stick);
            DungeonContext fakeContext = new DungeonContext("TEST DG", Minecraft.getMinecraft().theWorld);
            DungeonsGuide.getDungeonsGuide().getDungeonFacade().setContext(fakeContext);
            DungeonsGuide.getDungeonsGuide().getSkyblockStatus().setForceIsOnDungeon(true);
            DungeonMapLayout dungeonMapLayout = new DungeonMapLayout(
                    new Dimension(16, 16),
                    5,
                    new Point(0,0),
                    new BlockPos(0,70,0)
            );
            DungeonRoomScaffoldParser scaffoldParser1 = new DungeonRoomScaffoldParser(dungeonMapLayout, fakeContext);
            fakeContext.setScaffoldParser(scaffoldParser1);

            List<Point> points = new ArrayList<>();

            for (int dy = 0; dy < 4; dy++) {
                for (int dx = 0; dx < 4; dx++) {
                    boolean isSet = ((shape>> (dy * 4 + dx)) & 0x1) != 0;
                    if (isSet) {
                        points.add(new Point(dx, dy));
                    }
                }
            }

            DungeonRoom dungeonRoom = new DungeonRoom(
                    Sets.newHashSet(points),
                    shape,
                    color,
                    new BlockPos(0, 70, 0),
                    new BlockPos(32 * xWid - 1, 70, 32 * zWid - 1),
                    fakeContext,
                    Collections.emptySet());


            fakeContext.getScaffoldParser().getDungeonRoomList().add(dungeonRoom);
            for (Point p : points) {
                fakeContext.getScaffoldParser().getRoomMap().put(p, dungeonRoom);
            }

        }
    }

    @DGEventHandler
    public void onWorldUnload(WorldEvent.Unload e) {
        if (flag) {
            EditingContext.endEditingSession();
            DungeonsGuide.getDungeonsGuide().getDungeonFacade().setContext(null);
            DungeonsGuide.getDungeonsGuide().getSkyblockStatus().setForceIsOnDungeon(false);
            blockUpdates = null;
            flag = false;
        }
    }

    public static class RoomSwitch extends AnnotatedImportOnlyWidget {
        @Bind(variableName = "uuid")
        public final BindableAttribute<String> uuid = new BindableAttribute<>(String.class);
        @Bind(variableName = "name")
        public final BindableAttribute<String> name = new BindableAttribute<>(String.class);
        @Bind(variableName = "roomColor")
        public final BindableAttribute<Integer> color = new BindableAttribute<>(Integer.class);
        @Bind(variableName = "shape")
        public final BindableAttribute<String> shape = new BindableAttribute<>(String.class);
        public RoomSwitch(DungeonRoomInfo dungeonRoomInfo) {
            super(new ResourceLocation("dungeonsguide:gui/features/roomedit/room.gui"));
            name.setValue(dungeonRoomInfo.getName());
            uuid.setValue(dungeonRoomInfo.getUuid().toString());
            StringBuilder builder = new StringBuilder();
            for (int dy = 0; dy < 4; dy++) {
                if (dy > 0)
                    builder.append("\n");
                for (int dx = 0; dx < 4; dx++) {
                    boolean isSet = ((dungeonRoomInfo.getShape() >> (dy * 4 + dx)) & 0x1) != 0;
                    builder.append(isSet ? "O" : " ");
                }
            }
            shape.setValue(builder.toString());

            int j = dungeonRoomInfo.getColor() & 255;

            int color;
            if (j / 4 == 0) {
                color = 0x00000000;
            } else {
                color = MapColor.mapColorArray[j / 4].getMapColor(j & 3);
            }

            this.color.setValue(color);
        }

        @On(functionName = "edit")
        public void edit() {
            // blahblah
        }

    }
}
