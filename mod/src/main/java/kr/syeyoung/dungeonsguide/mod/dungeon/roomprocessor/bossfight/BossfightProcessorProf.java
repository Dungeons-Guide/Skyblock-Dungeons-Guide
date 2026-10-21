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

import kr.syeyoung.dungeonsguide.mod.events.impl.LivingTickEvent;
import com.mojang.authlib.properties.Property;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.phys.Vec3;

import java.util.*;

// TODO: D5 entity name stopgap (D116), the getFormattedText(entity.getName()) reads
public class BossfightProcessorProf extends GeneralBossfightProcessor {
    public BossfightProcessorProf(boolean isMasterMode) {
        super(isMasterMode ? "MASTERMODE_CATACOMBS_FLOOR_THREE" : "CATACOMBS_FLOOR_THREE");
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("start")
                .signatureMsg("§r§c[BOSS] The Professor§r§f: I was burdened with terrible news recently...§r")
                .nextPhase("fight-1").build()
        );
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("fight-1")
                .signatureMsg("§r§c[BOSS] The Professor§r§f: I'll show you real power!§r")
                .nextPhase("first-defeat").build()
        );
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("first-defeat")
                .signatureMsg("§r§c[BOSS] The Professor§r§f: Oh? You found my Guardians' one weakness?§r")
                .nextPhase("fight-2").build()
        );
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("fight-2")
                .signatureMsg("§r§c[BOSS] The Professor§r§f: This time I'll be your opponent!§r")
                .nextPhase("second-defeat").build()
        );
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("second-defeat")
                .signatureMsg("§r§c[BOSS] The Professor§r§f: I see. You have forced me to use my ultimate technique.§r")
                .nextPhase("fight-3").build()
        );
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("fight-3")
                .signatureMsg("§r§c[BOSS] The Professor§r§f: The process is irreversible, but I'll be stronger than a Wither now!§r")
                .nextPhase("final-defeat").build()
        );
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("final-defeat")
                .signatureMsg("§r§c[BOSS] The Professor§r§f: What?! My Guardian power is unbeatable!§r").build()
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
                String name = TextUtils.stripColor(TextUtils.getFormattedText(profStand.getName()));
                String healthPart = name.split(" ")[3];
                health = TextUtils.reverseFormat(healthPart.substring(0, healthPart.length() - 1));
            }
            healths.add(new HealthData("The Professor", (int) health, isMasterMode ? 600000000 :3000000, this.getCurrentPhase().startsWith("fight-") && !this.getCurrentPhase().equals("fight-1")));
        }
        if (!getCurrentPhase().equals("second-defeat") && !getCurrentPhase().equals("fight-3") && !getCurrentPhase().equals("final-defeat")) {
            {
                long health = 0;
                if (healthyGuard != null) {
                    String name = TextUtils.stripColor(TextUtils.getFormattedText(healthyGuard.getName()));
                    String healthPart = name.split(" ")[2];
                    health = TextUtils.reverseFormat(healthPart.substring(0, healthPart.length() - 1));
                }
                healths.add(new HealthData("Healthy Guardian", (int) health, isMasterMode ? 120000000 : 1000000, this.getCurrentPhase().equals("fight-1")));
            }
            {
                long health = 0;
                if (chaosGuard != null) {
                    String name = TextUtils.stripColor(TextUtils.getFormattedText(chaosGuard.getName()));
                    String healthPart = name.split(" ")[2];
                    health = TextUtils.reverseFormat(healthPart.substring(0, healthPart.length() - 1));
                }
                healths.add(new HealthData("Chaos Guardian", (int) health, isMasterMode ? 120000000 : 1000000, this.getCurrentPhase().equals("fight-1")));
            }
            {
                long health = 0;
                if (laserGuard != null) {
                    String name = TextUtils.stripColor(TextUtils.getFormattedText(laserGuard.getName()));
                    String healthPart = name.split(" ")[2];
                    health = TextUtils.reverseFormat(healthPart.substring(0, healthPart.length() - 1));
                }
                healths.add(new HealthData("Laser Guardian", (int) health, isMasterMode ? 120000000 : 1000000, this.getCurrentPhase().equals("fight-1")));
            }
            {
                long health = 0;
                if (reinforcedGuard != null) {
                    String name = TextUtils.stripColor(TextUtils.getFormattedText(reinforcedGuard.getName()));
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

    private ArmorStand profStand;
    private ArmorStand laserGuard;
    private ArmorStand chaosGuard;
    private ArmorStand reinforcedGuard;
    private ArmorStand healthyGuard;
    @Override
    public void onEntityUpdate(LivingTickEvent updateEvent) {
        if (updateEvent.entityLiving instanceof ArmorStand) {
            if (TextUtils.getFormattedText(updateEvent.entityLiving.getName()).startsWith("§e﴾ §c§lThe Professor§r"))
                profStand = (ArmorStand) updateEvent.entityLiving;
            else if (TextUtils.getFormattedText(updateEvent.entityLiving.getName()).startsWith("§cHealthy Guardian"))
                healthyGuard = (ArmorStand) updateEvent.entityLiving;
            else if (TextUtils.getFormattedText(updateEvent.entityLiving.getName()).startsWith("§cChaos Guardian"))
                chaosGuard = (ArmorStand) updateEvent.entityLiving;
            else if (TextUtils.getFormattedText(updateEvent.entityLiving.getName()).startsWith("§cLaser Guardian"))
                laserGuard = (ArmorStand) updateEvent.entityLiving;
            else if (TextUtils.getFormattedText(updateEvent.entityLiving.getName()).startsWith("§cReinforced Guardian"))
                reinforcedGuard = (ArmorStand) updateEvent.entityLiving;
        }
        if (updateEvent.entityLiving instanceof Guardian) {
            boolean xB = Math.abs(updateEvent.entityLiving.getX() - 14.5) < 0.01;
            boolean xS = Math.abs(updateEvent.entityLiving.getX() - -11.5) < 0.01;
            boolean zB = Math.abs(updateEvent.entityLiving.getZ() - 14.5) < 0.01;
            boolean zS = Math.abs(updateEvent.entityLiving.getZ() - -11.5) < 0.01;
            boolean yE = Math.abs(updateEvent.entityLiving.getY() - 72.5) < 0.01;

            if (getCurrentPhase().equals("fight-3")) {
                if (profStand != null && profStand.blockPosition().distSqr(updateEvent.entityLiving.blockPosition().offset(0, 2, 0)) < 5) {
                    mapping.put(updateEvent.entityLiving.getId(), 23);
                }
            }

            if (yE && xB && zB) {
                mapping.put(updateEvent.entityLiving.getId(), 18);
            } else if (yE && xB && zS) {
                mapping.put(updateEvent.entityLiving.getId(), 19);
            } else if (yE && xS && zB) {
                mapping.put(updateEvent.entityLiving.getId(), 17);
            } else if (yE && xS && zS) {
                mapping.put(updateEvent.entityLiving.getId(), 20);
            } else if (!mapping.containsKey(updateEvent.entityLiving.getId())) {
                mapping.put(updateEvent.entityLiving.getId(), updateEvent.entityLiving instanceof ElderGuardian ? 21 : 22);
            }
        }
    }


    private Map<Integer, Integer> mapping = new HashMap<>();

    @Override
    public MarkerData convertToMarker(Entity entity) {
        if (entity instanceof RemotePlayer) {
            if ("The Professor".equals(TextUtils.getFormattedText(entity.getName()))) {
                return MarkerData.fromEntity(entity, MarkerData.MobType.BOSS, 16);
            }
        } else if (entity instanceof Guardian) {
            if (entity.isInvisible()) return null;
            Integer val = mapping.get(entity.getId());
            if (val == null) return null;
            return MarkerData.fromEntity(entity, val == 23 ? MarkerData.MobType.BOSS : val > 20 ? MarkerData.MobType.ENEMIES : MarkerData.MobType.MINIBOSS, val); // Rogue
        }
        return null;
    }
}
