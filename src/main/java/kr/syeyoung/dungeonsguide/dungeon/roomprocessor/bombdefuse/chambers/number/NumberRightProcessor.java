/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.dungeon.roomprocessor.bombdefuse.chambers.number;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.bombdefuse.RoomProcessorBombDefuseSolver;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.bombdefuse.chambers.BDChamber;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.bombdefuse.chambers.GeneralDefuseChamberProcessor;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;

public class NumberRightProcessor extends GeneralDefuseChamberProcessor {
    public NumberRightProcessor(RoomProcessorBombDefuseSolver solver, BDChamber chamber) {
        super(solver, chamber);

        d1p = chamber.getBlockPos(1,1,4);
        d2p = chamber.getBlockPos(2,1,4);
        d3p = chamber.getBlockPos(6,1,4);
        d4p = chamber.getBlockPos(7,1,4);
        center = chamber.getBlockPos(4,4,4);
    }

    @Override
    public String getName() {
        return "numberRight";
    }


    private int answer = -1, d1, d2, d3 ,d4, a1, a2, a3, a4;
    private final BlockPos d1p;
    private final BlockPos d2p;
    private final BlockPos d3p;
    private final BlockPos d4p;
    private final BlockPos center;
    @Override
    public void tick() {
        super.tick();
        a1 = match(getChamber().getEntityAt(EntityArmorStand.class,d1p));
        a2 = match(getChamber().getEntityAt(EntityArmorStand.class,d2p));
        a3 = match(getChamber().getEntityAt(EntityArmorStand.class,d3p));
        a4 = match(getChamber().getEntityAt(EntityArmorStand.class,d4p));
    }

    @Override
    public void drawWorld(float partialTicks) {
        super.drawWorld(partialTicks);
        RenderUtils.drawTextAtWorld(a1+"", d1p.getX()+ 0.5f, d1p.getY()+ 0.6f, d1p.getZ()+ 0.5f, a1 == d1 ? 0xFF00FF00 : 0xFFFF0000, 0.03F, false, false, partialTicks);
        RenderUtils.drawTextAtWorld(a2+"", d2p.getX()+ 0.5f, d2p.getY()+ 0.6f, d2p.getZ()+ 0.5f, a2 == d2 ? 0xFF00FF00 : 0xFFFF0000, 0.03F, false, false, partialTicks);
        RenderUtils.drawTextAtWorld(a3+"", d3p.getX()+ 0.5f, d3p.getY()+ 0.6f, d3p.getZ()+ 0.5f, a3 == d3 ? 0xFF00FF00 : 0xFFFF0000, 0.03F, false, false, partialTicks);
        RenderUtils.drawTextAtWorld(a4+"", d4p.getX()+ 0.5f, d4p.getY()+ 0.6f, d4p.getZ()+ 0.5f, a4 == d4 ? 0xFF00FF00 : 0xFFFF0000, 0.03F, false, false, partialTicks);
        RenderUtils.drawTextAtWorld(answer == -1 ? "Answer not received yet. Visit left room to obtain solution" : ("Solution: "+answer) , center.getX()+ 0.5f, center.getY(), center.getZ()+ 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);
        RenderUtils.drawTextAtWorld(d1+"", d1p.getX()+ 0.5f, d1p.getY()+ 0.2f, d1p.getZ()+ 0.5f, 0xFFFFFF00, 0.03F, false, false, partialTicks);
        RenderUtils.drawTextAtWorld(d2+"", d2p.getX()+ 0.5f, d2p.getY()+ 0.2f, d2p.getZ()+ 0.5f, 0xFFFFFF00, 0.03F, false, false, partialTicks);
        RenderUtils.drawTextAtWorld(d3+"", d3p.getX()+ 0.5f, d3p.getY()+ 0.2f, d3p.getZ()+ 0.5f, 0xFFFFFF00, 0.03F, false, false, partialTicks);
        RenderUtils.drawTextAtWorld(d4+"", d4p.getX()+ 0.5f, d4p.getY()+ 0.2f, d4p.getZ()+ 0.5f, 0xFFFFFF00, 0.03F, false, false, partialTicks);
    }

    @Override
    public void onDataRecieve(NBTTagCompound compound) {
        if (1 == compound.getByte("a")) {
            answer = compound.getInteger("b");
            d1 = answer / 1000;
            d2 = (answer % 1000) / 100;
            d3 = (answer % 100) / 10;
            d4 = (answer % 10);
            answer = d4 * 1000 + d3 * 100 + d2 * 10 + d1;
        }
    }

    private int match(EntityArmorStand armorStand) {
        if (armorStand == null) {
            return -1;
        }
        ItemStack item = armorStand.getInventory()[4];
        NBTTagList list = item.getTagCompound().getCompoundTag("SkullOwner").getCompoundTag("Properties").getTagList("textures", 10);
        String str = ((NBTTagCompound)list.get(0)).getString("Value");
        return !integers.containsKey(str) ? -1 : integers.get(str);
    }

    private static final BiMap<String, Integer> integers = HashBiMap.create(10);
    {
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzFiYzJiY2ZiMmJkMzc1OWU2YjFlODZmYzdhNzk1ODVlMTEyN2RkMzU3ZmMyMDI4OTNmOWRlMjQxYmM5ZTUzMCJ9fX0=", 1);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNkOWVlZWU4ODM0Njg4ODFkODM4NDhhNDZiZjMwMTI0ODVjMjNmNzU3NTNiOGZiZTg0ODczNDE0MTk4NDcifX19", 2);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWQ0ZWFlMTM5MzM4NjBhNmRmNWU4ZTk1NTY5M2I5NWE4YzNiMTVjMzZiOGI1ODc1MzJhYzA5OTZiYzM3ZTUifX19", 3);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDJlNzhmYjIyNDI0MjMyZGMyN2I4MWZiY2I0N2ZkMjRjMWFjZjc2MDk4NzUzZjJkOWMyODU5ODI4N2RiNSJ9fX0=", 4);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmQ1N2UzYmM4OGE2NTczMGUzMWExNGUzZjQxZTAzOGE1ZWNmMDg5MWE2YzI0MzY0M2I4ZTU0NzZhZTIifX19", 5);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzM0YjM2ZGU3ZDY3OWI4YmJjNzI1NDk5YWRhZWYyNGRjNTE4ZjVhZTIzZTcxNjk4MWUxZGNjNmIyNzIwYWIifX19", 6);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmRiNmViMjVkMWZhYWJlMzBjZjQ0NGRjNjMzYjU4MzI0NzVlMzgwOTZiN2UyNDAyYTNlYzQ3NmRkN2I5In19fQ==", 7);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTkxOTQ5NzNhM2YxN2JkYTk5NzhlZDYyNzMzODM5OTcyMjI3NzRiNDU0Mzg2YzgzMTljMDRmMWY0Zjc0YzJiNSJ9fX0=", 8);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTY3Y2FmNzU5MWIzOGUxMjVhODAxN2Q1OGNmYzY0MzNiZmFmODRjZDQ5OWQ3OTRmNDFkMTBiZmYyZTViODQwIn19fQ==", 9);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMGViZTdlNTIxNTE2OWE2OTlhY2M2Y2VmYTdiNzNmZGIxMDhkYjg3YmI2ZGFlMjg0OWZiZTI0NzE0YjI3In19fQ==", 0);
    }
}
