package kr.syeyoung.dungeonsguide.roomprocessor.bossfight;

import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraftforge.event.entity.living.LivingEvent;

import java.util.ArrayList;
import java.util.List;

public class BossfightProcessorScarf extends GeneralBossfightProcessor {
    public BossfightProcessorScarf() {
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("start")
                .signatureMsg("§r§c[BOSS] Scarf§r§f: This is where the journey ends for you, Adventurers.§r")
                .nextPhase("fight-1").build()
        );
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("fight-1")
                .signatureMsg("§r§c[BOSS] Scarf§r§f: ARISE, MY CREATIONS!§r")
                .nextPhase("first-defeat").build()
        );
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("first-defeat")
                .signatureMsg("§r§c[BOSS] Scarf§r§f: Those toys are not strong enough I see.§r")
                .nextPhase("fight-2").build()
        );
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("fight-2")
                .signatureMsg("§r§c[BOSS] Scarf§r§f: Did you forget? I was taught by the best! Let's dance.§r")
                .nextPhase("final-defeat").build()
        );
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("final-defeat")
                .signatureMsg("§r§c[BOSS] Scarf§r§f: Whatever...§r").build()
        );
    }


/*§6§4§lUndead Archer §e0§c❤
            §6§4§lUndead Mage §a366k§c❤
            §6§4§lUndead Priest §e180k§c❤
            §6§4§lUndead Warrior §e189k§c❤
            §e﴾ §c§lScarf§r §a1M§c❤ §e﴿*/

    @Override
    public List<HealthData> getHealths() {
        List<HealthData> healths = new ArrayList<HealthData>();
        {
            long health = 0;
            if (scarfStand != null) {
                String name = TextUtils.stripColor(scarfStand.getName());
                String healthPart = name.split(" ")[2];
                health = TextUtils.reverseFormat(healthPart.substring(0, healthPart.length() - 1));
            }
            healths.add(new HealthData("Scarf", (int) health, 1000000, this.getCurrentPhase().equals("fight-2")));
        }
        if (!getCurrentPhase().equals("start") && !getCurrentPhase().equals("final-defeat")) {
            {
                long health = 0;
                if (priestStand != null) {
                    String name = TextUtils.stripColor(priestStand.getName());
                    String healthPart = name.split(" ")[2];
                    health = TextUtils.reverseFormat(healthPart.substring(0, healthPart.length() - 1));
                }
                healths.add(new HealthData("Undead Priest", (int) health, 600000, this.getCurrentPhase().startsWith("fight-")));
            }
            {
                long health = 0;
                if (berserkStand != null) {
                    String name = TextUtils.stripColor(berserkStand.getName());
                    String healthPart = name.split(" ")[2];
                    health = TextUtils.reverseFormat(healthPart.substring(0, healthPart.length() - 1));
                }
                healths.add(new HealthData("Undead Warrior", (int) health, 500000, this.getCurrentPhase().startsWith("fight-")));
            }
            {
                long health = 0;
                if (mageStand != null) {
                    String name = TextUtils.stripColor(mageStand.getName());
                    String healthPart = name.split(" ")[2];
                    health = TextUtils.reverseFormat(healthPart.substring(0, healthPart.length() - 1));
                }
                healths.add(new HealthData("Undead Mage", (int) health, 400000, this.getCurrentPhase().startsWith("fight-")));
            }
            {
                long health = 0;
                if (archerStand != null) {
                    String name = TextUtils.stripColor(archerStand.getName());
                    String healthPart = name.split(" ")[2];
                    health = TextUtils.reverseFormat(healthPart.substring(0, healthPart.length() - 1));
                }
                healths.add(new HealthData("Undead Archer", (int) health, 400000, this.getCurrentPhase().startsWith("fight-")));
            }
        }
        return healths;
    }

    private EntityArmorStand scarfStand;
    private EntityArmorStand priestStand;
    private EntityArmorStand mageStand;
    private EntityArmorStand berserkStand;
    private EntityArmorStand archerStand;
    @Override
    public void onEntitySpawn(LivingEvent.LivingUpdateEvent updateEvent) {
        if (updateEvent.entityLiving instanceof EntityArmorStand) {
            if (updateEvent.entityLiving.getName().startsWith("§e﴾ §c§lScarf§r "))
                scarfStand = (EntityArmorStand) updateEvent.entityLiving;
            else if (updateEvent.entityLiving.getName().startsWith("§6§4§lUndead Archer "))
                archerStand = (EntityArmorStand) updateEvent.entityLiving;
            else if (updateEvent.entityLiving.getName().startsWith("§6§4§lUndead Mage "))
                mageStand = (EntityArmorStand) updateEvent.entityLiving;
            else if (updateEvent.entityLiving.getName().startsWith("§6§4§lUndead Priest "))
                priestStand = (EntityArmorStand) updateEvent.entityLiving;
            else if (updateEvent.entityLiving.getName().startsWith("§6§4§lUndead Warrior "))
                berserkStand = (EntityArmorStand) updateEvent.entityLiving;
        }
    }
}
