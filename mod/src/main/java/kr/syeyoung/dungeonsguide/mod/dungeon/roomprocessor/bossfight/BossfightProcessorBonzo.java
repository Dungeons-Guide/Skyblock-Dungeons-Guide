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
import kr.syeyoung.modapi.entity.UEntity;
import kr.syeyoung.modapi.entity.UEntityArmorStand;
import kr.syeyoung.modapi.entity.UEntityPlayer;
import kr.syeyoung.modapi.event.events.LivingEntityTickEvent;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class BossfightProcessorBonzo extends GeneralBossfightProcessor {
    public BossfightProcessorBonzo(boolean isMasterMode) {
        super(isMasterMode ? "MASTERMODE_CATACOMBS_FLOOR_ONE" : "CATACOMBS_FLOOR_ONE");
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("start")
                .signatureMsg("§c[BOSS] Bonzo §f: Gratz for making it this far, but I’m basically unbeatable.")
                .nextPhase("fight-1").build()
        );

        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("fight-1")
                .signatureMsg("§c[BOSS] Bonzo §f: I can summon lots of undead! Check this out.")
                .nextPhase("first-defeat").build()
        );
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("first-defeat")
                .signatureMsg("§c[BOSS] Bonzo §f: Oh I'm dead!").signatureMsg("§c[BOSS] Bonzo §f: Hoho, looks like you killed me!")
                .nextPhase("fight-2").build()
        );
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("fight-2")
                .signatureMsg("§c[BOSS] Bonzo §f: Sike").signatureMsg("§c[BOSS] Bonzo §f: I can revive myself and become much stronger!")
                .nextPhase("final-defeat").build()
        );
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("final-defeat")
                .signatureMsg("§c[BOSS] Bonzo §f: Alright, maybe I'm just weak after all..").build()
        );
        this.isMasterMode = isMasterMode;
    }
    private boolean isMasterMode;

    @Override
    public List<HealthData> getHealths() {
        List<HealthData> healths = new ArrayList<HealthData>();
        long health = 0;
        if (bonzoStand != null) {
            String name = TextUtils.stripColor(bonzoStand.getName());
            String healthPart = name.split(" ")[2];
            health = TextUtils.reverseFormat(healthPart.substring(0, healthPart.length() - 1));
        }
        healths.add(new HealthData("Bonzo", (int) health,isMasterMode? 200_000_000: 250000 , this.getCurrentPhase().startsWith("fight-")));
        return healths;
    }

    @Override
    public String getBossName() {
        return "Bonzo";
    }

    private UEntityArmorStand bonzoStand;


    private static final ResourceLocation UNDEAD = new ResourceLocation("dungeonsguide:map/bossfight/f1/undead.png");
    private static final ResourceLocation BONZO = new ResourceLocation("dungeonsguide:map/bossfight/f1/bonzo.png");

    @Override
    public MarkerData convertToMarker(UEntity entity) {
        if (entity instanceof UEntityPlayer) {
            if ("Undead ".equals(entity.getName())) {
                return MarkerData.fromEntity(entity, MarkerData.MobType.ENEMIES, 4);
            } else if ("Bonzo ".equals(entity.getName())){
                return MarkerData.fromEntity(entity, MarkerData.MobType.ENEMIES, Arrays.asList("start", "fight-1", "first-defeat").contains(getCurrentPhase()) ? 0 : 1);
            }
        }
        return null;
    }

    @Override
    // §e﴾ §c§lBonzo§r §e71k§c❤ §e﴿
    // §e﴾ §c§lBonzo§r §a250k§c❤ §e﴿
    // Now I'm convinced name format is always the same
    public void onEntityUpdate(LivingEntityTickEvent updateEvent) {
        if (updateEvent.getEntityLiving().getName().startsWith("§e﴾ §c§lBonzo§r") && updateEvent.getEntityLiving() instanceof UEntityArmorStand) {
            bonzoStand = (UEntityArmorStand) updateEvent.getEntityLiving();
        }
    }
}
