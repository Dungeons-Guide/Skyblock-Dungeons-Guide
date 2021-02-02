package kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.color;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import kr.syeyoung.dungeonsguide.Keybinds;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.RoomProcessorBombDefuseSolver;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.BDChamber;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.GeneralDefuseChamberProcessor;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ColorRightProcessor extends GeneralDefuseChamberProcessor {
    public ColorRightProcessor(RoomProcessorBombDefuseSolver solver, BDChamber chamber) {
        super(solver, chamber);
        center = chamber.getBlockPos(4,4,4);
        b1 = chamber.getBlockPos(1,3,3);
        b2 = chamber.getBlockPos(1,3,2);
        b3 = chamber.getBlockPos(1,3,1);
    }

    private BlockPos center;

    private BlockPos b1, b2, b3;
    private byte b1b = 0, b2b = 0, b3b = 0, c1b, c2b, c3b;
    private int answer = -1;

    @Override
    public String getName() {
        return "colorRight";
    }

    @Override
    public void tick() {
        super.tick();
        c1b = match(getChamber().getEntityAt(EntityArmorStand.class,b1.add(0, -1, 0)));
        c2b = match(getChamber().getEntityAt(EntityArmorStand.class,b2.add(0, -1, 0)));
        c3b = match(getChamber().getEntityAt(EntityArmorStand.class,b3.add(0, -1, 0)));
    }

    @Override
    public void drawWorld(float partialTicks) {
        super.drawWorld(partialTicks);
        RenderUtils.drawTextAtWorld(answer == -1 ? "Press "+ Keyboard.getKeyName(Keybinds.sendBombdefuse.getKeyCode())+" to request solution" : "" , center.getX()+ 0.5f, center.getY(), center.getZ()+ 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);
        RenderUtils.drawTextAtWorld( colors.get(c1b), b1.getX()+ 0.5f, b1.getY()+0.6f, b1.getZ()+ 0.5f,c1b == b1b ? 0xFF00FF00 : 0xFFFF0000, 0.03F, false, false, partialTicks);
        RenderUtils.drawTextAtWorld( colors.get(c2b), b2.getX()+ 0.5f, b2.getY()+0.6f, b2.getZ()+ 0.5f,c2b == b2b ? 0xFF00FF00 : 0xFFFF0000, 0.03F, false, false, partialTicks);
        RenderUtils.drawTextAtWorld( colors.get(c3b), b3.getX()+ 0.5f, b3.getY()+0.6f, b3.getZ()+ 0.5f,c3b == b3b ? 0xFF00FF00 : 0xFFFF0000, 0.03F, false, false, partialTicks);

        RenderUtils.drawTextAtWorld( colors.get(b1b), b1.getX()+ 0.5f, b1.getY()+0.2f, b1.getZ()+ 0.5f,0xFFFFFF00, 0.03F, false, false, partialTicks);
        RenderUtils.drawTextAtWorld( colors.get(b2b), b2.getX()+ 0.5f, b2.getY()+0.2f, b2.getZ()+ 0.5f,0xFFFFFF00, 0.03F, false, false, partialTicks);
        RenderUtils.drawTextAtWorld( colors.get(b3b), b3.getX()+ 0.5f, b3.getY()+0.2f, b3.getZ()+ 0.5f,0xFFFFFF00, 0.03F, false, false, partialTicks);
    }

    @Override
    public void onSendData() {
        super.onSendData();
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByte("a", (byte) 6);
        nbt.setByte("f", (byte) Block.getIdFromBlock(getChamber().getBlock(0,3,3).getBlock()));
        nbt.setByte("s", (byte) Block.getIdFromBlock(getChamber().getBlock(0,3,2).getBlock()));
        nbt.setByte("t", (byte) Block.getIdFromBlock(getChamber().getBlock(0,3,1).getBlock()));
        getSolver().communicate(nbt);
    }

    @Override
    public void onDataRecieve(NBTTagCompound compound) {
        if (7 == compound.getByte("a")) {
            int answer = compound.getInteger("b");
            b1b = (byte) (answer / 100);
            b2b = (byte) ((answer % 100) / 10);
            b3b = (byte) (answer % 10);
        }
    }

    private byte match(EntityArmorStand armorStand) {
        if (armorStand == null) {
            return 0;
        }
        ItemStack item = armorStand.getInventory()[4];
        NBTTagList list = item.getTagCompound().getCompoundTag("SkullOwner").getCompoundTag("Properties").getTagList("textures", 10);
        String str = ((NBTTagCompound)list.get(0)).getString("Value");
        return (byte) (!integers.containsKey(str) ? 0 : integers.get(str));
    }

    private static final BiMap<String, Integer> integers = HashBiMap.create();
    private static final BiMap<Integer, String> colors = HashBiMap.create();
    static {
        colors.put(0, "?");
    }
}
