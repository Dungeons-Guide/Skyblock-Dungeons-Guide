package kr.syeyoung.dungeonsguide.features.impl.party.api;

import lombok.Data;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class PlayerProfile {
    private String profileUID;
    private String memberUID;
    private String profileName;

    private long lastSave;

    private int fairySouls;
    private int fairyExchanges;

    private Armor currentArmor;
    private List<Armor> wardrobe = new ArrayList<>();
    private int selectedWardrobe = -1;

    private ItemStack[] inventory;
    private ItemStack[] enderchest;
    private ItemStack[] talismans;

    @Data
    public static class Armor {
        private final ItemStack[] armorSlots = new ItemStack[4];

        public ItemStack getHelmet() { return armorSlots[3]; }
        public ItemStack getChestPlate() { return armorSlots[2]; }
        public ItemStack getLeggings() { return armorSlots[1]; }
        public ItemStack getBoots() { return armorSlots[0]; }
    }

    private Map<DungeonType, DungeonSpecificData<DungeonStat>> dungeonStats = new HashMap<>();

    private Map<DungeonClass, ClassSpecificData<PlayerClassData>> playerClassData = new HashMap<>();
    private DungeonClass selectedClass;
    @Data
    public static class PlayerClassData {
        private double experience;
    }

    private Map<Skill, Double> skillXp = new HashMap<>();

    private List<Pet> pets = new ArrayList<>();

}