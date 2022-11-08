package kr.syeyoung.dungeonsguide.mod.cosmetics.data;

import lombok.Data;
import java.util.UUID;

@Data
public class ActiveCosmetic {
    private UUID activityUID;
    private UUID playerUID;
    private UUID cosmeticData;
    private String username;
}
