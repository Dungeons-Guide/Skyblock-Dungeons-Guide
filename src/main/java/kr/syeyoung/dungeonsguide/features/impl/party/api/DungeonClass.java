package kr.syeyoung.dungeonsguide.features.impl.party.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum DungeonClass {
    MAGE("mage"), ARCHER("archer"), HEALER("healer"), TANK("tank"), BERSERK("berserk");


    private String jsonName;
    private DungeonClass(String jsonName) {
        this.jsonName = jsonName;
    }

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
