package kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.color;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import kr.syeyoung.dungeonsguide.Keybinds;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
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
import org.omg.PortableInterceptor.INACTIVE;

import java.util.HashMap;
import java.util.Map;

public class ColorLeftProcessor extends GeneralDefuseChamberProcessor {
    public ColorLeftProcessor(RoomProcessorBombDefuseSolver solver, BDChamber chamber) {
        super(solver, chamber);
        center = chamber.getBlockPos(4,6,4);
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
        RenderUtils.drawTextAtWorld(w1 == null ? "Request Not Received Yet" : "Building- "+w1.getLocalizedName() +" / "+w2.getLocalizedName() +" / "+w3.getLocalizedName() , center.getX()+ 0.5f, center.getY(), center.getZ()+ 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);

        RenderUtils.drawTextAtWorld(w1 == null ? "null" : w1.getLocalizedName(), b1p.getX()+ 0.5f, b1p.getY() + 0.2f, b1p.getZ()+ 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);
        RenderUtils.drawTextAtWorld(w2 == null ? "null" : w2.getLocalizedName(), b2p.getX()+ 0.5f, b2p.getY() + 0.2f, b2p.getZ()+ 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);
        RenderUtils.drawTextAtWorld(w3 == null ? "null" : w3.getLocalizedName(), b3p.getX()+ 0.5f, b3p.getY() + 0.2f, b3p.getZ()+ 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);
        RenderUtils.drawTextAtWorld(c1 == null ? "null" : c1.getLocalizedName(), b1p.getX()+ 0.5f, b1p.getY() + 0.6f, b1p.getZ()+ 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);
        RenderUtils.drawTextAtWorld(c2 == null ? "null" : c2.getLocalizedName(), b2p.getX()+ 0.5f, b2p.getY() + 0.6f, b2p.getZ()+ 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);
        RenderUtils.drawTextAtWorld(c3 == null ? "null" : c3.getLocalizedName(), b3p.getX()+ 0.5f, b3p.getY() + 0.6f, b3p.getZ()+ 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);

        if (FeatureRegistry.DEBUG.isEnabled()) {
            RenderUtils.drawTextAtWorld(s1 + "", b1p.getX() + 0.5f, b1p.getY() + 2.6f, b1p.getZ() + 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);
            RenderUtils.drawTextAtWorld(s2 + "", b2p.getX() + 0.5f, b2p.getY() + 3.6f, b2p.getZ() + 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);
            RenderUtils.drawTextAtWorld(s3 + "", b3p.getX() + 0.5f, b3p.getY() + 2.6f, b3p.getZ() + 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);
        }

        RenderUtils.drawTextAtWorld(colors.get(s1), b1p.getX()+ 0.5f, b1p.getY() + 2.2f, b1p.getZ()+ 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);
        RenderUtils.drawTextAtWorld(colors.get(s2), b2p.getX()+ 0.5f, b2p.getY() + 3.2f, b2p.getZ()+ 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);
        RenderUtils.drawTextAtWorld(colors.get(s3), b3p.getX()+ 0.5f, b3p.getY() + 2.2f, b3p.getZ()+ 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);

        if (FeatureRegistry.DEBUG.isEnabled()) {
            RenderUtils.drawTextAtWorld(s1t + "", b1p.getX() + 0.5f, b1p.getY() + 1.5f, b1p.getZ() + 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);
            RenderUtils.drawTextAtWorld(s2t + "", b2p.getX() + 0.5f, b2p.getY() + 2f, b2p.getZ() + 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);
            RenderUtils.drawTextAtWorld(s3t + "", b3p.getX() + 0.5f, b3p.getY() + 1.5f, b3p.getZ() + 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);
        }
    }

