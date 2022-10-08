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

package kr.syeyoung.dungeonsguide.dungeon.roomprocessor.bossfight;

import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraftforge.event.entity.living.LivingEvent;

import java.util.ArrayList;
import java.util.List;

public class BossfightProcessorScarf extends GeneralBossfightProcessor {
    public BossfightProcessorScarf() {
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("start")
                .signatureMsg("§r§c[BOSS] Scarf §r§f: This is where the journey ends for you, Adventurers.§r")
                .nextPhase("fight-1").build()
        );
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("fight-1")
                .signatureMsg("§r§c[BOSS] Scarf §r§f: ARISE, MY CREATIONS!§r")
                .nextPhase("first-defeat").build()
        );
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("first-defeat")
                .signatureMsg("§r§c[BOSS] Scarf §r§f: Those toys are not strong enough I see.§r")
                .nextPhase("fight-2").build()
        );
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("fight-2")
                .signatureMsg("§r§c[BOSS] Scarf §r§f: Did you forget? I was taught by the best! Let's dance.§r")
                .nextPhase("final-defeat").build()
        );
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("final-defeat")
                .signatureMsg("§r§c[BOSS] Scarf §r§f: Whatever...§r").build()
        );
    }

    @Override
    public List<HealthData> getHealths() {
        List<HealthData> healths = new ArrayList<HealthData>();
        {
            long health = 0;
            if (scarfStand != null) {
                String name = TextUtils.stripColor(scarfStand.getName());
                String healthPart = name.split(" ")[2];
                health = TextUtils.reverseFormat(healthPart.substring(0, healthPart.length() - 1));
            }
            healths.add(new HealthData("Scarf", (int) health, 1000000, this.getCurrentPhase().equals("fight-2")));
        }
        if (!getCurrentPhase().equals("start") && !getCurrentPhase().equals("final-defeat")) {
            {
                long health = 0;
                if (priestStand != null) {
                    String name = TextUtils.stripColor(priestStand.getName());
                    String healthPart = name.split(" ")[2];
                    health = TextUtils.reverseFormat(healthPart.substring(0, healthPart.length() - 1));
                }
                healths.add(new HealthData("Undead Priest", (int) health, 600000, this.getCurrentPhase().startsWith("fight-")));
            }
            {
                long health = 0;
                if (berserkStand != null) {
                    String name = TextUtils.stripColor(berserkStand.getName());
                    String healthPart = name.split(" ")[2];
                    health = TextUtils.reverseFormat(healthPart.substring(0, healthPart.length() - 1));
                }
                healths.add(new HealthData("Undead Warrior", (int) health, 500000, this.getCurrentPhase().startsWith("fight-")));
            }
            {
                long health = 0;
                if (mageStand != null) {
                    String name = TextUtils.stripColor(mageStand.getName());
                    String healthPart = name.split(" ")[2];
                    health = TextUtils.reverseFormat(healthPart.substring(0, healthPart.length() - 1));
                }
                healths.add(new HealthData("Undead Mage", (int) health, 400000, this.getCurrentPhase().startsWith("fight-")));
            }
            {
                long health = 0;
                if (archerStand != null) {
                    String name = TextUtils.stripColor(archerStand.getName());
                    String healthPart = name.split(" ")[2];
                    health = TextUtils.reverseFormat(healthPart.substring(0, healthPart.length() - 1));
                }
                healths.add(new HealthData("Undead Archer", (int) health, 400000, this.getCurrentPhase().startsWith("fight-")));
            }
        }
        return healths;
    }

    @Override
    public String getBossName() {
        return "Scarf";
    }

    private EntityArmorStand scarfStand;
    private EntityArmorStand priestStand;
    private EntityArmorStand mageStand;
    private EntityArmorStand berserkStand;
    private EntityArmorStand archerStand;
    @Override
    public void onEntityUpdate(LivingEvent.LivingUpdateEvent updateEvent) {
        if (updateEvent.entityLiving instanceof EntityArmorStand) {
            if (updateEvent.entityLiving.getName().startsWith("§e﴾ §c§lScarf§r "))
                scarfStand = (EntityArmorStand) updateEvent.entityLiving;
            else if (updateEvent.entityLiving.getName().startsWith("§6§4§lUndead Archer "))
                archerStand = (EntityArmorStand) updateEvent.entityLiving;
            else if (updateEvent.entityLiving.getName().startsWith("§6§4§lUndead Mage "))
                mageStand = (EntityArmorStand) updateEvent.entityLiving;
            else if (updateEvent.entityLiving.getName().startsWith("§6§4§lUndead Priest "))
                priestStand = (EntityArmorStand) updateEvent.entityLiving;
            else if (updateEvent.entityLiving.getName().startsWith("§6§4§lUndead Warrior "))
                berserkStand = (EntityArmorStand) updateEvent.entityLiving;
        }
    }
}
