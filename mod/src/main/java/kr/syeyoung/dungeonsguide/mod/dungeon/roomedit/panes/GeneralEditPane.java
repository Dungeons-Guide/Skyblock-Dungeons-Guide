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

package kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.panes;


import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.gui.MPanel;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.gui.elements.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoomInfoRegistry;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.ProcessorFactory;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.world.UBlockState;
import kr.syeyoung.modapi.world.UTileEntity;
import net.kyori.adventure.nbt.*;
import net.minecraft.util.ChatComponentText;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.UUID;

public class GeneralEditPane extends MPanel {
    private final DungeonRoom dungeonRoom;

    private final MLabelAndElement uuid;
    private final MLabelAndElement name;
    private final MLabelAndElement secrets;

    private final MLabelAndElement shape;
    private final MLabelAndElement rotation;
    private final MLabelAndElement shape2;

    private MButton save;
    private final MButton end;
    private final MButton schematic;

    private final MLabelAndElement roomProcessor;

    public GeneralEditPane(final DungeonRoom dungeonRoom) {
        this.dungeonRoom = dungeonRoom;
        {
            MLabel la;
            uuid = new MLabelAndElement("Room UUID: ", la = new MLabel());
            la.setText(dungeonRoom.getDungeonRoomInfo().getUuid().toString());
            uuid.setBounds(new Rectangle(0,0,getBounds().width, 20));
            add(uuid);
        }
        {
            MTextField la = new MTextField() {
                @Override
                public void edit(String str) {
                    dungeonRoom.getDungeonRoomInfo().setName(str);
                }
            };
            name = new MLabelAndElement("Room Name: ", la);
            la.setText(dungeonRoom.getDungeonRoomInfo().getName());
            name.setBounds(new Rectangle(0,20,getBounds().width, 20));
            add(name);
        }
        {
            final MIntegerSelectionButton la = new MIntegerSelectionButton(dungeonRoom.getDungeonRoomInfo().getTotalSecrets());
            la.setOnUpdate(new Runnable() {
                @Override
                public void run() {
                    dungeonRoom.getDungeonRoomInfo().setTotalSecrets(la.getData());
                }
            });
            secrets = new MLabelAndElement("Room Secrets: ", la);
            secrets.setBounds(new Rectangle(0,40,getBounds().width, 20));
            add(secrets);
        }

        {
            MLabel la;
            shape = new MLabelAndElement("Room Shape: ", la = new MLabel());
            la.setText(dungeonRoom.getDungeonRoomInfo().getShape()+"");
            shape.setBounds(new Rectangle(0,60,getBounds().width, 20));
            add(shape);
        }

        {
            MLabel la;
            rotation = new MLabelAndElement("Found Room Rotation: ", la = new MLabel());
            la.setText(dungeonRoom.getRoomMatcher().getRotation()+"");
            rotation.setBounds(new Rectangle(0,80,getBounds().width, 20));
            add(rotation);
        }
        {
            MLabel la;
            shape2 = new MLabelAndElement("Found Room Shape: ", la = new MLabel());
            la.setText(dungeonRoom.getRoomBounds().getShape()+"");
            shape2.setBounds(new Rectangle(0,100,getBounds().width, 20));
            add(shape2);
        }
        {
            final MStringSelectionButton mStringSelectionButton = new MStringSelectionButton(new ArrayList<String>(ProcessorFactory.getProcessors()), dungeonRoom.getDungeonRoomInfo().getProcessorId());
            roomProcessor = new MLabelAndElement("Room Processor: ", mStringSelectionButton);
            roomProcessor.setBounds(new Rectangle(0,120,getBounds().width, 20));
            add(roomProcessor);

            mStringSelectionButton.setOnUpdate(new Runnable() {
                @Override
                public void run() {
                    dungeonRoom.getDungeonRoomInfo().setProcessorId(mStringSelectionButton.getSelected());
                    dungeonRoom.updateRoomProcessor();
                }
            });
        }
        {
            end = new MButton();
            end.setText("End Editing Session");
            end.setOnActionPerformed(new Runnable() {
                @Override
                public void run() {
                    EditingContext.endEditingSession();
                }
            });
            end.setBackgroundColor(Color.green);
            end.setBounds(new Rectangle(0,140,getBounds().width, 20));
            add(end);
        }
        {
            schematic = new MButton();
            schematic.setText("Save Schematic");
            schematic.setOnActionPerformed(new Runnable() {
                @Override
                public void run() {
                    try {
                        CompoundBinaryTag nbtTagCompound2 = createNBT();

                        File f=new File(Main.getConfigDir(), "schematics/"+
                                dungeonRoom.getDungeonRoomInfo().getName()+"-"+dungeonRoom.getDungeonRoomInfo().getUuid().toString()+"-"+ UUID.randomUUID()+".schematic");

                        FileOutputStream fos = new FileOutputStream(f);

                        try
                        {
                            BinaryTagIO.writer().writeNamed(new AbstractMap.SimpleEntry<>("Schematic", nbtTagCompound2), fos, BinaryTagIO.Compression.GZIP);
                        }
                        finally
                        {
                            fos.close();
                        }

//                        NBTTagCompound compound = nbtTagCompound2;
//                        int w = compound.getShort("Width");
//                        int l = compound.getShort("Length");
//                        if (dungeonRoom.getRoomMatcher().getRotation() % 2 == 1) {
//                            int temp = l;
//                            l = w;
//                            w = temp;
//                        }
////                        if (!dungeonRoom.getDungeonRoomInfo().hasSchematic())
//                        dungeonRoom.getDungeonRoomInfo().setSize(w,l,256);
//
//                        byte[] blocks = compound.getByteArray("Blocks");
//                        byte[] meta = compound.getByteArray("Data");
//                        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
//                        OffsetPoint offsetPoint = new OffsetPoint();
//                        for (int x = 1; x < compound.getShort("Width"); x++) {
//                            for (int y = 0; y < compound.getShort("Height"); y++) {
//                                for (int z = 1; z < compound.getShort("Length"); z++) {
//                                    int index = x + (y * compound.getShort("Length") + z) * compound.getShort("Width");
//                                    mpos.set(x+dungeonRoom.getRoomBounds().getMinX(),y,z+dungeonRoom.getRoomBounds().getMinZ());
//                                    offsetPoint.setPosInWorld(dungeonRoom, mpos);
//
//                                    Block b = Block.getBlockById(blocks[index] & 0xFF);
//                                    Optional<PropertyDirection> propertyDirection = b.getDefaultState().getPropertyNames().stream()
//                                            .filter(a -> a instanceof PropertyDirection)
//                                            .map(PropertyDirection.class::cast).findFirst();
//
//                                    if (!dungeonRoom.getRoomBounds().canAccessRelative(x,z)) {
//                                        continue;
//                                    }
//
//
//                                    IBlockState blockState = b.getStateFromMeta(meta[index] & 0xFF);
//                                    if (propertyDirection.isPresent()) {
//                                        EnumFacing enumFacing = blockState.getValue(propertyDirection.get());
//                                        if (!(enumFacing == EnumFacing.UP || enumFacing == EnumFacing.DOWN)) {
//                                            for (int i = 0; i < dungeonRoom.getRoomMatcher().getRotation(); i++)
//                                                enumFacing = enumFacing.rotateY();
//                                            blockState = blockState.withProperty(propertyDirection.get(), enumFacing);
//                                        }
//                                    }
//
//                                    dungeonRoom.getDungeonRoomInfo().setBlock(offsetPoint, blockState);
//                                }
//                            }
//                        }

                        ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §fSaved to "+f.getName()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
            schematic.setBackgroundColor(Color.orange);
            schematic.setBounds(new Rectangle(0,180,getBounds().width, 20));
            add(schematic);
        }
        {
            if (dungeonRoom.getDungeonRoomInfo().isRegistered()) return;
            save = new MButton();
            save.setText("Save RoomData");
            save.setOnActionPerformed(new Runnable() {
                @Override
                public void run() {
                    DungeonRoomInfoRegistry.register(dungeonRoom.getDungeonRoomInfo());
                    remove(save);
                }
            });
            save.setBackgroundColor(Color.green);
            save.setBounds(new Rectangle(0,10,getBounds().width, 20));
            add(save);
        }
    }

    @Override
    public void resize(int parentWidth, int parentHeight) {
        this.setBounds(new Rectangle(5,5,parentWidth-10,parentHeight-10));
    }

    @Override
    public void onBoundsUpdate() {
        if (save != null)
            save.setBounds(new Rectangle(0,160,getBounds().width, 20));
        end.setBounds(new Rectangle(1,140,getBounds().width-2, 20));
        if (schematic != null)
        schematic.setBounds(new Rectangle(0,180,getBounds().width, 20));
    }

    private CompoundBinaryTag createNBT() {
        CompoundBinaryTag.Builder compound =CompoundBinaryTag.builder();
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
                    UBlockState blockState = dungeonRoom.getRoomWorld().getBlockStateAt(pos);
                    boolean acc = dungeonRoom.getRoomBounds().canAccessRelative(x,z);
                    int id = blockState.getLegacyId();
                    blocks[index] = acc ? (byte) id : 0;
                    meta[index] = acc ? (byte) blockState.getLegacyMeta() : 0;
                    if ((extra[index] = (byte) ((acc ? id : 0) >> 8)) > 0) {
                        extraEx = true;
                    }

                    if (blockState.hasTileEntity()) {
                        UTileEntity tileEntity = dungeonRoom.getContext().getUworld().getTileEntityAt(pos);
                        try {
                            tileEntitiesList.add(tileEntity.serialize());
                        } catch (final Exception e) {
                            blocks[index] = (byte) 7;
                            meta[index] = 0;
                            extra[index] = 0;
                        }
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
}
