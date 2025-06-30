package kr.syeyoung.modapi.world;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

public interface UMapData {
    public byte get(int x, int y);

    public Map<String, MapMarker> getMarkers();

    @Data @AllArgsConstructor
    public static class MapMarker {
        private int markerId;
        private int x;
        private int y;
        private int rotation;
    }
}
