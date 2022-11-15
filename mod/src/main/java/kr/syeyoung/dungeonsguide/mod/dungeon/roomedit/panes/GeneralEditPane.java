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

<<<<<<<< HEAD:mod/src/main/java/kr/syeyoung/dungeonsguide/roomedit/panes/GeneralEditPane.java
import kr.syeyoung.dungeonsguide.gui.elements.*;
import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoomInfoRegistry;
import kr.syeyoung.dungeonsguide.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.gui.elements.*;
import kr.syeyoung.dungeonsguide.roomprocessor.ProcessorFactory;
========
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.Main;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoomInfoRegistry;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.ProcessorFactory;
import kr.syeyoung.dungeonsguide.mod.gui.MPanel;
import kr.syeyoung.dungeonsguide.mod.gui.elements.*;
>>>>>>>> origin/breaking-changes-just-working-im-not-putting-all-of-these-into-3.0-but-for-the-sake-of-beta-release-this-thing-exists:mod/src/main/java/kr/syeyoung/dungeonsguide/mod/dungeon/roomedit/panes/GeneralEditPane.java
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

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
            la.setText(dungeonRoom.getShape()+"");
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
                        NBTTagCompound nbtTagCompound2 = createNBT();

                        File f=new File(Main.getConfigDir(), "schematics/"+
                                dungeonRoom.getDungeonRoomInfo().getName()+"-"+dungeonRoom.getDungeonRoomInfo().getUuid().toString()+"-"+ UUID.randomUUID()+".schematic");

                        Method method = null;
                        try {
                            method = NBTTagCompound.class.getDeclaredMethod("write", DataOutput.class);
                            method.setAccessible(true);
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                            return;
                        }
                        FileOutputStream fos = new FileOutputStream(f);
                        DataOutputStream dataoutputstream = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(fos)));

                        try
                        {
                            dataoutputstream.writeByte(nbtTagCompound2.getId());

                            dataoutputstream.writeUTF("Schematic");
                            method.invoke(nbtTagCompound2, dataoutputstream);
                        }
                        finally
                        {
                            dataoutputstream.close();
                        }
                        ChatTransmitter.addToQueue(new ChatComponentText("§eDungeons Guide §7:: §fSaved to "+f.getName()));
                    } catch (Throwable e) {
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

    private NBTTagCompound createNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setShort("Width", (short) (dungeonRoom.getMax().getX() - dungeonRoom.getMin().getX() + 1));
        compound.setShort("Height", (short) 255);
        compound.setShort("Length", (short) (dungeonRoom.getMax().getZ() - dungeonRoom.getMin().getZ() + 1));
        int size =compound.getShort("Width") * compound.getShort("Height") * compound.getShort("Length");

        byte[] blocks = new byte[size];
        byte[] meta = new byte[size];
        byte[] extra = new byte[size];
        byte[] extranibble = new byte[(int) Math.ceil(size / 2.0)];

        boolean extraEx = false;
        NBTTagList tileEntitiesList = new NBTTagList();
        for (int x = 0; x < compound.getShort("Width"); x++) {
            for (int y = 0; y <  compound.getShort("Height"); y++) {
                for (int z = 0; z < compound.getShort("Length"); z++) {
                    int index = x + (y * compound.getShort("Length") + z) * compound.getShort("Width");
                    BlockPos pos = dungeonRoom.getRelativeBlockPosAt(x,y - 70,z);
                    IBlockState blockState = DungeonsGuide.getDungeonsGuide().getBlockCache().getBlockState(pos);
                    boolean acc = dungeonRoom.canAccessRelative(x,z);
                    int id = Block.getIdFromBlock(blockState.getBlock());
                    blocks[index] = acc ? (byte) id : 0;
                    meta[index] = acc ? (byte) blockState.getBlock().getMetaFromState(blockState) : 0;
                    if ((extra[index] = (byte) ((acc ? id : 0) >> 8)) > 0) {
                        extraEx = true;
                    }

                    if (blockState.getBlock().hasTileEntity(blockState)) {
                        TileEntity tileEntity = dungeonRoom.getContext().getWorld().getTileEntity(pos);
                        try {
                            final NBTTagCompound tileEntityCompound = new NBTTagCompound();
                            tileEntity.writeToNBT(tileEntityCompound);
                            tileEntitiesList.appendTag(tileEntityCompound);
                        } catch (final Exception e) {
                            final BlockPos tePos = tileEntity.getPos();

                            blocks[index] = (byte) 7;
                            meta[index] = 0;
                            extra[index] = 0;
                        }
                    }
                }
            }
        }
        for (int i = 0; i < extranibble.length; i++) {
            if (i * 2 + 1 < extra.length) {
                extranibble[i] = (byte) ((extra[i * 2 + 0] << 4) | extra[i * 2 + 1]);
            } else {
                extranibble[i] = (byte) (extra[i * 2 + 0] << 4);
            }
        }


        compound.setByteArray("Blocks", blocks);
        compound.setByteArray("Data", meta);
        compound.setString("Materials", "Alpha");
        if (extraEx) {
            compound.setByteArray("AddBlocks", extranibble);
        }
        compound.setTag("Entities", new NBTTagList());
        compound.setTag("TileEntities", tileEntitiesList);

        return compound;
    }
}
