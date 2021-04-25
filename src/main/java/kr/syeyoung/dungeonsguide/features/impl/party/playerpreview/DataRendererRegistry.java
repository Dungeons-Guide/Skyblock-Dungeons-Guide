package kr.syeyoung.dungeonsguide.features.impl.party.playerpreview;

import kr.syeyoung.dungeonsguide.features.impl.party.api.DungeonClass;
import kr.syeyoung.dungeonsguide.features.impl.party.api.DungeonType;
import kr.syeyoung.dungeonsguide.features.impl.party.api.Skill;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DataRendererRegistry {
    private static final Map<String, DataRenderer> dataRendererMap = new HashMap<>();

    public static DataRenderer getDataRenderer(String id) {
        return dataRendererMap.get(id);
    }

    public static Set<String> getValidDataRenderer() {
        return dataRendererMap.keySet();
    }

    static {
        dataRendererMap.put("catalv", new DataRendererDungeonLv(DungeonType.CATACOMBS));
        for (DungeonClass value : DungeonClass.values()) {
            dataRendererMap.put("class_"+value.getJsonName()+"_lv", new DataRendererClassLv(value));
        }
        dataRendererMap.put("selected_class_lv", new DataRendererSelectedClassLv());
        for (Skill value : Skill.values()) {
            dataRendererMap.put("skill_"+value.getJsonName()+"_lv", new DataRendererSkillLv(value));
        }
        for (DungeonType value : DungeonType.values()) {
            for (Integer validFloor : value.getValidFloors()) {
                dataRendererMap.put("dungeon_"+value.getJsonName()+"_"+validFloor+"_stat", new DataRenderDungeonFloorStat(value, validFloor));
            }
            dataRendererMap.put("dungeon_"+value.getJsonName()+"_higheststat", new DataRenderDungeonHighestFloorStat(value));
        }
        dataRendererMap.put("fairyouls", new DataRendererFairySouls());
    }
}
