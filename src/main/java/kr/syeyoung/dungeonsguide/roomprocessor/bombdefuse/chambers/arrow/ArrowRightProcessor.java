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

package kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.arrow;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import kr.syeyoung.dungeonsguide.events.PlayerInteractEntityEvent;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.RoomProcessorBombDefuseSolver;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.BDChamber;
import kr.syeyoung.dungeonsguide.roomprocessor.bombdefuse.chambers.GeneralDefuseChamberProcessor;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;

import java.util.HashMap;
import java.util.Map;

public class ArrowRightProcessor extends GeneralDefuseChamberProcessor {
    public ArrowRightProcessor(RoomProcessorBombDefuseSolver solver, BDChamber chamber) {
        super(solver, chamber);

        for (int i = 0; i < 9; i++) {
            grid[i] = chamber.getBlockPos(1,1 + (i / 3),1 + (2 - (i % 3)));
        }

        center = chamber.getBlockPos(4,4,4);
    }

    @Override
    public String getName() {
        return "arrowRight";
    }


    private int answer = -1;
    private final int[] correctAnswers = new int[9];
    private final int[] currentAnswers = new int[9];
    private final BlockPos[] grid = new BlockPos[9];
    private final BlockPos center;
    @Override
    public void tick() {
        super.tick();
        for (int i = 0; i < 9; i++)
            currentAnswers[i] = match(getChamber().getEntityAt(EntityArmorStand.class, grid[i].add(0, -1, 0)));
    }

    @Override
    public void drawWorld(float partialTicks) {
        super.drawWorld(partialTicks);

        RenderUtils.drawTextAtWorld(answer == -1 ? "Answer not received yet. Visit left room to obtain solution" : "" , center.getX()+ 0.5f, center.getY(), center.getZ()+ 0.5f, 0xFFFFFFFF, 0.03F, false, false, partialTicks);

        for (int i = 0; i < 9; i++) {
            BlockPos pos = grid[i];
            int direction = correctAnswers[i];
            int direction2 = currentAnswers[i];
            String charac = arrows.get(direction);
            String car2 = arrows.get(direction2);

            RenderUtils.drawTextAtWorld(car2, pos.getX()+ 0.5f, pos.getY()+ 0.6f, pos.getZ()+ 0.5f, direction == direction2 ? 0xFF00FF00 : 0xFFFF0000, 0.03F, false, false, partialTicks);
            RenderUtils.drawTextAtWorld(charac, pos.getX()+ 0.5f, pos.getY()+ 0.2f, pos.getZ()+ 0.5f, 0xFFFFFF00, 0.03F, false, false, partialTicks);
        }
    }

    @Override
    public void onDataRecieve(NBTTagCompound compound) {
        if (3 == compound.getByte("a")) {
            answer = compound.getInteger("b");
            for (int i = 8; i >= 0; i--) {
                correctAnswers[i] = answer % 10;
                answer = answer / 10;
            }
            answer = compound.getInteger("b");
        }
    }

    private int match(EntityArmorStand armorStand) {
        if (armorStand == null) return -1;
        ItemStack item = armorStand.getInventory()[4];
        NBTTagList list = item.getTagCompound().getCompoundTag("SkullOwner").getCompoundTag("Properties").getTagList("textures", 10);
        String str = ((NBTTagCompound)list.get(0)).getString("Value");
        return !integers.containsKey(str) ? -1 : integers.get(str);
    }

    private static final BiMap<String, Integer> integers = HashBiMap.create(8);
    private static final Map<Integer, String> arrows = new HashMap<Integer, String>();
    {
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODY1NDI2YTMzZGY1OGI0NjVmMDYwMWRkOGI5YmVjMzY5MGIyMTkzZDFmOTUwM2MyY2FhYjc4ZjZjMjQzOCJ9fX0=", 1);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzA0MGZlODM2YTZjMmZiZDJjN2E5YzhlYzZiZTUxNzRmZGRmMWFjMjBmNTVlMzY2MTU2ZmE1ZjcxMmUxMCJ9fX0=", 2);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTBlMGE0ZDQ4Y2Q4MjlhNmQ1ODY4OTA5ZDY0M2ZhNGFmZmQzOWU4YWU2Y2FhZjZlYzc5NjA5Y2Y3NjQ5YjFjIn19fQ==", 3);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19", 4);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzVjYmRiMjg5OTFhMTZlYjJjNzkzNDc0ZWY3ZDBmNDU4YTVkMTNmZmZjMjgzYzRkNzRkOTI5OTQxYmIxOTg5In19fQ==", 5);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzQzNzM0NmQ4YmRhNzhkNTI1ZDE5ZjU0MGE5NWU0ZTc5ZGFlZGE3OTVjYmM1YTEzMjU2MjM2MzEyY2YifX19", 6);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzU0Y2U4MTU3ZTcxZGNkNWI2YjE2NzRhYzViZDU1NDkwNzAyMDI3YzY3NWU1Y2RjZWFjNTVkMmZiYmQ1YSJ9fX0=", 7);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==", 8);


        arrows.put(0, "?");
        arrows.put(1, "↖");
        arrows.put(2, "↑");
        arrows.put(3, "↗");
        arrows.put(4, "→");
        arrows.put(5, "↘");
        arrows.put(6, "↓");
        arrows.put(7, "↙");
        arrows.put(8, "←");
    }
}
