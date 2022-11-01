package kr.syeyoung.dungeonsguide.mod.cosmetics;


import lombok.Data;

import java.util.UUID;

@Data
public class CosmeticData {
    private UUID id;
    private String cosmeticType;
    private String reqPerm;
    private String data;
}
