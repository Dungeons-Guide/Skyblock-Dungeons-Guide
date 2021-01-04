package kr.syeyoung.dungeonsguide.dungeon.doorfinder;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class DungeonSpecificDataProviderRegistry {
    private static final Map<Pattern, DungeonSpecificDataProvider> doorFinders = new HashMap<Pattern, DungeonSpecificDataProvider>();

    static {
        doorFinders.put(Pattern.compile("The Catacombs (?:F[0-9]|E)"), new CatacombDataProvider());
    }

    public static DungeonSpecificDataProvider getDoorFinder(String dungeonName) {
        for (Map.Entry<Pattern, DungeonSpecificDataProvider> doorFinderEntry :doorFinders.entrySet()){
            if (doorFinderEntry.getKey().matcher(dungeonName).matches()) return doorFinderEntry.getValue();
        }
        return null;
    }
}
