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

import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import lombok.Getter;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraftforge.event.entity.living.LivingEvent;

import java.util.*;

@Getter
public class BossfightProcessorLivid extends GeneralBossfightProcessor {
    private String realLividName;
    private String prefix;
    private EntityOtherPlayerMP realLivid;

    private final Set<String> knownLivids = new HashSet<String>();

    private boolean isMasterMode;

    public BossfightProcessorLivid(boolean isMasterMode) {
        addPhase(PhaseData.builder().phase("start").build());
        this.isMasterMode = isMasterMode;
    }
    private static final Map<String, String> lividColorPrefix = new HashMap<String, String>() {{
            put("Vendetta", "§f");
            put("Crossed", "§d");
            put("Hockey", "§c");
            put("Doctor", "§7");
            put("Frog", "§2");
            put("Smile", "§a");
            put("Scream", "§1");
            put("Purple", "§5");
            put("Arcade", "§e");
    }};
    @Override
    public void onEntityUpdate(LivingEvent.LivingUpdateEvent updateEvent) {
        if (updateEvent.entityLiving.getName().endsWith("Livid") && updateEvent.entityLiving instanceof EntityOtherPlayerMP) {
            if (!knownLivids.contains(updateEvent.entityLiving.getName())) {
                knownLivids.add(updateEvent.entityLiving.getName());
                realLividName = updateEvent.entityLiving.getName();
                realLivid = (EntityOtherPlayerMP) updateEvent.entityLiving;
                prefix = lividColorPrefix.get(realLividName.split(" ")[0]);
            } else if (realLividName.equalsIgnoreCase(updateEvent.entityLiving.getName())) {
                realLivid = (EntityOtherPlayerMP) updateEvent.entityLiving;
            }
        } else if (updateEvent.entityLiving.getName().startsWith(prefix+"﴾ ") && updateEvent.entityLiving instanceof EntityArmorStand) {
            lividStand = (EntityArmorStand) updateEvent.entityLiving;
        }
    }
    private EntityArmorStand lividStand;

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

    @Override
    public String getBossName() {
        return realLividName == null ? "Livid" : realLividName;
    }
}
