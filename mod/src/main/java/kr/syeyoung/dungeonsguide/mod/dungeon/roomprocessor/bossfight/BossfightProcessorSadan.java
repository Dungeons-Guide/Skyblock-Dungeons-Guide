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
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityGiantZombie;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.passive.*;
import net.minecraftforge.event.entity.living.LivingEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BossfightProcessorSadan extends GeneralBossfightProcessor {
    public BossfightProcessorSadan() {
        super("CATACOMBS_FLOOR_SIX");
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
                healths.add(new HealthData("The Diamond Giant", (int) health, 25000000, this.getCurrentPhase().equals("fight-2")));
            }
            {
                long health = 0;
                if (bigfootGiant != null) {
                    String name = TextUtils.stripColor(bigfootGiant.getName());
                    String healthPart = name.split(" ")[1];
                    health = TextUtils.reverseFormat(healthPart.substring(0, healthPart.length() - 1));
                }
                healths.add(new HealthData("Bigfoot", (int) health, 25000000, this.getCurrentPhase().equals("fight-2")));
            }
            {
                long health = 0;
                if (laserGiant != null) {
                    String name = TextUtils.stripColor(laserGiant.getName());
                    String healthPart = name.split(" ")[1];
                    health = TextUtils.reverseFormat(healthPart.substring(0, healthPart.length() - 1));
                }
                healths.add(new HealthData("L.A.S.R.", (int) health, 25000000, this.getCurrentPhase().equals("fight-2")));
            }
            {
                long health = 0;
                if (boulderGiant != null) {
                    String name = TextUtils.stripColor(boulderGiant.getName());
                    String healthPart = name.split(" ")[3];
                    health = TextUtils.reverseFormat(healthPart.substring(0, healthPart.length() - 1));
                }
                healths.add(new HealthData("Jolly Pink Giant", (int) health, 25000000, this.getCurrentPhase().equals("fight-2")));
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
            if (updateEvent.entityLiving.getName().startsWith("§e﴾ §c§lSadan§r"))
                sadanStand = (EntityArmorStand) updateEvent.entityLiving;
            else if (updateEvent.entityLiving.getName().startsWith("§c§d§lJolly Pink Giant"))
                boulderGiant = (EntityArmorStand) updateEvent.entityLiving;
            else if (updateEvent.entityLiving.getName().startsWith("§c§4§lL.A.S.R."))
                laserGiant = (EntityArmorStand) updateEvent.entityLiving;
            else if (updateEvent.entityLiving.getName().startsWith("§c§3§lThe Diamond Giant"))
                diamondGiant = (EntityArmorStand) updateEvent.entityLiving;
            else if (updateEvent.entityLiving.getName().startsWith("§c§c§lBigfoot"))
                bigfootGiant = (EntityArmorStand) updateEvent.entityLiving;
        } else if (updateEvent.entityLiving instanceof EntityGiantZombie) {

            if (updateEvent.entityLiving.posY < 55) {
                mapping.put(updateEvent.entityLiving.getEntityId(), 50);
            } else if (Math.abs(updateEvent.entityLiving.posY - 84.0) < 0.01) {
                boolean xS = Math.abs(updateEvent.entityLiving.posX - -16.5) < 0.01;
                boolean xB = Math.abs(updateEvent.entityLiving.posX - -0.5) < 0.01;
                boolean zS = Math.abs(updateEvent.entityLiving.posZ - 53.5) < 0.01;
                boolean zB = Math.abs(updateEvent.entityLiving.posZ - 79.5) < 0.01;


            }
            // lpx
            // fpx

            // -8.5 66.5 54.0
            // -16.5 79.5
            // -0.5 53.5
            // -0.5 79.5
            // -16.5 53.5
        }
    }

    private Map<Integer, Integer> mapping = new HashMap<>();


    @Override
    public MarkerData convertToMarker(Entity entity) {
        if (entity instanceof EntityIronGolem) {
            return MarkerData.fromEntity(entity, MarkerData.MobType.GOLEM, 42);
        } else if (entity instanceof EntityGiantZombie) {
            return MarkerData.fromEntity(entity, MarkerData.MobType.MINIBOSS, 43);
        } else if (entity instanceof EntityOtherPlayerMP) {
            String name = entity.getName();
            if ("Terracotta ".equals(name)) {
                return MarkerData.fromEntity(entity, MarkerData.MobType.TERRACOTA, 41);
            } else if ("Sadan ".equals(name)) {
                return MarkerData.fromEntity(entity, MarkerData.MobType.TERRACOTA, 40);
            }
        }
        return null;
    }
}