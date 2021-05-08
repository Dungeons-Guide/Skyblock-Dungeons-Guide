package kr.syeyoung.dungeonsguide.cosmetics;

import lombok.Data;
import java.util.UUID;

@Data
public class ActiveCosmetic {
    private UUID activityUID;
    private UUID playerUID;
    private UUID cosmeticData;
    private String username;
}
