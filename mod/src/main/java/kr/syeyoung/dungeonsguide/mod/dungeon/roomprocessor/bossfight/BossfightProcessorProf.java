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
import kr.syeyoung.modapi.entity.*;
import kr.syeyoung.modapi.event.events.LivingEntityTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BossfightProcessorProf extends GeneralBossfightProcessor {
    public BossfightProcessorProf(boolean isMasterMode) {
        super(isMasterMode ? "MASTERMODE_CATACOMBS_FLOOR_THREE" : "CATACOMBS_FLOOR_THREE");
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("start")
                .signatureMsg("§c[BOSS] The Professor§f: I was burdened with terrible news recently...")
                .nextPhase("fight-1").build()
        );
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("fight-1")
                .signatureMsg("§c[BOSS] The Professor§f: I'll show you real power!")
                .nextPhase("first-defeat").build()
        );
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("first-defeat")
                .signatureMsg("§c[BOSS] The Professor§f: Oh? You found my Guardians' one weakness?")
                .nextPhase("fight-2").build()
        );
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("fight-2")
                .signatureMsg("§c[BOSS] The Professor§f: This time I'll be your opponent!")
                .nextPhase("second-defeat").build()
        );
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("second-defeat")
                .signatureMsg("§c[BOSS] The Professor§f: I see. You have forced me to use my ultimate technique.")
                .nextPhase("fight-3").build()
        );
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("fight-3")
                .signatureMsg("§c[BOSS] The Professor§f: The process is irreversible, but I'll be stronger than a Wither now!")
                .nextPhase("final-defeat").build()
        );
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("final-defeat")
                .signatureMsg("§c[BOSS] The Professor§f: What?! My Guardian power is unbeatable!").build()
        );
        this.isMasterMode = isMasterMode;
    }

    private boolean isMasterMode;
    @Override
    public List<HealthData> getHealths() {
        List<HealthData> healths = new ArrayList<HealthData>();
        {
            long health = 0;
            if (profStand != null) {
                String name = TextUtils.stripColor(profStand.getName());
                String healthPart = name.split(" ")[3];
                health = TextUtils.reverseFormat(healthPart.substring(0, healthPart.length() - 1));
            }
            healths.add(new HealthData("The Professor", (int) health, isMasterMode ? 600000000 :3000000, this.getCurrentPhase().startsWith("fight-") && !this.getCurrentPhase().equals("fight-1")));
        }
        if (!getCurrentPhase().equals("second-defeat") && !getCurrentPhase().equals("fight-3") && !getCurrentPhase().equals("final-defeat")) {
            {
                long health = 0;
                if (healthyGuard != null) {
                    String name = TextUtils.stripColor(healthyGuard.getName());
                    String healthPart = name.split(" ")[2];
                    health = TextUtils.reverseFormat(healthPart.substring(0, healthPart.length() - 1));
                }
                healths.add(new HealthData("Healthy Guardian", (int) health, isMasterMode ? 120000000 : 1000000, this.getCurrentPhase().equals("fight-1")));
            }
            {
                long health = 0;
                if (chaosGuard != null) {
                    String name = TextUtils.stripColor(chaosGuard.getName());
                    String healthPart = name.split(" ")[2];
                    health = TextUtils.reverseFormat(healthPart.substring(0, healthPart.length() - 1));
                }
                healths.add(new HealthData("Chaos Guardian", (int) health, isMasterMode ? 120000000 : 1000000, this.getCurrentPhase().equals("fight-1")));
            }
            {
                long health = 0;
                if (laserGuard != null) {
                    String name = TextUtils.stripColor(laserGuard.getName());
                    String healthPart = name.split(" ")[2];
                    health = TextUtils.reverseFormat(healthPart.substring(0, healthPart.length() - 1));
                }
                healths.add(new HealthData("Laser Guardian", (int) health, isMasterMode ? 120000000 : 1000000, this.getCurrentPhase().equals("fight-1")));
            }
            {
                long health = 0;
                if (reinforcedGuard != null) {
                    String name = TextUtils.stripColor(reinforcedGuard.getName());
                    String healthPart = name.split(" ")[2];
                    health = TextUtils.reverseFormat(healthPart.substring(0, healthPart.length() - 1));
                }
                healths.add(new HealthData("Reinforced Guardian", (int) health, isMasterMode ? 140000000 : 1000000, this.getCurrentPhase().equals("fight-1")));
            }
        }
        return healths;
    }

    @Override
    public String getBossName() {
        return "The Professor";
    }

    private UEntityArmorStand profStand;
    private UEntityArmorStand laserGuard;
    private UEntityArmorStand chaosGuard;
    private UEntityArmorStand reinforcedGuard;
    private UEntityArmorStand healthyGuard;
    @Override
    public void onEntityUpdate(LivingEntityTickEvent updateEvent) {
        if (updateEvent.getEntityLiving() instanceof UEntityArmorStand) {
            if (updateEvent.getEntityLiving().getName().startsWith("§e﴾ §c§lThe Professor§r"))
                profStand = (UEntityArmorStand) updateEvent.getEntityLiving();
            else if (updateEvent.getEntityLiving().getName().startsWith("§cHealthy Guardian"))
                healthyGuard = (UEntityArmorStand) updateEvent.getEntityLiving();
            else if (updateEvent.getEntityLiving().getName().startsWith("§cChaos Guardian"))
                chaosGuard = (UEntityArmorStand) updateEvent.getEntityLiving();
            else if (updateEvent.getEntityLiving().getName().startsWith("§cLaser Guardian"))
                laserGuard = (UEntityArmorStand) updateEvent.getEntityLiving();
            else if (updateEvent.getEntityLiving().getName().startsWith("§cReinforced Guardian"))
                reinforcedGuard = (UEntityArmorStand) updateEvent.getEntityLiving();
        }
        if (updateEvent.getEntityLiving().getEntityType() == EntityType.GUARDIAN) {
            boolean xB = Math.abs(updateEvent.getEntityLiving().getPosX() - 14.5) < 0.01;
            boolean xS = Math.abs(updateEvent.getEntityLiving().getPosX() - -11.5) < 0.01;
            boolean zB = Math.abs(updateEvent.getEntityLiving().getPosZ() - 14.5) < 0.01;
            boolean zS = Math.abs(updateEvent.getEntityLiving().getPosZ() - -11.5) < 0.01;
            boolean yE = Math.abs(updateEvent.getEntityLiving().getPosY() - 72.5) < 0.01;

            if (getCurrentPhase().equals("fight-3")) {
                if (profStand != null && profStand.getPositionVector().distanceSq(updateEvent.getEntityLiving().getPositionVector().add(0, 2, 0)) < 5) {
                    mapping.put(updateEvent.getEntityLiving().getEntityId(), 23);
                }
            }

            if (yE && xB && zB) {
                mapping.put(updateEvent.getEntityLiving().getEntityId(), 18);
            } else if (yE && xB && zS) {
                mapping.put(updateEvent.getEntityLiving().getEntityId(), 19);
            } else if (yE && xS && zB) {
                mapping.put(updateEvent.getEntityLiving().getEntityId(), 17);
            } else if (yE && xS && zS) {
                mapping.put(updateEvent.getEntityLiving().getEntityId(), 20);
            } else if (!mapping.containsKey(updateEvent.getEntityLiving().getEntityId())) {
                mapping.put(updateEvent.getEntityLiving().getEntityId(), ((UEntityGuardian) updateEvent.getEntityLiving()).isElder() ? 21 : 22);
            }
        }
    }


    private Map<Integer, Integer> mapping = new HashMap<>();

    @Override
    public MarkerData convertToMarker(UEntity entity) {
        if (entity instanceof UEntityPlayer) {
            if ("The Professor".equals(entity.getName())) {
                return MarkerData.fromEntity(entity, MarkerData.MobType.BOSS, 16);
            }
        } else if (entity.getEntityType() == EntityType.GUARDIAN) {
            if (entity.isInvisible()) return null;
            Integer val = mapping.get(entity.getEntityId());
            if (val == null) return null;
            return MarkerData.fromEntity(entity, val == 23 ? MarkerData.MobType.BOSS : val > 20 ? MarkerData.MobType.ENEMIES : MarkerData.MobType.MINIBOSS, val); // Rogue
        }
        return null;
    }
}
