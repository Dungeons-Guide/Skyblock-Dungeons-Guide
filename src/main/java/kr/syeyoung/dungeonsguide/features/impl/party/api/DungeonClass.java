package kr.syeyoung.dungeonsguide.features.impl.party.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum DungeonClass {
    MAGE("mage", "Mage"), ARCHER("archer","Archer"), HEALER("healer", "Healer"), TANK("tank", "Tank"), BERSERK("berserk", "Berserk");


    private String jsonName;
    private String familarName;
    private static final Map<String, DungeonClass> jsonNameToClazz = new HashMap<>();
    static {
        for (DungeonClass value : values()) {
            jsonNameToClazz.put(value.getJsonName(), value);
        }
    }

    public static DungeonClass getClassByJsonName(String name) {
        return jsonNameToClazz.get(name);
    }

}
