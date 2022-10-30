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

package kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bossfight;

import net.minecraft.entity.boss.BossStatus;

import java.util.ArrayList;
import java.util.List;

public class BossfightProcessorNecron extends GeneralBossfightProcessor {
    // \A7 to §
    public BossfightProcessorNecron() {
        addPhase(PhaseData.builder()
                .phase("crystals")
                .signatureMsg("§r§4[BOSS] Necron§r§c: §r§cFinally, I heard so much about you. The Eye likes you very much.§r")
                .nextPhase("laser-attack").nextPhase("lost").build()
        );
        addPhase(PhaseData.builder()
                .phase("laser-attack")
                .signatureMsg("§r§4[BOSS] Necron§r§c: §r§cYou tricked me!§r")
                .nextPhase("fight-1").nextPhase("lost").build()
        );
        addPhase(PhaseData.builder()
                .phase("fight-1")
                .signatureMsg("§r§4[BOSS] Necron§r§c: §r§cFINE! LET'S MOVE TO SOMEWHERE ELSE!!§r")
                .nextPhase("terminals").nextPhase("lost").build()
        );
        addPhase(PhaseData.builder()
                .phase("terminals")
                .signatureMsg("§r§4[BOSS] Necron§r§c: §r§cCRAP!! IT BROKE THE FLOOR!§r")
                .nextPhase("fight-2").nextPhase("lost").build()
        );
        addPhase(PhaseData.builder()
                .phase("fight-2")
                .signatureMsg("§r§4[BOSS] Necron§r§c: §r§cTHAT'S IT YOU HAVE DONE IT!§r")
                .nextPhase("won").nextPhase("lost").build()
        );
        addPhase(PhaseData.builder()
                .phase("won")
                .signatureMsg("§r§4[BOSS] Necron§r§c: §r§cAll this, for nothing...§r").build()
        );
        addPhase(PhaseData.builder()
                .phase("lost")
                .signatureMsg("§r§4[BOSS] Necron§r§c: §r§cFINALLY! This took way too long.§r").build()
        );
    }


    @Override
    public List<HealthData> getHealths() {
        List<HealthData> healths = new ArrayList<HealthData>();
        int maxHealth = 1_000_000_000;
        healths.add(new HealthData("Necron", (int) (BossStatus.healthScale * maxHealth),maxHealth , this.getCurrentPhase().startsWith("fight-")));
        return healths;
    }

    @Override
    public String getBossName() {
        return "Necron";
    }
}
