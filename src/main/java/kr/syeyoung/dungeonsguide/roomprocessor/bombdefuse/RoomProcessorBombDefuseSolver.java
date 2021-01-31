package kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.roomprocessor.GeneralRoomProcessor;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.BDChamber;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.BombDefuseChamberGenerator;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.DummyDefuseChamberProcessor;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.living.LivingEvent;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RoomProcessorBombDefuseSolver extends GeneralRoomProcessor {

    private List<ChamberSet> chambers = new ArrayList<ChamberSet>();
    private OffsetPointSet doors;

    private static final List<BombDefuseChamberGenerator> chamberGenerators = new ArrayList<BombDefuseChamberGenerator>();
    {

    }

    private boolean bugged = false;

    public RoomProcessorBombDefuseSolver(DungeonRoom dungeonRoom) {
        super(dungeonRoom);
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
                    set.getLeft().setProcessor(bdcg.createLeft(set.getLeft()));
                    set.getRight().setProcessor(bdcg.createLeft(set.getRight()));
                    break;
                }
            }
            if (set.getChamberGen() == null) {
                set.setChamberGen(null);
                set.getLeft().setProcessor(new DummyDefuseChamberProcessor());
                set.getRight().setProcessor(new DummyDefuseChamberProcessor());
            }
        }
    }

    public BDChamber buildChamber(OffsetPointSet ops, int level, boolean left) {
        return new BDChamber(getDungeonRoom(), ops, left, level, null);
    }


    public void communicate(NBTTagCompound compound) {
        if (bugged) return;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream w = new DataOutputStream(baos);
            CompressedStreamTools.write(compound, w);
            w.flush();
            byte[] bytes = baos.toByteArray();
            String str = Base64.encode(bytes);
            Minecraft.getMinecraft().thePlayer.sendChatMessage("/pc " +str);
        } catch (IOException e2) {
            e2.printStackTrace();
            e.sendDebugChat(new ChatComponentText("Failed to send Bomb Defuse Chat"));
        }
    }

    @Override
    public void chatReceived(IChatComponent component) {
        super.chatReceived(component);
        if (bugged) return;

        if (component.getFormattedText().contains("$DG-BD")) {
            try {
                String data = component.getFormattedText().substring(component.getFormattedText().indexOf("$DG-BD"));
                String actual = TextUtils.stripColor(data);
                byte[] data2 = Base64.decode(actual);
                NBTTagCompound compound = CompressedStreamTools.readCompressed(new ByteArrayInputStream(data2));

                for (ChamberSet ch:chambers) {
                    if (ch.getLeft() != null && ch.getLeft().getProcessor() != null)
                        ch.getLeft().getProcessor().onDataRecieve(compound);
                    if (ch.getRight() != null && ch.getRight().getProcessor() != null)
                        ch.getRight().getProcessor().onDataRecieve(compound);
                }
            } catch (Throwable t) {
                t.printStackTrace();
                e.sendDebugChat(new ChatComponentText("Failed to analyze Bomb Defuse Chat"));
            }
        }
    }


    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void drawScreen(float partialTicks) {
        super.drawScreen(partialTicks);

        if (bugged) return;

        for (ChamberSet ch:chambers) {
            if (ch.getLeft() != null && ch.getLeft().getProcessor() != null)
                ch.getLeft().getProcessor().drawScreen(partialTicks);
            if (ch.getRight() != null && ch.getRight().getProcessor() != null)
                ch.getRight().getProcessor().drawScreen(partialTicks);
        }
    }

    @Override
    public void drawWorld(float partialTicks) {
        super.drawWorld(partialTicks);
        if (bugged) return;

        for (ChamberSet ch:chambers) {
            if (ch.getLeft() != null && ch.getLeft().getProcessor() != null)
                ch.getLeft().getProcessor().drawWorld(partialTicks);
            if (ch.getRight() != null && ch.getRight().getProcessor() != null)
                ch.getRight().getProcessor().drawWorld(partialTicks);
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

        for (ChamberSet ch:chambers) {
            if (ch.getLeft() != null && ch.getLeft().getProcessor() != null)
                ch.getLeft().getProcessor().onPostGuiRender(event);
            if (ch.getRight() != null && ch.getRight().getProcessor() != null)
                ch.getRight().getProcessor().onPostGuiRender(event);
        }
    }

    @Override
    public void onEntitySpawn(LivingEvent.LivingUpdateEvent updateEvent) {
        super.onEntitySpawn(updateEvent);
        if (bugged) return;

        for (ChamberSet ch:chambers) {
            if (ch.getLeft() != null && ch.getLeft().getProcessor() != null)
                ch.getLeft().getProcessor().onEntitySpawn(updateEvent);
            if (ch.getRight() != null && ch.getRight().getProcessor() != null)
                ch.getRight().getProcessor().onEntitySpawn(updateEvent);
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
}
