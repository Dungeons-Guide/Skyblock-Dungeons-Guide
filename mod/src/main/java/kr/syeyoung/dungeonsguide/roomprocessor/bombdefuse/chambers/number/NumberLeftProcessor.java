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

package kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.number;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.RoomProcessorBombDefuseSolver;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.BDChamber;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.GeneralDefuseChamberProcessor;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;

public class NumberLeftProcessor extends GeneralDefuseChamberProcessor {
    public NumberLeftProcessor(RoomProcessorBombDefuseSolver solver, BDChamber chamber) {
        super(solver, chamber);

        d1p = chamber.getBlockPos(1,1,4);
        d2p = chamber.getBlockPos(2,1,4);
        d3p = chamber.getBlockPos(6,1,4);
        d4p = chamber.getBlockPos(7,1,4);
    }

    @Override
    public String getName() {
        return "numberLeft";
    }


    private int answer = -1, d1, d2, d3 ,d4;
    private final BlockPos d1p;
    private final BlockPos d2p;
    private final BlockPos d3p;
    private final BlockPos d4p;
    @Override
    public void tick() {
        super.tick();
        if (answer != -1) return;
        d1 = match(getChamber().getEntityAt(EntityArmorStand.class,d1p));
        d2 = match(getChamber().getEntityAt(EntityArmorStand.class,d2p));
        d3 = match(getChamber().getEntityAt(EntityArmorStand.class,d3p));
        d4 = match(getChamber().getEntityAt(EntityArmorStand.class,d4p));
        if (d1 == -1 || d2 == -1 || d3 == -1 || d4 == -1) return;

        answer = d1 * 1000 + d2 * 100 + d3 * 10 + d4;
    }

    @Override
    public void drawScreen(float partialTicks) {
        if (answer == -1) return;
        drawPressKey();
    }

    @Override
    public void drawWorld(float partialTicks) {
        super.drawWorld(partialTicks);
            RenderUtils.drawTextAtWorld(d1+"", d1p.getX()+ 0.5f, d1p.getY()+ 0.5f, d1p.getZ()+ 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);
            RenderUtils.drawTextAtWorld(d2+"", d2p.getX()+ 0.5f, d2p.getY()+ 0.5f, d2p.getZ()+ 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);
            RenderUtils.drawTextAtWorld(d3+"", d3p.getX()+ 0.5f, d3p.getY()+ 0.5f, d3p.getZ()+ 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);
            RenderUtils.drawTextAtWorld(d4+"", d4p.getX()+ 0.5f, d4p.getY()+ 0.5f, d4p.getZ()+ 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);

    }

    @Override
    public void onSendData() {
        if (answer == -1) return;
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByte("a", (byte) 1);
        nbt.setInteger("b", answer);
        getSolver().communicate(nbt);
    }

    @Override
    public void onDataRecieve(NBTTagCompound compound) {
        if (1 == compound.getByte("a")) {
            answer = compound.getInteger("b");
            d1 = answer / 1000;
            d2 = (answer % 1000) / 100;
            d3 = (answer % 100) / 10;
            d4 = (answer % 10);
            answer = d1 * 1000 + d2 * 100 + d3 * 10 + d4;
        }
    }

    private int match(EntityArmorStand armorStand) {
        if (armorStand == null) return -1;
        ItemStack item = armorStand.getInventory()[4];
        NBTTagList list = item.getTagCompound().getCompoundTag("SkullOwner").getCompoundTag("Properties").getTagList("textures", 10);
        String str = ((NBTTagCompound)list.get(0)).getString("Value");
        return !integers.containsKey(str) ? -1 : integers.get(str);
    }

    private static final BiMap<String, Integer> integers = HashBiMap.create(10);
    {
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzFhOTQ2M2ZkM2M0MzNkNWUxZDlmZWM2ZDVkNGIwOWE4M2E5NzBiMGI3NGRkNTQ2Y2U2N2E3MzM0OGNhYWIifX19", 1);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWNiNDE5ZDk4NGQ4Nzk2MzczYzk2NDYyMzNjN2EwMjY2NGJkMmNlM2ExZDM0NzZkZDliMWM1NDYzYjE0ZWJlIn19fQ==", 2);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjhlYmFiNTdiNzYxNGJiMjJhMTE3YmU0M2U4NDhiY2QxNGRhZWNiNTBlOGY1ZDA5MjZlNDg2NGRmZjQ3MCJ9fX0=", 3);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjJiZmNmYjQ4OWRhODY3ZGNlOTZlM2MzYzE3YTNkYjdjNzljYWU4YWMxZjlhNWE4YzhhYzk1ZTRiYTMifX19", 4);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWY0ZWNmMTEwYjBhY2VlNGFmMWRhMzQzZmIxMzZmMWYyYzIxNjg1N2RmZGE2OTYxZGVmZGJlZTdiOTUyOCJ9fX0=", 5);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjMzMWE2YTZmY2Q2OTk1YjYyMDg4ZDM1M2JmYjY4ZDliODlhZTI1ODMyNWNhZjNmMjg4NjQ2NGY1NGE3MzI5In19fQ==", 6);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDRiYTZhYzA3ZDQyMjM3N2E4NTU3OTNmMzZkZWEyZWQyNDAyMjNmNTJmZDE2NDgxODE2MTJlY2QxYTBjZmQ1In19fQ==", 7);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzYxYThhNjQxNDM3YmU5YWVhMjA3MjUzZGQzZjI1NDQwZDk1NGVhMmI1ODY2YzU1MmYzODZiMjlhYzRkMDQ5In19fQ==", 8);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTE5MjhlMWJmZDg2YTliNzkzOTdjNGNiNGI2NWVmOTlhZjQ5YjdkNWY3OTU3YWQ2MmMwYzY5OWE2MjJjZmJlIn19fQ==", 9);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTVhMjI0ODA3NjkzOTc4ZWQ4MzQzNTVmOWU1MTQ1ZjljNTZlZjY4Y2Y2ZjJjOWUxNzM0YTQ2ZTI0NmFhZTEifX19", 0);
    }
}
