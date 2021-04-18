package kr.syeyoung.dungeonsguide.roomprocessor.bossfight;

import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraftforge.event.entity.living.LivingEvent;

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
