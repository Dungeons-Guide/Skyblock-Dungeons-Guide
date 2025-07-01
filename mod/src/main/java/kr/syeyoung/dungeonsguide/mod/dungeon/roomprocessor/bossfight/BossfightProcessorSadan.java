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
import kr.syeyoung.modapi.entity.EntityType;
import kr.syeyoung.modapi.entity.UEntity;
import kr.syeyoung.modapi.entity.UEntityArmorStand;
import kr.syeyoung.modapi.entity.UEntityPlayer;
import kr.syeyoung.modapi.event.events.LivingEntityTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BossfightProcessorSadan extends GeneralBossfightProcessor {
    public BossfightProcessorSadan() {
        super("CATACOMBS_FLOOR_SIX");
        addPhase(PhaseData.builder()
                .phase("start")
                .signatureMsg("So you made it all the way §fhere...and§f you wish to defy me? Sadan?!")
                .nextPhase("fight-1").build()
        );
        addPhase(PhaseData.builder()
                .phase("fight-1")
                .signatureMsg("§c[BOSS] Sadan §f: I am the bridge between this realm and the world below! You shall not pass!")
                .nextPhase("first-defeat").build()
        );
        addPhase(PhaseData.builder()
                .phase("first-defeat")
                .signatureMsg("§c[BOSS] Sadan §f: ENOUGH!")
                .nextPhase("fight-2").build()
        );
        addPhase(PhaseData.builder()
                .phase("fight-2")
                .signatureMsg("§c[BOSS] Sadan §f: My giants! Unleashed!")
                .nextPhase("second-defeat").build()
        );
        addPhase(PhaseData.builder()
                .phase("second-defeat")
                .signatureMsg("§c[BOSS] Sadan §f: You did it. I understand now, you have earned my respect.")
                .nextPhase("fight-3").build()
        );
        addPhase(PhaseData.builder()
                .phase("fight-3")
                .signatureMsg("§c[BOSS] Sadan §f: I'm sorry but I need to concentrate. I wish it didn't have to come to this.")
                .nextPhase("final-defeat").build()
        );
        addPhase(PhaseData.builder()
                .phase("final-defeat")
                .signatureMsg("§c[BOSS] Sadan §f: NOOOOOOOOO!!! THIS IS IMPOSSIBLE!!").build()
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

    private UEntityArmorStand sadanStand;
    private UEntityArmorStand diamondGiant;
    private UEntityArmorStand laserGiant;
    private UEntityArmorStand bigfootGiant;
    private UEntityArmorStand boulderGiant;
    @Override
    public void onEntityUpdate(LivingEntityTickEvent updateEvent) {
        if (updateEvent.getEntityLiving() instanceof UEntityArmorStand) {
            if (updateEvent.getEntityLiving().getName().startsWith("§e﴾ §c§lSadan§r"))
                sadanStand = (UEntityArmorStand) updateEvent.getEntityLiving();
            else if (updateEvent.getEntityLiving().getName().startsWith("§c§d§lJolly Pink Giant"))
                boulderGiant = (UEntityArmorStand) updateEvent.getEntityLiving();
            else if (updateEvent.getEntityLiving().getName().startsWith("§c§4§lL.A.S.R."))
                laserGiant = (UEntityArmorStand) updateEvent.getEntityLiving();
            else if (updateEvent.getEntityLiving().getName().startsWith("§c§3§lThe Diamond Giant"))
                diamondGiant = (UEntityArmorStand) updateEvent.getEntityLiving();
            else if (updateEvent.getEntityLiving().getName().startsWith("§c§c§lBigfoot"))
                bigfootGiant = (UEntityArmorStand) updateEvent.getEntityLiving();
        } else if (updateEvent.getEntityLiving().getEntityType() == EntityType.GIANT) {

            if (updateEvent.getEntityLiving().getPosY() < 55) {
                mapping.put(updateEvent.getEntityLiving().getEntityId(), 50);
            } else if (Math.abs(updateEvent.getEntityLiving().getPosY() - 84.0) < 0.01) {
                boolean xS = Math.abs(updateEvent.getEntityLiving().getPosX() - -16.5) < 0.01;
                boolean xB = Math.abs(updateEvent.getEntityLiving().getPosX() - -0.5) < 0.01;
                boolean zS = Math.abs(updateEvent.getEntityLiving().getPosZ() - 53.5) < 0.01;
                boolean zB = Math.abs(updateEvent.getEntityLiving().getPosZ() - 79.5) < 0.01;

                // 43 44 51 52
                if (xS && zS) {
                    mapping.put(updateEvent.getEntityLiving().getEntityId(), 52); // jolley
                } else if (xS && zB) {
                    mapping.put(updateEvent.getEntityLiving().getEntityId(), 43); // diamond
                } else if (xB && zB) {
                    mapping.put(updateEvent.getEntityLiving().getEntityId(), 51); // big foot
                } else if (xB && zS) {
                    mapping.put(updateEvent.getEntityLiving().getEntityId(), 49); // laser
                }
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
    public MarkerData convertToMarker(UEntity entity) {
        if (entity.getEntityType() == EntityType.IRON_GOLEM) {
            return MarkerData.fromEntity(entity, MarkerData.MobType.GOLEM, Math.abs(entity.getRotationPitch() - 59.0625) < 0.01 ? 42 : 49);
        } else if (entity.getEntityType() == EntityType.GIANT) {
            Integer map = mapping.get(entity.getEntityId());
            if (map == null) return null;
            return MarkerData.fromEntity(entity, MarkerData.MobType.MINIBOSS, map);
        } else if (entity instanceof UEntityPlayer) {
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