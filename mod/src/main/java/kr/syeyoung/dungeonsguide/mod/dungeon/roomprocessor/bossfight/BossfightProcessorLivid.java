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

package kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bossfight;

import kr.syeyoung.dungeonsguide.mod.events.impl.BlockUpdateEvent;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import lombok.Getter;
import net.minecraft.block.BlockColored;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Tuple;
import net.minecraftforge.event.entity.living.LivingEvent;

import java.util.*;

@Getter
public class BossfightProcessorLivid extends GeneralBossfightProcessor {
    private String realLividName = "Hockey";
    private String prefix = "§c";


    private EntityOtherPlayerMP realLivid;
    private EntityArmorStand lividStand;

    private final boolean isMasterMode;

    public BossfightProcessorLivid(boolean isMasterMode) {
        super(isMasterMode ? "MASTERMODE_CATACOMBS_FLOOR_FIVE" : "CATACOMBS_FLOOR_FIVE");
        addPhase(PhaseData.builder().phase("start").build());
        this.isMasterMode = isMasterMode;
    }
    private static final Map<String, String> lividColorPrefix = new HashMap<String, String>() {{
            put("Vendetta", "§f"); // white
            put("Crossed", "§d"); // light purple
            put("Hockey", "§c"); // red
            put("Doctor", "§7"); // gray
            put("Frog", "§2"); // green
            put("Smile", "§a"); // light green
            put("Scream", "§1"); // blue
            put("Purple", "§5"); // dark purple
            put("Arcade", "§e"); // yellow
    }};
    private static final Map<Integer, String> lividMetadata = new HashMap<Integer, String>() {{
        put(0, "Vendetta"); // white
        put(2, "Crossed"); // magenta
        put(4, "Arcade"); // yellow
        put(5, "Smile"); // light green
        put(6, "Crossed"); // pink
        put(7, "Doctor"); // gray
        put(8, "Doctor"); // gray
        put(10, "Purple"); // dark purple
        put(11, "Scream"); // blue
        put(13, "Frog"); // green
        put(14, "Hockey"); // red
    }};

    private int correctLivid = 14;
    @Override
    public void onEntityUpdate(LivingEvent.LivingUpdateEvent updateEvent) {
        correctLivid = Minecraft.getMinecraft().theWorld.getChunkFromBlockCoords(new BlockPos(5, 108, 42)).getBlockMetadata(new BlockPos(5, 108, 42));
        realLividName = lividMetadata.get(correctLivid);
        prefix = lividColorPrefix.get(realLividName);
        if (updateEvent.entityLiving.getName().startsWith(realLividName) && updateEvent.entityLiving instanceof EntityOtherPlayerMP) {
            realLivid = (EntityOtherPlayerMP) updateEvent.entityLiving;
        } else if (updateEvent.entityLiving.getName().startsWith(prefix+"﴾ ") && updateEvent.entityLiving instanceof EntityArmorStand) {
            lividStand = (EntityArmorStand) updateEvent.entityLiving;
        }
    }

    // §2﴾ §2§lLivid§r§r §a317M§c❤ §2﴿
    @Override
    public List<HealthData> getHealths() {
        List<HealthData> healths = new ArrayList<HealthData>();
        long health = 0;
        if (lividStand != null) {
            try {
                String name = TextUtils.stripColor(lividStand.getName());
                String healthPart = name.split(" ")[2];
                health = TextUtils.reverseFormat(healthPart.substring(0, healthPart.length() - 1));
            } catch (Exception e) {e.printStackTrace();}
        }
        healths.add(new HealthData(realLividName == null ? "unknown" : realLividName, (int) health,isMasterMode ? 600000000 : 7000000 , true));
        return healths;
    }

    private static final BlockPos lividPos = new BlockPos(6, 108, 43);

    @Override
    public void onBlockUpdate(BlockUpdateEvent blockUpdateEvent) {
    }

    @Override
    public String getBossName() {
        return realLividName == null ? "Livid" : realLividName;
    }
}
