package kr.syeyoung.dungeonsguide.roomprocessor.bossfight;

import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraftforge.event.entity.living.LivingEvent;

import java.util.ArrayList;
import java.util.List;

public class BossfightProcessorThorn extends GeneralBossfightProcessor {
    public BossfightProcessorThorn() {
        addPhase(GeneralBossfightProcessor.PhaseData.builder()
                .phase("fight").build()
        );
    }


    @Override
    public List<HealthData> getHealths() {
        List<HealthData> healths = new ArrayList<HealthData>();
        healths.add(new HealthData("Thorn", (int) Math.round(BossStatus.healthScale * 4),4, true));
        return healths;
    }
}
