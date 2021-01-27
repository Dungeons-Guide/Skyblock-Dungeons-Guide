package kr.syeyoung.dungeonsguide.roomprocessor.bossfight;

import lombok.Getter;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraftforge.event.entity.living.LivingEvent;

import java.util.*;

@Getter
public class BossfightProcessorLivid extends GeneralBossfightProcessor {
    private String realLividName;
    private EntityOtherPlayerMP realLivid;

    private Set<String> knownLivids = new HashSet<String>();

    public BossfightProcessorLivid() {
        addPhase(PhaseData.builder().phase("start").build());
    }

    @Override
    public void onEntitySpawn(LivingEvent.LivingUpdateEvent updateEvent) {
        if (updateEvent.entityLiving.getName().endsWith("Livid") && updateEvent.entityLiving instanceof EntityOtherPlayerMP) {
            if (!knownLivids.contains(updateEvent.entityLiving.getName())) {
                knownLivids.add(updateEvent.entityLiving.getName());
                realLividName = updateEvent.entityLiving.getName();
                realLivid = (EntityOtherPlayerMP) updateEvent.entityLiving;
                System.out.println("Think real livid is "+realLividName);
            }
        }
    }

    @Override
    public List<HealthData> getHealths() {
        ArrayList<HealthData> healthData = new ArrayList<HealthData>();
        return healthData;
    }
}
