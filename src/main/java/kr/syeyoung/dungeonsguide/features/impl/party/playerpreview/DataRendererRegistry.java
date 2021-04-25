package kr.syeyoung.dungeonsguide.features.impl.party.playerpreview;

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
}
