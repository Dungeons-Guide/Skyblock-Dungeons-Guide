/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.features.impl.party.playerpreview.datarenders;

import kr.syeyoung.dungeonsguide.features.impl.party.playerpreview.api.playerprofile.dataclasses.DungeonClass;
import kr.syeyoung.dungeonsguide.features.impl.party.playerpreview.api.playerprofile.dataclasses.DungeonType;
import kr.syeyoung.dungeonsguide.features.impl.party.playerpreview.api.playerprofile.dataclasses.Skill;
import kr.syeyoung.dungeonsguide.features.impl.party.playerpreview.datarenders.impl.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DataRendererRegistry {
    private static final Map<String, IDataRenderer> dataRendererMap = new HashMap<>();

    public static IDataRenderer getDataRenderer(String id) {
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
        dataRendererMap.put("fairysouls", new DataRendererFairySouls());
        dataRendererMap.put("secrets", new DataRendererSecrets());

        dataRendererMap.put("dummy", new DataRendererSetUrOwn());

        dataRendererMap.put("talismans", new DataRendererTalismans());
        dataRendererMap.put("weight", new DataRendererLilyWeight());
    }
}
