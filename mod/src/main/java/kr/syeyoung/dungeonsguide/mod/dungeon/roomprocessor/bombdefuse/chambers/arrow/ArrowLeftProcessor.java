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

package kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers.arrow;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.RoomProcessorBombDefuseSolver;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers.BDChamber;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bombdefuse.chambers.GeneralDefuseChamberProcessor;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;

import java.util.HashMap;
import java.util.Map;

public class ArrowLeftProcessor extends GeneralDefuseChamberProcessor {
    public ArrowLeftProcessor(RoomProcessorBombDefuseSolver solver, BDChamber chamber) {
        super(solver, chamber);

        for (int i = 0; i < 9; i++) {
            grid[i] = chamber.getBlockPos(7,1 + (i / 3),1 + (i % 3));
        }
    }

    @Override
    public String getName() {
        return "arrowLeft";
    }


    private int answer = -1;
    private final int[] answers = new int[9];
    private final BlockPos[] grid = new BlockPos[9];
    @Override
    public void tick() {
        super.tick();
        if (answer != -1) return;
        for (int i = 0; i < 9; i++)
            answers[i] = match(getChamber().getEntityAt(EntityArmorStand.class, grid[i].add(0, -1, 0)));

        answer = 0;
        for (int i =0; i < 9; i++) {
            answer = answer * 10 + answers[i];
        }
    }

    @Override
    public void drawScreen(float partialTicks) {
        if (answer == -1) return;
        drawPressKey();
    }

    @Override
    public void drawWorld(float partialTicks) {
        super.drawWorld(partialTicks);

        if (answer == -1) return;
        for (int i = 0; i < 9; i++) {
            BlockPos pos = grid[i];
            int direction = answers[i];
            String charac = arrows.get(direction);

            RenderUtils.drawTextAtWorld(charac, pos.getX()+ 0.5f, pos.getY()+ 0.5f, pos.getZ()+ 0.5f, 0xFFFFFFFF, 0.05F, false, false, partialTicks);
        }
    }

    @Override
    public void onSendData() {
        if (answer == -1) return;
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByte("a", (byte) 3);
        nbt.setInteger("b", answer);
        getSolver().communicate(nbt);
    }

    @Override
    public void onDataRecieve(NBTTagCompound compound) {
        if (3 == compound.getByte("a")) {
            answer = compound.getInteger("b");
            for (int i = 8; i >= 0; i--) {
                answers[i] = answer % 10;
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
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDczZTFmZTBmZmNkNTQ2YzIxYjYzZWFhOTEwYWI1YzkzMTMyYTY0ZTY0NDE3OWZjY2FhMjFkYzhiYzY2MCJ9fX0=", 1);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNThmZTI1MWE0MGU0MTY3ZDM1ZDA4MWMyNzg2OWFjMTUxYWY5NmI2YmQxNmRkMjgzNGQ1ZGM3MjM1ZjQ3NzkxZCJ9fX0=", 2);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTc3ZWM0NGZiNzUwM2RlNmFmZTNiZGQ3ZTU4M2M3YjkzOTc5ZjU5ZjZkNjM0YjZiNmE1YWY3ZjNhMWM1ODUxNSJ9fX0=", 3);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjJmM2EyZGZjZTBjM2RhYjdlZTEwZGIzODVlNTIyOWYxYTM5NTM0YThiYTI2NDYxNzhlMzdjNGZhOTNiIn19fQ==", 4);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGUxY2U5YzBkMzMyMzZmOTkyMWQyMmM0ZWM0YmY1MDkyMzdhZWY2NzZiNWQxZDJiYWNjOTNmNWE4MTk0ODAifX19", 5);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWI3Y2U2ODNkMDg2OGFhNDM3OGFlYjYwY2FhNWVhODA1OTZiY2ZmZGFiNmI1YWYyZDEyNTk1ODM3YTg0ODUzIn19fQ==", 6);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTI2NTFlY2QzNzgwY2Y2ZTQ1MWVjYWNjZGVlNjk3MTVjMDhjYWU3Y2Q0NTA5MDg0NDYyY2RjZDk2M2E2YjMyMiJ9fX0=", 7);
        integers.put("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmIwZjZlOGFmNDZhYzZmYWY4ODkxNDE5MWFiNjZmMjYxZDY3MjZhNzk5OWM2MzdjZjJlNDE1OWZlMWZjNDc3In19fQ==", 8);

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
