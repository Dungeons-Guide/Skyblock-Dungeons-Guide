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

package kr.syeyoung.dungeonsguide.dungeon.doorfinder;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class DungeonSpecificDataProviderRegistry {
    private static final Map<Pattern, DungeonSpecificDataProvider> doorFinders = new HashMap<Pattern, DungeonSpecificDataProvider>();

    static {
        doorFinders.put(Pattern.compile("The Catacombs (?:F[0-9]|E0)"), new CatacombDataProvider());
        doorFinders.put(Pattern.compile("The Catacombs (?:M[0-9])"), new CatacombMasterDataProvider());
    }

    public static DungeonSpecificDataProvider getDoorFinder(String dungeonName) {
        for (Map.Entry<Pattern, DungeonSpecificDataProvider> doorFinderEntry :doorFinders.entrySet()){
            if (doorFinderEntry.getKey().matcher(dungeonName).matches()) return doorFinderEntry.getValue();
        }
        return null;
    }
}
