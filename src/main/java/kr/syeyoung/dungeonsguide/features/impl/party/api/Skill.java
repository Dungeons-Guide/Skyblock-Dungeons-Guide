package kr.syeyoung.dungeonsguide.features.impl.party.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Skill {
    RUNECRAFTING("runecrafting", "Runecrafting"), COMBAT("combat", "Combat"), MINING("mining", "Mining"), ALCHEMY("alchemy", "Alchemy"), FARMING("farming", "Farming"), TAMING("taming", "Taming"), ENCHANTING("enchanting", "Enchanting"), FISHING("fishing", "Fishing"), FORAGING("foraging", "Foraging"), CARPENTRY("carpentry", "Carpentry");

    private final String jsonName;
    private final String friendlyName;
}
