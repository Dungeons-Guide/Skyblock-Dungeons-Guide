package kr.syeyoung.dungeonsguide.dungeon.doorfinder;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class DoorFinderRegistry {
    private static final Map<Pattern, StartDoorFinder> doorFinders = new HashMap<Pattern, StartDoorFinder>();

    static {
        doorFinders.put(Pattern.compile("The Catacombs F[0-9]"), new CatacombDoorFinder());
    }

    public static StartDoorFinder getDoorFinder(String dungeonName) {
        for (Map.Entry<Pattern, StartDoorFinder> doorFinderEntry :doorFinders.entrySet()){
            if (doorFinderEntry.getKey().matcher(dungeonName).matches()) return doorFinderEntry.getValue();
        }
        return null;
    }
}
