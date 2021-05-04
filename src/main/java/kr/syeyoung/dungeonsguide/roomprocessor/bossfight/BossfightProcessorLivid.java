package kr.syeyoung.dungeonsguide.roomprocessor.bossfight;

import kr.syeyoung.dungeonsguide.utils.TextUtils;
import lombok.Getter;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraftforge.event.entity.living.LivingEvent;

import java.util.*;

@Getter
public class BossfightProcessorLivid extends GeneralBossfightProcessor {
    private String realLividName;
    private String prefix;
    private EntityOtherPlayerMP realLivid;

    private final Set<String> knownLivids = new HashSet<String>();

    public BossfightProcessorLivid() {
        addPhase(PhaseData.builder().phase("start").build());
    }
    private static final Map<String, String> lividColorPrefix = new HashMap<String, String>() {{
            put("Vendetta", "§f");
            put("Crossed", "§d");
            put("Hockey", "§c");
            put("Doctor", "§7");
            put("Frog", "§2");
            put("Smile", "§a");
            put("Scream", "§1");
            put("Purple", "§5");
            put("Arcade", "§e");
    }};
    @Override
    public void onEntityUpdate(LivingEvent.LivingUpdateEvent updateEvent) {
        if (updateEvent.entityLiving.getName().endsWith("Livid") && updateEvent.entityLiving instanceof EntityOtherPlayerMP) {
            if (!knownLivids.contains(updateEvent.entityLiving.getName())) {
                knownLivids.add(updateEvent.entityLiving.getName());
                realLividName = updateEvent.entityLiving.getName();
                realLivid = (EntityOtherPlayerMP) updateEvent.entityLiving;
                prefix = lividColorPrefix.get(realLividName.split(" ")[0]);
            } else if (realLividName.equalsIgnoreCase(updateEvent.entityLiving.getName())) {
                realLivid = (EntityOtherPlayerMP) updateEvent.entityLiving;
            }
        } else if (updateEvent.entityLiving.getName().startsWith(prefix+"﴾ ") && updateEvent.entityLiving instanceof EntityArmorStand) {
            lividStand = (EntityArmorStand) updateEvent.entityLiving;
        }
    }
    private EntityArmorStand lividStand;

    @Override
    public List<HealthData> getHealths() {
        List<HealthData> healths = new ArrayList<HealthData>();
        long health = 0;
        if (lividStand != null) {
            try {
                String name = TextUtils.stripColor(lividStand.getName());
                String healthPart = name.split(" ")[2];
                health = TextUtils.reverseFormat(healthPart.substring(0, healthPart.length() - 1));
            } catch (Exception e) {e.printStackTrace();}
        }
        healths.add(new HealthData(realLividName == null ? "unknown" : realLividName, (int) health,7000000 , true));
        return healths;
    }

    @Override
    public String getBossName() {
        return realLividName == null ? "Livid" : realLividName;
    }
}
