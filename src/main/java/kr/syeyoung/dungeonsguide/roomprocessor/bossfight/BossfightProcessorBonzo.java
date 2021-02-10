package kr.syeyoung.dungeonsguide.roomprocessor.bossfight;

import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraftforge.event.entity.living.LivingEvent;

import java.util.ArrayList;
import java.util.List;

public class BossfightProcessorBonzo extends GeneralBossfightProcessor {
    public BossfightProcessorBonzo() {
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("start")
                .signatureMsg("§r§c[BOSS] Bonzo§r§f: Gratz for making it this far, but I’m basically unbeatable.§r")
                .nextPhase("fight-1").build()
        );
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("fight-1")
                .signatureMsg("§r§c[BOSS] Bonzo§r§f: I can summon lots of undead! Check this out.§r")
                .nextPhase("first-defeat").build()
        );
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("first-defeat")
                .signatureMsg("§r§c[BOSS] Bonzo§r§f: Oh I'm dead!§r").signatureMsg("§r§c[BOSS] Bonzo§r§f: Hoho, looks like you killed me!§r")
                .nextPhase("fight-2").build()
        );
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("fight-2")
                .signatureMsg("§r§c[BOSS] Bonzo§r§f: Sike§r").signatureMsg("§r§c[BOSS] Bonzo§r§f: I can revive myself and become much stronger!§r")
                .nextPhase("final-defeat").build()
        );
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("final-defeat")
                .signatureMsg("§r§c[BOSS] Bonzo§r§f: Alright, maybe I'm just weak after all..§r").build()
        );
    }


    @Override
    public List<HealthData> getHealths() {
        List<HealthData> healths = new ArrayList<HealthData>();
        long health = 0;
        if (bonzoStand != null) {
            String name = TextUtils.stripColor(bonzoStand.getName());
            String healthPart = name.split(" ")[2];
            health = TextUtils.reverseFormat(healthPart.substring(0, healthPart.length() - 1));
        }
        healths.add(new HealthData("Bonzo", (int) health,250000 , this.getCurrentPhase().startsWith("fight-")));
        return healths;
    }

    private EntityArmorStand bonzoStand;
    @Override
    // §e﴾ §c§lBonzo§r §e71k§c❤ §e﴿
    // §e﴾ §c§lBonzo§r §a250k§c❤ §e﴿
    public void onEntityUpdate(LivingEvent.LivingUpdateEvent updateEvent) {
        if (updateEvent.entityLiving.getName().startsWith("§e﴾ §c§lBonzo§r ") && updateEvent.entityLiving instanceof EntityArmorStand) {
            bonzoStand = (EntityArmorStand) updateEvent.entityLiving;
        }
    }
}
