package kr.syeyoung.dungeonsguide.roomprocessor.bossfight;

import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraftforge.event.entity.living.LivingEvent;

import java.util.ArrayList;
import java.util.List;

public class BossfightProcessorProf extends GeneralBossfightProcessor {
    public BossfightProcessorProf() {
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("start")
                .signatureMsg("§r§c[BOSS] The Professor §r§f: I was burdened with terrible news recently...§r")
                .nextPhase("fight-1").build()
        );
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("fight-1")
                .signatureMsg("§r§c[BOSS] The Professor §r§f: I'll show you real power!§r")
                .nextPhase("first-defeat").build()
        );
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("first-defeat")
                .signatureMsg("§r§c[BOSS] The Professor §r§f: Oh? You found my Guardians one weakness?§r")
                .nextPhase("fight-2").build()
        );
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("fight-2")
                .signatureMsg("§r§c[BOSS] The Professor §r§f: This time I'll be your opponent!§r")
                .nextPhase("second-defeat").build()
        );
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("second-defeat")
                .signatureMsg("§r§c[BOSS] The Professor §r§f: I see. You have forced me to use my ultimate technique.§r")
                .nextPhase("fight-3").build()
        );
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("fight-3")
                .signatureMsg("§r§c[BOSS] The Professor §r§f: The process is irreversible, but I'll be stronger than a Wither now!§r")
                .nextPhase("final-defeat").build()
        );
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("final-defeat")
                .signatureMsg("§r§c[BOSS] The Professor §r§f: What?! My Guardian power is unbeatable!§r").build()
        );
    }

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
            healths.add(new HealthData("The Professor", (int) health, 3000000, this.getCurrentPhase().startsWith("fight-") && !this.getCurrentPhase().equals("fight-1")));
        }
        if (!getCurrentPhase().equals("second-defeat") && !getCurrentPhase().equals("fight-3") && !getCurrentPhase().equals("final-defeat")) {
            {
                long health = 0;
                if (healthyGuard != null) {
                    String name = TextUtils.stripColor(healthyGuard.getName());
                    String healthPart = name.split(" ")[2];
                    health = TextUtils.reverseFormat(healthPart.substring(0, healthPart.length() - 1));
                }
                healths.add(new HealthData("Healthy Guardian", (int) health, 1000000, this.getCurrentPhase().equals("fight-1")));
            }
            {
                long health = 0;
                if (chaosGuard != null) {
                    String name = TextUtils.stripColor(chaosGuard.getName());
                    String healthPart = name.split(" ")[2];
                    health = TextUtils.reverseFormat(healthPart.substring(0, healthPart.length() - 1));
                }
                healths.add(new HealthData("Chaos Guardian", (int) health, 1000000, this.getCurrentPhase().equals("fight-1")));
            }
            {
                long health = 0;
                if (laserGuard != null) {
                    String name = TextUtils.stripColor(laserGuard.getName());
                    String healthPart = name.split(" ")[2];
                    health = TextUtils.reverseFormat(healthPart.substring(0, healthPart.length() - 1));
                }
                healths.add(new HealthData("Laser Guardian", (int) health, 1000000, this.getCurrentPhase().equals("fight-1")));
            }
            {
                long health = 0;
                if (reinforcedGuard != null) {
                    String name = TextUtils.stripColor(reinforcedGuard.getName());
                    String healthPart = name.split(" ")[2];
                    health = TextUtils.reverseFormat(healthPart.substring(0, healthPart.length() - 1));
                }
                healths.add(new HealthData("Reinforced Guardian", (int) health, 1000000, this.getCurrentPhase().equals("fight-1")));
            }
        }
        return healths;
    }

    private EntityArmorStand profStand;
    private EntityArmorStand laserGuard;
    private EntityArmorStand chaosGuard;
    private EntityArmorStand reinforcedGuard;
    private EntityArmorStand healthyGuard;
    @Override
    public void onEntityUpdate(LivingEvent.LivingUpdateEvent updateEvent) {
        if (updateEvent.entityLiving instanceof EntityArmorStand) {
            if (updateEvent.entityLiving.getName().startsWith("§e﴾ §c§lThe Professor§r "))
                profStand = (EntityArmorStand) updateEvent.entityLiving;
            else if (updateEvent.entityLiving.getName().startsWith("§cHealthy Guardian "))
                healthyGuard = (EntityArmorStand) updateEvent.entityLiving;
            else if (updateEvent.entityLiving.getName().startsWith("§cChaos Guardian "))
                chaosGuard = (EntityArmorStand) updateEvent.entityLiving;
            else if (updateEvent.entityLiving.getName().startsWith("§cLaser Guardian "))
                laserGuard = (EntityArmorStand) updateEvent.entityLiving;
            else if (updateEvent.entityLiving.getName().startsWith("§cReinforced Guardian "))
                reinforcedGuard = (EntityArmorStand) updateEvent.entityLiving;
        }
    }
}
