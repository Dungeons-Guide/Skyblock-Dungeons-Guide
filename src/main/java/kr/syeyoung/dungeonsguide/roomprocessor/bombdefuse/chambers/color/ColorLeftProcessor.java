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
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

public class ColorLeftProcessor extends GeneralDefuseChamberProcessor {
    public ColorLeftProcessor(RoomProcessorBombDefuseSolver solver, BDChamber chamber) {
        super(solver, chamber);
        center = chamber.getBlockPos(4,4,4);
        b1p = chamber.getBlockPos(1,1,4);
        b2p = chamber.getBlockPos(4,1,4);
        b3p = chamber.getBlockPos(7,1,4);
    }

    private BlockPos center;

    private Block w1, w2, w3, c1, c2, c3;
    private BlockPos b1p, b2p, b3p;
    private int s1, s2, s3;
    private int s1t, s2t, s3t;
    private boolean solutionBuilt;

    @Override
    public String getName() {
        return "colorRight";
    }

    @Override
    public void tick() {
        super.tick();
        if (solutionBuilt) return;
        World w = getChamber().getRoom().getContext().getWorld();
        if ((c1 = w.getBlockState(b1p).getBlock()) == w1 && s1t < 7) {
            int semi = match(getChamber().getEntityAt(EntityArmorStand.class,b1p.add(0, 1, 0)));
            if (s1 == semi) {
                s1t++;
            } else {
                s1 = semi;
                s1t = 0;
            }
        }
        if ((c2 = w.getBlockState(b2p).getBlock()) == w2 && s2t < 7) {
            int semi = match(getChamber().getEntityAt(EntityArmorStand.class,b2p.add(0, 2, 0)));
            if (s2 == semi) {
                s2t++;
            } else {
                s2 = semi;
                s2t = 0;
            }
        }
        if ((c3 =w.getBlockState(b3p).getBlock()) == w3 && s3t < 7) {
            int semi = match(getChamber().getEntityAt(EntityArmorStand.class,b3p.add(0, 1, 0)));
            if (s3== semi) {
                s3t++;
            } else {
                s3 = semi;
                s3t = 0;
            }
        }

        if (s1t > 5 && s2t > 5 && s3t > 5) {
            solutionBuilt = true;
        }
    }

    @Override
    public void drawScreen(float partialTicks) {
        super.drawScreen(partialTicks);
        if (solutionBuilt)
            drawPressKey();
    }

    @Override
    public void drawWorld(float partialTicks) {
        super.drawWorld(partialTicks);
        RenderUtils.drawTextAtWorld(w1 == null ? "Request Not Received Yet" : "" , center.getX()+ 0.5f, center.getY(), center.getZ()+ 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);

        RenderUtils.drawTextAtWorld(w1 == null ? "null" : w1.getLocalizedName(), b1p.getX()+ 0.5f, b1p.getY() + 0.2f, b1p.getZ()+ 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);
        RenderUtils.drawTextAtWorld(w2 == null ? "null" : w2.getLocalizedName(), b2p.getX()+ 0.5f, b2p.getY() + 0.2f, b2p.getZ()+ 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);
        RenderUtils.drawTextAtWorld(w3 == null ? "null" : w3.getLocalizedName(), b3p.getX()+ 0.5f, b3p.getY() + 0.2f, b3p.getZ()+ 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);
        RenderUtils.drawTextAtWorld(c1 == null ? "null" : c1.getLocalizedName(), b1p.getX()+ 0.5f, b1p.getY() + 0.6f, b1p.getZ()+ 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);
        RenderUtils.drawTextAtWorld(c2 == null ? "null" : c2.getLocalizedName(), b2p.getX()+ 0.5f, b2p.getY() + 0.6f, b2p.getZ()+ 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);
        RenderUtils.drawTextAtWorld(c3 == null ? "null" : c3.getLocalizedName(), b3p.getX()+ 0.5f, b3p.getY() + 0.6f, b3p.getZ()+ 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);

        RenderUtils.drawTextAtWorld(s1 + "", b1p.getX()+ 0.5f, b1p.getY() + 2.6f, b1p.getZ()+ 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);
        RenderUtils.drawTextAtWorld(s2 + "", b2p.getX()+ 0.5f, b2p.getY() + 3.6f, b2p.getZ()+ 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);
        RenderUtils.drawTextAtWorld(s3 + "", b3p.getX()+ 0.5f, b3p.getY() + 2.6f, b3p.getZ()+ 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);

        RenderUtils.drawTextAtWorld(colors.get(s1), b1p.getX()+ 0.5f, b1p.getY() + 2.2f, b1p.getZ()+ 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);
        RenderUtils.drawTextAtWorld(colors.get(s2), b2p.getX()+ 0.5f, b2p.getY() + 3.2f, b2p.getZ()+ 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);
        RenderUtils.drawTextAtWorld(colors.get(s3), b3p.getX()+ 0.5f, b3p.getY() + 2.2f, b3p.getZ()+ 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);

        RenderUtils.drawTextAtWorld(s1t+"", b1p.getX()+ 0.5f, b1p.getY() + 1.5f, b1p.getZ()+ 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);
        RenderUtils.drawTextAtWorld(s2t+"", b2p.getX()+ 0.5f, b2p.getY() + 2f, b2p.getZ()+ 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);
        RenderUtils.drawTextAtWorld(s3t+"", b3p.getX()+ 0.5f, b3p.getY() + 1.5f, b3p.getZ()+ 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);

    }

    @Override
    public void onSendData() {
        super.onSendData();
        if (!solutionBuilt) return;
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByte("a", (byte) 7);
        int answer = s1 * 100 + s2 * 10 + s3;
        nbt.setInteger("b", answer);
        getSolver().communicate(nbt);
    }

    @Override
    public void onDataRecieve(NBTTagCompound compound) {
        if (6 == compound.getByte("a")) {
            w1 = Block.getBlockById(compound.getByte("f"));
            w2 = Block.getBlockById(compound.getByte("s"));
            w3 = Block.getBlockById(compound.getByte("t"));
            solutionBuilt = false;
            s1 = s2 = s3 = s1t = s2t = s3t =0;
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
