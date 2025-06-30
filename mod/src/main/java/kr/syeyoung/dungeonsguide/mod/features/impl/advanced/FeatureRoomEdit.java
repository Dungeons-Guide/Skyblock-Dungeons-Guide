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
import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.config.types.TCKeybind;
import kr.syeyoung.dungeonsguide.mod.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.DungeonRoomInfo;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.map.DungeonMapLayout;
import kr.syeyoung.dungeonsguide.mod.dungeon.map.DungeonRoomScaffoldParser;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoomInfoRegistry;
import kr.syeyoung.dungeonsguide.mod.dungeon.world.ArrayBackedBlockMap;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.KeyBindPressedEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCollectDungeonRooms;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.GuiScreenAdapter;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.gui.xml.data.WidgetList;
import kr.syeyoung.dungeonsguide.mod.utils.DungeonServerLaunchUtils;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.EnumFacing;
import kr.syeyoung.modapi.data.ResourceIdentifier;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.event.events.ClientTickEvent;
import kr.syeyoung.modapi.event.events.WorldUnloadEvent;
import kr.syeyoung.modapi.world.BlockType;
import kr.syeyoung.modapi.world.IBlockRegistry;
import kr.syeyoung.modapi.world.UBlockState;
import lombok.Getter;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.apache.commons.io.IOUtils;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;
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

        OffsetPoint offsetPoint = new OffsetPoint(dungeonRoom, new VectorI3D(0,0,0));

        CompoundBinaryTag compound = schematic;
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
        VectorI3D mpos = new VectorI3D(0,0,0);
        for (int x = 1; x < compound.getShort("Width"); x++) {
            for (int y = 0; y < compound.getShort("Height"); y++) {
                for (int z = 1; z < compound.getShort("Length"); z++) {
                    int index = x + (y * compound.getShort("Length") + z) * compound.getShort("Width");
                    mpos.x = x;mpos.y = y;mpos.z = z;
                    offsetPoint.setPosInWorld(dungeonRoom, mpos);

                    UBlockState blockState = ModAPI.getAPI().getBlockRegistry().fromOldId(((blocks[index] & 0xFF) << 4) | (meta[index] &0xF));

                    if (!dungeonRoom.getRoomBounds().canAccessRelative(x,z)) {
                        continue;
                    }

                    EnumFacing enumFacing = blockState.getAnyBlockFacingIfItExists();

                    if (enumFacing != null) {
                        if (!(enumFacing == EnumFacing.UP || enumFacing == EnumFacing.DOWN)) {
                            for (int i = 0; i < dungeonRoom.getRoomMatcher().getRotation(); i++)
                                enumFacing = enumFacing.rotateY();
                            blockState = blockState.withFacing(enumFacing);
                        }
                    }

                    if ((blocks[index] & 0xFF) != 0 || !ignoreAir)
                        info.setBlock(offsetPoint, blockState);
                }
            }
        }
    }

    private void load(DungeonRoomInfo dungeonRoomInfo) {
        if (!dungeonRoomInfo.hasSchematic()) throw new IllegalArgumentException("HAS NO SCHEMATIC!");

        blockUpdates = new ArrayList<>();
        shape = dungeonRoomInfo.getShape();
        color = dungeonRoomInfo.getColor();

        schematic = null;

        DungeonServerLaunchUtils.launchDungeonServerAndJoin(dungeonRoomInfo, FeatureRegistry.SECRET_PRECALC_LIST.getSelectedPreset());

        xWid = (dungeonRoomInfo.getWidth() + 5) / 32;
        zWid = (dungeonRoomInfo.getLength() + 5) / 32;

        FeatureRoomEdit.this.flag = true;
        FeatureRoomEdit.this.setup = false;
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

        blockUpdates = new ArrayList<>();
        IBlockRegistry registry = ModAPI.getAPI().getBlockRegistry();
        int minX = jsonObject.get("minX").getAsInt(), minZ = jsonObject.get("minZ").getAsInt();
        for (JsonElement updates : jsonObject.get("blockupdates").getAsJsonArray()) {
            List<FeatureCollectDungeonRooms.RoomInfo.BlockUpdate.BlockUpdateData> list = new ArrayList<>();
            for (JsonElement updatedBlocks : updates.getAsJsonObject().get("updatedBlocks").getAsJsonArray()) {
                JsonArray pos = updatedBlocks.getAsJsonArray();
                VectorI3D bPos = new VectorI3D(pos.get(0).getAsInt()-minX, pos.get(1).getAsInt(), pos.get(2).getAsInt()-minZ);
                String[] block = pos.get(3).getAsString().split(":");

                FeatureCollectDungeonRooms.RoomInfo.BlockUpdate.BlockUpdateData data = new FeatureCollectDungeonRooms.RoomInfo.BlockUpdate.BlockUpdateData(bPos,
                        registry.fromOldId((Integer.parseInt(block[0]) << 4) | Integer.parseInt(block[1])));
                list.add(data);
            }
            long time = updates.getAsJsonObject().get("time").getAsLong();

            blockUpdates.add(new FeatureCollectDungeonRooms.RoomInfo.BlockUpdate(list, time));
        }

        shape = jsonObject.get("shape").getAsShort();
        color = jsonObject.get("color").getAsByte();

        CompoundBinaryTag compound;
        try {
            compound = BinaryTagIO.reader().readNamed(new ByteArrayInputStream(Base64.getDecoder().decode(
                    jsonObject.get("schematic").getAsString()
            )), BinaryTagIO.Compression.GZIP).getValue();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        schematic = compound;

        ArrayBackedBlockMap blockMap = SchematicLoader.loadSchematicWithShape(compound, shape);
        ModAPI.getAPI().getFakeServerUtils().launchFakeServerAndJoin(blockMap);


        xWid = (compound.getShort("Width") + 5) / 32;
        zWid = (compound.getShort("Length") + 5) / 32;

        FeatureRoomEdit.this.flag = true;
        FeatureRoomEdit.this.setup = false;
    }

    private void loadSchm(File f) {
        this.f = f;

        blockUpdates = new ArrayList<>();


        CompoundBinaryTag compound;
        try (FileInputStream fis = new FileInputStream(f)){
            compound = BinaryTagIO.reader().readNamed(fis, BinaryTagIO.Compression.GZIP).getValue();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        schematic = compound;


        ArrayBackedBlockMap blockMap = SchematicLoader.loadSchematic(compound);
        ModAPI.getAPI().getFakeServerUtils().launchFakeServerAndJoin(blockMap);


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
            super(new ResourceIdentifier("dungeonsguide:gui/features/roomedit/roomconfiguration.gui"));


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

        @On(functionName = "loadschematic")
        public  void loadSchematic() {
            Frame parent = new Frame();
            FileDialog dialog = new FileDialog(parent, "Choose a Schematic file", FileDialog.LOAD);
            dialog.setDirectory(Main.getConfigDir().getAbsolutePath());

            dialog.setFilenameFilter((dir, name) -> name.endsWith(".schematic")); //osx
            dialog.setFile("*.schematic"); // windows

            dialog.setVisible(true);

            File[] chosen = dialog.getFiles();

            parent.dispose();
            dialog.dispose();
            if (chosen.length == 0) return;
            File f = chosen[0];
            try {
                loadSchm(f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        @On(functionName = "loaddgrun")
        public void loadDGRun() {


            Frame parent = new Frame();
            FileDialog dialog = new FileDialog(parent, "Choose a DG Run file", FileDialog.LOAD);
            dialog.setDirectory(Main.getConfigDir().getAbsolutePath());

            dialog.setFilenameFilter((dir, name) -> name.endsWith(".dgrun")); //osx
            dialog.setFile("*.dgrun"); // windows

            dialog.setVisible(true);

            File[] chosen = dialog.getFiles();

            parent.dispose();
            dialog.dispose();
            if (chosen.length == 0) return;
            File f = chosen[0];
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
    private CompoundBinaryTag schematic;

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
                    if (ModAPI.getAPI().getPlayer().getPositionVector().distanceSq(updatedBlock.getPos()) > 100)
                        RenderUtils.highlightBlock(updatedBlock.getPos(), new Color(0x33FFFF00, true), event.partialTicks, false);
                    UBlockState blockstate1 = updatedBlock.getBlock();
                    UBlockState blockstate2 = ModAPI.getAPI().getWorld().getBlockStateAt(updatedBlock.getPos());

                    if (blockstate1 == blockstate2)
                        continue;

//                    GlStateManager.enableCull();
                    if (!updatedBlock.getBlock().isOf(BlockType.AIR, BlockType.BARRIER)) {
                        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
                        float partialTicks = event.partialTicks;

                        RenderUtils.pushAndTranslateAccordingToRenderViewEntity(partialTicks);

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
                                blockrendererdispatcher.getBlockModelShapes().getModelForState((IBlockState) updatedBlock.getBlock().getIBlockState()),
                                ((IBlockState)updatedBlock.getBlock().getIBlockState()), new BlockPos(updatedBlock.getPos().x, updatedBlock.getPos().y, updatedBlock.getPos().z), vertexBuffer, false);
                        tessellator.draw();

                        GlStateManager.enableLighting();
                        GlStateManager.popMatrix();
                    } else {
                        RenderUtils.highlightBlock(updatedBlock.getPos(),
                                updatedBlock.getBlock().isOf(BlockType.AIR) ? new Color(0x50FF00FF, true)
                                :  new Color(0x500000FF, true), event.partialTicks, true);
                    }
                }
            }
        }
    }

    @DGEventHandler(triggerOutOfSkyblock = true)
    public void onWorldLoad(ClientTickEvent event) {
        if (flag && !setup) {
            setup = true;
            System.out.println(Minecraft.getMinecraft().theWorld);
            Minecraft.getMinecraft().thePlayer.setPosition(0, 70, 0);
            Minecraft.getMinecraft().thePlayer.inventory.mainInventory[0] = new ItemStack(Items.stick);

            DungeonContext fakeContext = new DungeonContext("TEST DG", ModAPI.getAPI().getWorld());
            DungeonsGuide.getDungeonsGuide().getDungeonFacade().setContext(fakeContext);
            DungeonsGuide.getDungeonsGuide().getSkyblockStatus().setForceIsOnDungeon2(true);
            DungeonMapLayout dungeonMapLayout = new DungeonMapLayout(
                    new Dimension(16, 16),
                    5,
                    new Point(0,0),
                    new VectorI3D(0,70,0)
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
                    new VectorI3D(0, 70, 0),
                    new VectorI3D(32 * xWid - 1, 70, 32 * zWid - 1),
                    fakeContext,
                    Collections.emptySet());

            fakeContext.getScaffoldParser().insertRoom(dungeonRoom);
        }
    }

    @DGEventHandler
    public void onWorldUnload(WorldUnloadEvent e) {
        if (flag) {
            EditingContext.endEditingSession();
            DungeonsGuide.getDungeonsGuide().getDungeonFacade().setContext(null);
            DungeonsGuide.getDungeonsGuide().getSkyblockStatus().setForceIsOnDungeon2(false);
            blockUpdates = null;
            flag = false;
        }
    }

    public class RoomSwitch extends AnnotatedImportOnlyWidget {
        @Bind(variableName = "uuid")
        public final BindableAttribute<String> uuid = new BindableAttribute<>(String.class);
        @Bind(variableName = "name")
        public final BindableAttribute<String> name = new BindableAttribute<>(String.class);
        @Bind(variableName = "roomColor")
        public final BindableAttribute<Integer> color = new BindableAttribute<>(Integer.class);
        @Bind(variableName = "shape")
        public final BindableAttribute<String> shape = new BindableAttribute<>(String.class);

        private DungeonRoomInfo info;
        public RoomSwitch(DungeonRoomInfo dungeonRoomInfo) {
            super(new ResourceIdentifier("dungeonsguide:gui/features/roomedit/room.gui"));
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
                color = ModAPI.getAPI().getMapUtils().getRGBColor(j);
            }

            this.color.setValue(color);

            this.info = dungeonRoomInfo  ;
        }

        @On(functionName = "edit")
        public void edit() {

            FeatureRoomEdit.this.load(info);
            // blahblah
        }

    }
}
