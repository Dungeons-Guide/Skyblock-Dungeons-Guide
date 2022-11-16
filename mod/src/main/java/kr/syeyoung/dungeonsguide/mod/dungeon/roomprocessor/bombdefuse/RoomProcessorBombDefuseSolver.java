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

package kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse;


import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.mod.chat.ChatTransmitter;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.RoomProcessorGenerator;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers.BDChamber;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers.BombDefuseChamberGenerator;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers.DummyDefuseChamberProcessor;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers.arrow.ArrowProcessorMatcher;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers.bugged.ImpossibleMatcher;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers.color.ColorProcessorMatcher;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers.creeper.CreeperProcessorMatcher;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers.goldenpath.GoldenPathProcessorMatcher;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers.maze.MazeProcessorMatcher;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers.number.NumberProcessorMatcher;
import kr.syeyoung.dungeonsguide.mod.events.impl.KeyBindPressedEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.PlayerInteractEntityEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RoomProcessorBombDefuseSolver extends GeneralRoomProcessor {

    @Getter
    private final List<ChamberSet> chambers = new ArrayList<ChamberSet>();
    @Getter
    private OffsetPointSet doors;

    private static final List<BombDefuseChamberGenerator> chamberGenerators = new ArrayList<BombDefuseChamberGenerator>();
    {
        chamberGenerators.add(new ArrowProcessorMatcher());
        chamberGenerators.add(new ColorProcessorMatcher());
        chamberGenerators.add(new CreeperProcessorMatcher());
        chamberGenerators.add(new NumberProcessorMatcher());
        chamberGenerators.add(new GoldenPathProcessorMatcher());
        chamberGenerators.add(new MazeProcessorMatcher());
        chamberGenerators.add(new ImpossibleMatcher());
    }

    private boolean bugged = false;
    private boolean maze = false;
    private boolean impossible = false;

    public RoomProcessorBombDefuseSolver(DungeonRoom dungeonRoom) {
        super(dungeonRoom);
        if (!FeatureRegistry.SOLVER_BOMBDEFUSE.isEnabled()) {
            bugged = true;
            return;
        }
        chambers.add(new ChamberSet(
                buildChamber((OffsetPointSet) dungeonRoom.getDungeonRoomInfo().getProperties().get("L1"), 1, true),
                buildChamber((OffsetPointSet) dungeonRoom.getDungeonRoomInfo().getProperties().get("R1"), 1, true), null
        ));
        chambers.add(new ChamberSet(
                buildChamber((OffsetPointSet) dungeonRoom.getDungeonRoomInfo().getProperties().get("L2"), 2, true),
                buildChamber((OffsetPointSet) dungeonRoom.getDungeonRoomInfo().getProperties().get("R2"), 2, true), null
        ));
        chambers.add(new ChamberSet(
                buildChamber((OffsetPointSet) dungeonRoom.getDungeonRoomInfo().getProperties().get("L3"), 3, true),
                buildChamber((OffsetPointSet) dungeonRoom.getDungeonRoomInfo().getProperties().get("R3"), 3, true), null
        ));
        chambers.add(new ChamberSet(
                buildChamber((OffsetPointSet) dungeonRoom.getDungeonRoomInfo().getProperties().get("L4"), 4, true),
                buildChamber((OffsetPointSet) dungeonRoom.getDungeonRoomInfo().getProperties().get("R4"), 4, true), null
        ));
        doors = (OffsetPointSet) dungeonRoom.getDungeonRoomInfo().getProperties().get("Door");
        if (doors == null) {bugged = true; return;}
        for (ChamberSet set:chambers) {
            if (set.getLeft().getChamberBlocks() == null) {
                bugged = true;
                return;
            }
            if (set.getRight().getChamberBlocks() == null) {
                bugged = true;
                return;
            }
        }

        for (ChamberSet set:chambers) {
            if (set.getLeft().getChamberBlocks() == null) {
                bugged = true;
                return;
            }
            if (set.getRight().getChamberBlocks() == null) {
                bugged = true;
                return;
            }
        }

        for (ChamberSet set:chambers) {
            for (BombDefuseChamberGenerator bdcg:chamberGenerators) {
                if (bdcg.match(set.getLeft(), set.getRight())) {
                    set.setChamberGen(bdcg);
                    set.getLeft().setProcessor(bdcg.createLeft(set.getLeft(), this));
                    set.getRight().setProcessor(bdcg.createRight(set.getRight(), this));
                    if (bdcg instanceof ImpossibleMatcher) impossible=true;
                    if (bdcg instanceof MazeProcessorMatcher) maze = true;
                    break;
                }
            }
            if (set.getChamberGen() == null) {
                set.setChamberGen(null);
                set.getLeft().setProcessor(new DummyDefuseChamberProcessor(this, set.getLeft()));
                set.getRight().setProcessor(new DummyDefuseChamberProcessor(this, set.getRight()));
            }
        }

        OffsetPoint warning1 = (OffsetPoint) dungeonRoom.getDungeonRoomInfo().getProperties().get("Warning");
        if (warning1 != null) warning = warning1.getBlockPos(dungeonRoom);
    }

    public BDChamber buildChamber(OffsetPointSet ops, int level, boolean left) {
        return new BDChamber(getDungeonRoom(), ops, left, level, null);
    }
    BlockPos warning;


    public void communicate(NBTTagCompound compound) {
        if (bugged) return;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream w = new DataOutputStream(baos);
            CompressedStreamTools.writeCompressed(compound, w);
            w.flush();
            byte[] bytes = baos.toByteArray();
            String str = Base64.encodeBase64String(bytes);
            Minecraft.getMinecraft().thePlayer.sendChatMessage("/pc $DG-BD " +str);

            for (ChamberSet ch:chambers) {
                if (ch.getLeft() != null && ch.getLeft().getProcessor() != null)
                    ch.getLeft().getProcessor().onDataRecieve(compound);
                if (ch.getRight() != null && ch.getRight().getProcessor() != null)
                    ch.getRight().getProcessor().onDataRecieve(compound);
            }
        } catch (IOException e2) {
            e2.printStackTrace();
            ChatTransmitter.sendDebugChat(new ChatComponentText("Failed to send Bomb Defuse Chat"));
        }
    }

    @Override
    public void chatReceived(IChatComponent component) {
        super.chatReceived(component);
        if (bugged) return;
        for (ChamberSet ch:chambers) {
            if (ch.getLeft() != null && ch.getLeft().getProcessor() != null)
                ch.getLeft().getProcessor().chatReceived(component);
            if (ch.getRight() != null && ch.getRight().getProcessor() != null)
                ch.getRight().getProcessor().chatReceived(component);
        }

        if (component.getFormattedText().contains("$DG-BD ")) {
            try {
                String data = component.getFormattedText().substring(component.getFormattedText().indexOf("$DG-BD"));
                String actual = TextUtils.stripColor(data).trim().split(" ")[1];
                byte[] data2 = Base64.decodeBase64(actual);
                NBTTagCompound compound = CompressedStreamTools.readCompressed(new ByteArrayInputStream(data2));

                for (ChamberSet ch:chambers) {
                    if (ch.getLeft() != null && ch.getLeft().getProcessor() != null)
                        ch.getLeft().getProcessor().onDataRecieve(compound);
                    if (ch.getRight() != null && ch.getRight().getProcessor() != null)
                        ch.getRight().getProcessor().onDataRecieve(compound);
                }
            } catch (Throwable t) {
                t.printStackTrace();
                ChatTransmitter.sendDebugChat(new ChatComponentText("Failed to analyze Bomb Defuse Chat"));
            }
        }
    }


    @Override
    public void tick() {
        super.tick();
        if (bugged) return;
        BlockPos player = Minecraft.getMinecraft().thePlayer.getPosition();
        OffsetPoint offsetPoint = new OffsetPoint(getDungeonRoom(), new BlockPos(player.getX(), 68, player.getZ()));
        for (ChamberSet ch:chambers) {
            if (ch.getLeft() != null && ch.getLeft().getProcessor() != null) {
                if (ch.getLeft().getChamberBlocks().getOffsetPointList().contains(offsetPoint)) {
                    ch.getLeft().getProcessor().tick();
                }
            }
            if (ch.getRight() != null && ch.getRight().getProcessor() != null) {
                if (ch.getRight().getChamberBlocks().getOffsetPointList().contains(offsetPoint)) {
                    ch.getRight().getProcessor().tick();
                }
            }
        }
    }


    @Override
    public void drawScreen(float partialTicks) {
        super.drawScreen(partialTicks);
        if (bugged) return;
        BlockPos player = Minecraft.getMinecraft().thePlayer.getPosition();
        OffsetPoint offsetPoint = new OffsetPoint(getDungeonRoom(), new BlockPos(player.getX(), 68, player.getZ()));
        if (FeatureRegistry.DEBUG.isEnabled()) {
            for (ChamberSet ch : chambers) {
                if (ch.getChamberGen() == null) continue;
                if (ch.getLeft() != null && ch.getLeft().getProcessor() != null) {
                    if (ch.getLeft().getChamberBlocks().getOffsetPointList().contains(offsetPoint)) {
                        ch.getLeft().getProcessor().drawScreen(partialTicks);

                        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
                        String str = "Current: " + ch.getChamberGen().getName() + " Specific: " + ch.getLeft().getProcessor().getName();
                        fr.drawString(str, 0, 0, 0xFFFFFFFF);
                    }
                }
                if (ch.getRight() != null && ch.getRight().getProcessor() != null) {
                    if (ch.getRight().getChamberBlocks().getOffsetPointList().contains(offsetPoint)) {
                        ch.getRight().getProcessor().drawScreen(partialTicks);

                        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
                        if (ch.getChamberGen() == null || ch.getRight().getProcessor() == null) continue;
                        String str = "Current: " + ch.getChamberGen().getName() + " Specific: " + ch.getRight().getProcessor().getName();
                        fr.drawString(str, 0, 0, 0xFFFFFFFF);
                    }
                }
            }
        }
    }

    @Override
    public void drawWorld(float partialTicks) {
        super.drawWorld(partialTicks);
        if (bugged) return;

        OffsetPoint offsetPoint = new OffsetPoint(getDungeonRoom(), new BlockPos((int)Minecraft.getMinecraft().thePlayer.posX, 68, (int)Minecraft.getMinecraft().thePlayer.posZ));
        boolean found = false;
        for (ChamberSet ch:chambers) {
            if (ch.getLeft() != null && ch.getLeft().getProcessor() != null) {
                if (ch.getLeft().getChamberBlocks().getOffsetPointList().contains(offsetPoint)) {
                    found = true;
                    ch.getLeft().getProcessor().drawWorld(partialTicks);
                }
            }
            if (ch.getRight() != null && ch.getRight().getProcessor() != null) {
                if (ch.getRight().getChamberBlocks().getOffsetPointList().contains(offsetPoint)) {
                    found = true;
                    ch.getRight().getProcessor().drawWorld(partialTicks);
                }
            }
        }

        if ((maze || impossible) && warning != null && !found) {
            if (impossible) {
                RenderUtils.drawTextAtWorld("Warning: This Bomb Defuse is bugged and Impossible" , warning.getX()+ 0.5f, warning.getY(), warning.getZ()+ 0.5f, 0xFF00FF00, 0.03F, false, false, partialTicks);
            } else {
                RenderUtils.drawTextAtWorld("Warning: This Bomb Defuse must be done with 2 people (maze)" , warning.getX()+ 0.5f, warning.getY(), warning.getZ()+ 0.5f, 0xFF00FF00, 0.03F, false, false, partialTicks);
            }
        }
        if (warning != null && !found) {
            for (int i = 0; i < 4; i++) {
                BombDefuseChamberGenerator bdcg = chambers.get(i).getChamberGen();
                RenderUtils.drawTextAtWorld((i + 1) + ". " + (bdcg == null ? "null" : bdcg.getName()), warning.getX() + 0.5f, warning.getY() - ((i + 1) * 0.3f), warning.getZ() + 0.5f, 0xFF00FF00, 0.03F, false, false, partialTicks);
            }
        }
    }

    @Override
    public void actionbarReceived(IChatComponent chat) {
        super.actionbarReceived(chat);
        if (bugged) return;

        for (ChamberSet ch:chambers) {
            if (ch.getLeft() != null && ch.getLeft().getProcessor() != null)
                ch.getLeft().getProcessor().actionbarReceived(chat);
            if (ch.getRight() != null && ch.getRight().getProcessor() != null)
                ch.getRight().getProcessor().actionbarReceived(chat);
        }
    }

    @Override
    public void onPostGuiRender(GuiScreenEvent.DrawScreenEvent.Post event) {
        super.onPostGuiRender(event);
        if (bugged) return;

        BlockPos player = Minecraft.getMinecraft().thePlayer.getPosition();
        OffsetPoint offsetPoint = new OffsetPoint(getDungeonRoom(), new BlockPos(player.getX(), 68, player.getZ()));
        for (ChamberSet ch:chambers) {
            if (ch.getLeft() != null && ch.getLeft().getProcessor() != null) {
                if (ch.getLeft().getChamberBlocks().getOffsetPointList().contains(offsetPoint)) {
                    ch.getLeft().getProcessor().onPostGuiRender(event);
                }
            }
            if (ch.getRight() != null && ch.getRight().getProcessor() != null) {
                if (ch.getRight().getChamberBlocks().getOffsetPointList().contains(offsetPoint)) {
                    ch.getRight().getProcessor().onPostGuiRender(event);
                }
            }
        }
    }

    @Override
    public void onEntityUpdate(LivingEvent.LivingUpdateEvent updateEvent) {
        super.onEntityUpdate(updateEvent);
        if (bugged) return;

        BlockPos player = Minecraft.getMinecraft().thePlayer.getPosition();
        OffsetPoint offsetPoint = new OffsetPoint(getDungeonRoom(), new BlockPos(player.getX(), 68, player.getZ()));
        for (ChamberSet ch:chambers) {
            if (ch.getLeft() != null && ch.getLeft().getProcessor() != null) {
                if (ch.getLeft().getChamberBlocks().getOffsetPointList().contains(offsetPoint)) {
                    ch.getLeft().getProcessor().onEntityUpdate(updateEvent);
                }
            }
            if (ch.getRight() != null && ch.getRight().getProcessor() != null) {
//                if (ch.getRight().getChamberBlocks().getOffsetPointList().contains(offsetPoint)) {
                    ch.getRight().getProcessor().onEntityUpdate(updateEvent);
//                }
            }
        }
    }

    @Override
    public void onKeybindPress(KeyBindPressedEvent keyInputEvent) {
        super.onKeybindPress(keyInputEvent);
        if (bugged) return;

        BlockPos player = Minecraft.getMinecraft().thePlayer.getPosition();
        OffsetPoint offsetPoint = new OffsetPoint(getDungeonRoom(), new BlockPos(player.getX(), 68, player.getZ()));
        for (ChamberSet ch:chambers) {
            if (ch.getLeft() != null && ch.getLeft().getProcessor() != null) {
                if (ch.getLeft().getChamberBlocks().getOffsetPointList().contains(offsetPoint)) {
                    ch.getLeft().getProcessor().onKeybindPress(keyInputEvent);
                }
            }
            if (ch.getRight() != null && ch.getRight().getProcessor() != null) {
                if (ch.getRight().getChamberBlocks().getOffsetPointList().contains(offsetPoint)) {
                    ch.getRight().getProcessor().onKeybindPress(keyInputEvent);
                }
            }
        }
    }

    @Override
    public void onInteract(PlayerInteractEntityEvent event) {
        super.onInteract(event);
        if (bugged) return;

        BlockPos player = Minecraft.getMinecraft().thePlayer.getPosition();
        OffsetPoint offsetPoint = new OffsetPoint(getDungeonRoom(), new BlockPos(player.getX(), 68, player.getZ()));
        for (ChamberSet ch:chambers) {
            if (ch.getLeft() != null && ch.getLeft().getProcessor() != null) {
                if (ch.getLeft().getChamberBlocks().getOffsetPointList().contains(offsetPoint)) {
                    ch.getLeft().getProcessor().onInteract(event);
                }
            }
            if (ch.getRight() != null && ch.getRight().getProcessor() != null) {
                if (ch.getRight().getChamberBlocks().getOffsetPointList().contains(offsetPoint)) {
                    ch.getRight().getProcessor().onInteract(event);
                }
            }
        }
    }

    @Override
    public void onInteractBlock(PlayerInteractEvent event) {
        super.onInteractBlock(event);
        if (bugged) return;

        BlockPos player = Minecraft.getMinecraft().thePlayer.getPosition();
        OffsetPoint offsetPoint = new OffsetPoint(getDungeonRoom(), new BlockPos(player.getX(), 68, player.getZ()));
        for (ChamberSet ch:chambers) {
            if (ch.getLeft() != null && ch.getLeft().getProcessor() != null) {
                if (ch.getLeft().getChamberBlocks().getOffsetPointList().contains(offsetPoint)) {
                    ch.getLeft().getProcessor().onInteractBlock(event);
                }
            }
            if (ch.getRight() != null && ch.getRight().getProcessor() != null) {
                if (ch.getRight().getChamberBlocks().getOffsetPointList().contains(offsetPoint)) {
                    ch.getRight().getProcessor().onInteractBlock(event);
                }
            }
        }
    }

    @Override public boolean readGlobalChat() { return true; }

    @Data
    @AllArgsConstructor
    public static class ChamberSet {
        private BDChamber left;
        private BDChamber right;
        private BombDefuseChamberGenerator chamberGen;
    }


    public static class Generator implements RoomProcessorGenerator<RoomProcessorBombDefuseSolver> {
        @Override
        public RoomProcessorBombDefuseSolver createNew(DungeonRoom dungeonRoom) {
            RoomProcessorBombDefuseSolver defaultRoomProcessor = new RoomProcessorBombDefuseSolver(dungeonRoom);
            return defaultRoomProcessor;
        }
    }
}