    @Override
    public void onSendData() {
        super.onSendData();
        if (!solutionBuilt) return;
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByte("a", (byte) 7);
        int answer = s1 * 10000 + s2 * 100 + s3;
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

    private static final Map<String, Integer> integers = new HashMap<String, Integer>();
    private static final BiMap<Integer, String> colors = HashBiMap.create();
    static {
        colors.put(0, "?");
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2YzZTQwNjI5MTE3NGQyNGNkZjBmOTUzZjhhMTc0YTgyYmIzNDg5ZGNlOGY2NzlhNDQzZWYxYWFlMDE2OTA2MSJ9fX0=", 1);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWE4MWZjYjUxYmUyYTlmODliMWFkYzlkODcyMzliYTQyOWQ2MzVmYmUwMWIzN2VjMzI5MTY0ODg3YmY2NjViIn19fQ==", 1);
        colors.put(1, "blue");
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2FiMDI2M2JkZDc2ZjNlNDE4ZGJhNWJmNDgxYjkyMWNlZDM5N2Q4YjhhMzRhNTU2MWZiN2JlYWE0NmVjZTEifX19", 2);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2ZhNGRkYTZkMTlhMWZlMmQ5ODhkNjVkZWM1MzQyOTUwNTMwODE2NmM5MDY3YjY4YTQ3NzBjYTVjNDM2Y2Y5NCJ9fX0=", 2);
        colors.put(2, "black");
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDg0Njg0MzQ0YWUwOTg1MjlmYzk0MWFhODRlMTk1YmRjYTM3NDhkNjlhY2ZlZTJiYWMxMzMyMTM1ZWRkOThjIn19fQ==", 3);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTM1ODFjMmY5Y2YzNThkN2VkYzc4ZGQ2ZmQ0YjYyNTc1MDFiYzRlNjQ1NWUzM2ZhMGNhYWUyMDdjZjAzMjFhMiJ9fX0=", 3);
        colors.put(3, "green");
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTllNjkxN2YyZmI0ZWEwOGU3MTMyZGYzMDk2MWQyYjVjNTIzYWJiYTE5Y2U0M2Y4MzVmYzE0YzU2OGY0In19fQ==", 4);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWRmMjFmNTMyMTIyNTY2YWY4OTNkYTI3ODgwYTFiNjA5NWMzNTcxMmYyOWEzNzhjZmVjYzdmZTJiMTMyOGFiNCJ9fX0=", 4);
        colors.put(4, "gray");
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODhlZmFkNzRiMjU0ZTU3Yzc5OTc2M2RjZWVlNDUxMWZhMmY4NWFlOWZhNTU2ZWFhOTdkNDViZjY3ZTBiNmIzIn19fQ==", 5);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWZhZGY3NDFhYjc2Y2QzNjIwYWQxNjEzMDAyMDJkN2I1OWEzMzA1MWU1OTY3ZTRiNjE5NGJhYzQwYmIyODBmZiJ9fX0=", 5);
        colors.put(5, "cyan");
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzJlMzZmNmE2NTRkZTc0NTgzZDgwMzAxNzdhZDZlM2FjNjc1NWQ3NDM1ZDkxMjNlOGViZGZmNzRiMmQ5MGNiIn19fQ==", 6);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjJjYmQ5ZjQzNjE5YWI1Y2IxYjExZjkxY2IwM2U5NTVjNmZjNmM0NThhYmY4OWFiNjEwMzEzNDZhMDkwNjEyZSJ9fX0=", 6);
        colors.put(6, "brown");
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjFhZjQ2ZmViZDQ1YzBmNGQ4MWU4ZmExYjY2YjI3NWQ4OWUyNzJiMmFkNTVjOTc4NTUzYTk5YzczM2UxZmYifX19", 7);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTYzZTY2NDZmMWMwZDQxZmQzYmY1NTg0YTFjZTA0NGY1YzQ2ZDU5ODI1OGRiNDYyMTYxMTc4NTlmNTdhZjE5NyJ9fX0=", 7);
        colors.put(7, "light-blue");
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDY3NDcwYTBjMThmNjg1MWU5MTQzNTM3MTllNzk1ODc3ZDI5YjMyNTJmN2U2YmQ0YTFiODY1NzY1YmQ3NGZlYiJ9fX0=", 8);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzc0NzJkNjA4ODIxZjQ1YTg4MDUzNzZlYzBjNmZmY2I3ODExNzgyOWVhNWY5NjAwNDFjMmEwOWQxMGUwNGNiNCJ9fX0=", 8);
        colors.put(8, "lime");
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWJiNDM4NmJjZGE4NGUzNTNjMzFkNzc4ZDNiMTFiY2QyNmZlYTQ5NGRkNjM0OTZiOGE4MmM3Yzc4YTRhZCJ9fX0=", 9);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjk3MDEyZWQ2YTkyYjA1ZWEwZjE5NDk1MDc0ODU0NGUwNzViYWEyODc4MWNhMzczZDFiMjdlMjhjMjY5NTNjIn19fQ==", 9);
        colors.put(9, "magenta");
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2JmNzc5N2EyNGE2YWY4NzVmNWM4MjcxYzViOGM0MjVlMTlmMzcyYTQxNWUwNTUyZmMyNDc3NjNmMjg1OWQxIn19fQ==", 10);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWRmMmViMjA1YTIzYzExOTZiM2VjZjIxZTY4YzA3NmI2OTZlNzYxNjNhYzhmYzRmYjlmNTMxOGMyYTVlNWIxYSJ9fX0=", 10);
        colors.put(10, "orange");
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmJlY2ZiMzg3OTkzNmI4OTllNDIwYmZjZDNhNzRmOGExYmY5ZGQ1NGM1OGVjN2ZiOWY4MWQ5YTVkOTg4ZSJ9fX0=", 11);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDA4ODM2NTBlYTkyOWRiMGVhYmRmNWJjNzU1OTlkOGVmMDBkNzAzNDBjZDFjZTVlMDRhZDk1ZWY4ZWQ4M2I3MyJ9fX0=", 11);
        colors.put(11, "pink");
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmE5NGNiMjVkZTYyOGNhMzU5YjJmNmVhNWE4ODY4Y2JlMjY1OTVlZWRiMmJmZmI3NTA5NjdhZDFlZTE4NTAifX19", 12);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODllYzVhMzAyMjJkMDY1OWIwZGJlZTg0NGI4ZjUzZWFlNjJmZTk1YjRhMzQ0OGE5ZWY3OTBhN2FlZGIyOTZkOSJ9fX0=", 12);
        colors.put(12, "purple");
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODZkMzVhOTYzZDU5ODc4OTRiNmJjMjE0ZTMyOGIzOWNkMjM4MjQyNmZmOWM4ZTA4MmIwYjZhNmUwNDRkM2EzIn19fQ==", 13);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjk1M2IxMmEwOTQ2YjYyOWI0YzA4ODlkNDFmZDI2ZWQyNmZiNzI5ZDRkNTE0YjU5NzI3MTI0YzM3YmI3MGQ4ZCJ9fX0=", 13);
        colors.put(13, "red");
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTk4YmEyYjM3NGNmYzg5NDU0YzFiOGMzMmRiNDU4YTI3MDY3NTQzOWE0OTU0OTZjOTY3NzFjOTg5MTE2MTYyIn19fQ==", 14);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTUyODhkZGM5MTFhNzVmNzdjM2E1ZDMzNjM2NWE4ZjhiMTM5ZmE1MzkzMGI0YjZlZTEzOTg3NWM4MGNlMzY2YyJ9fX0=", 14);
        colors.put(14, "light-gray");
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2ZhZjRjMjlmMWU3NDA1ZjQ2ODBjNWMyYjAzZWY5Mzg0ZjFhZWNmZTI5ODZhZDUwMTM4YzYwNWZlZmZmMmYxNSJ9fX0=", 15);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDA4ZGY2MGM1MTA3NGVlZjI1NDRmZjM4Y2VhZDllMTY2NzVhZTQyNTE5MTYxMDUxODBlMWY4Y2UxOTdhYjNiYyJ9fX0=", 15);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTVhZDkzZDU2NTQ2ZjEyZDUzNTZlZmZjYmM2ZWM0Yzg3YmEyNDVkODFlMTY2MmM0YjgzMGY3ZDI5OGU5In19fQ==", 15);
        colors.put(15, "white");
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDRiMDM3OTRiOWIzZTNiNWQwN2UzYmU2OGI5NmFmODdkZjIxNWMzNzUyZTU0NzM2YzgwZjdkNTBiZDM0MzdhNCJ9fX0=", 16);
        colors.put(16, "rainbow");
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTkxMjdjYjdiZDNhOTg5ZDcyYzJlNWM0MjZlMWNjMTQ0NmI1ZTZkZTc0MTRkNDI3ODNmMmZlNmJhZGIxNzdkNCJ9fX0=", 17);
        colors.put(17, "lilac");
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTkxMjdjYjdiZDNhOTg5ZDcyYzJlNWM0MjZlMWNjMTQ0NmI1ZTZkZTc0MTRkNDI3ODNmMmZlNmJhZGIxNzdkNCJ9fX0=", 18);
        colors.put(18, "dark-red");
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzM4NmY5YjBiMWQ5ODc5YzNkYTMzYzdhOGNhMjQ0MGMxZTQxMWZlOTNjMjdjOWRiYmZmNTZiZDY5N2JiNzM3NSJ9fX0=", 19);
        colors.put(19, "i-rainbow");
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjdiYmQwYjI5MTFjOTZiNWQ4N2IyZGY3NjY5MWE1MWI4YjEyYzZmZWZkNTIzMTQ2ZDhhYzVlZjFiOGVlIn19fQ==", 20);
        colors.put(20, "yellow");
    }
}
