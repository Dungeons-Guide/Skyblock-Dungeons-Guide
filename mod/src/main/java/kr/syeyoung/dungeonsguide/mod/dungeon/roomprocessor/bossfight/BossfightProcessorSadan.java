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
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraftforge.event.entity.living.LivingEvent;

import java.util.ArrayList;
import java.util.List;

public class BossfightProcessorSadan extends GeneralBossfightProcessor {
    public BossfightProcessorSadan() {
        addPhase(PhaseData.builder()
                .phase("start")
                .signatureMsg("So you made it all the way §r§fhere...and§r§f you wish to defy me? Sadan?!§r")
                .nextPhase("fight-1").build()
        );
        addPhase(PhaseData.builder()
                .phase("fight-1")
                .signatureMsg("§r§c[BOSS] Sadan §r§f: I am the bridge between this realm and the world below! You shall not pass!§r")
                .nextPhase("first-defeat").build()
        );
        addPhase(PhaseData.builder()
                .phase("first-defeat")
                .signatureMsg("§r§c[BOSS] Sadan §r§f: ENOUGH!§r")
                .nextPhase("fight-2").build()
        );
        addPhase(PhaseData.builder()
                .phase("fight-2")
                .signatureMsg("§r§c[BOSS] Sadan §r§f: My giants! Unleashed!§r")
                .nextPhase("second-defeat").build()
        );
        addPhase(PhaseData.builder()
                .phase("second-defeat")
                .signatureMsg("§r§c[BOSS] Sadan §r§f: You did it. I understand now, you have earned my respect.§r")
                .nextPhase("fight-3").build()
        );
        addPhase(PhaseData.builder()
                .phase("fight-3")
                .signatureMsg("§r§c[BOSS] Sadan §r§f: I'm sorry but I need to concentrate. I wish it didn't have to come to this.§r")
                .nextPhase("final-defeat").build()
        );
        addPhase(PhaseData.builder()
                .phase("final-defeat")
                .signatureMsg("§r§c[BOSS] Sadan §r§f: NOOOOOOOOO!!! THIS IS IMPOSSIBLE!!§r").build()
        );
    }

    @Override
    public List<HealthData> getHealths() {
        List<HealthData> healths = new ArrayList<HealthData>();
        {
            long health = 0;
            if (sadanStand != null) {
                String name = TextUtils.stripColor(sadanStand.getName());
                String healthPart = name.split(" ")[2];
                health = TextUtils.reverseFormat(healthPart.substring(0, healthPart.length() - 1));
            }
            healths.add(new HealthData("Sadan", (int) health, 40000000, this.getCurrentPhase().equals("fight-3")));
        }
        if (getCurrentPhase().equals("fight-2")) {
            {
                long health = 0;
                if (diamondGiant != null) {
                    String name = TextUtils.stripColor(diamondGiant.getName());
                    String healthPart = name.split(" ")[3];
                    health = TextUtils.reverseFormat(healthPart.substring(0, healthPart.length() - 1));
                }
                healths.add(new HealthData("The Diamond Giant", (int) health, 25000000, this.getCurrentPhase().startsWith("fight-")));
            }
            {
                long health = 0;
                if (bigfootGiant != null) {
                    String name = TextUtils.stripColor(bigfootGiant.getName());
                    String healthPart = name.split(" ")[1];
                    health = TextUtils.reverseFormat(healthPart.substring(0, healthPart.length() - 1));
                }
                healths.add(new HealthData("Bigfoot", (int) health, 25000000, this.getCurrentPhase().startsWith("fight-")));
            }
            {
                long health = 0;
                if (laserGiant != null) {
                    String name = TextUtils.stripColor(laserGiant.getName());
                    String healthPart = name.split(" ")[1];
                    health = TextUtils.reverseFormat(healthPart.substring(0, healthPart.length() - 1));
                }
                healths.add(new HealthData("L.A.S.R.", (int) health, 25000000, this.getCurrentPhase().startsWith("fight-")));
            }
            {
                long health = 0;
                if (boulderGiant != null) {
                    String name = TextUtils.stripColor(boulderGiant.getName());
                    String healthPart = name.split(" ")[3];
                    health = TextUtils.reverseFormat(healthPart.substring(0, healthPart.length() - 1));
                }
                healths.add(new HealthData("Jolly Pink Giant", (int) health, 25000000, this.getCurrentPhase().startsWith("fight-")));
            }
        }
        return healths;
    }

    @Override
    public String getBossName() {
        return "Sadan";
    }

    private EntityArmorStand sadanStand;
    private EntityArmorStand diamondGiant;
    private EntityArmorStand laserGiant;
    private EntityArmorStand bigfootGiant;
    private EntityArmorStand boulderGiant;
    @Override
    public void onEntityUpdate(LivingEvent.LivingUpdateEvent updateEvent) {
        if (updateEvent.entityLiving instanceof EntityArmorStand) {
            if (updateEvent.entityLiving.getName().startsWith("§e﴾ §c§lSadan§r "))
                sadanStand = (EntityArmorStand) updateEvent.entityLiving;
            else if (updateEvent.entityLiving.getName().startsWith("§c§d§lJolly Pink Giant "))
                boulderGiant = (EntityArmorStand) updateEvent.entityLiving;
            else if (updateEvent.entityLiving.getName().startsWith("§c§4§lL.A.S.R. "))
                laserGiant = (EntityArmorStand) updateEvent.entityLiving;
            else if (updateEvent.entityLiving.getName().startsWith("§c§3§lThe Diamond Giant "))
                diamondGiant = (EntityArmorStand) updateEvent.entityLiving;
            else if (updateEvent.entityLiving.getName().startsWith("§c§c§lBigfoot "))
                bigfootGiant = (EntityArmorStand) updateEvent.entityLiving;
        }
    }
}