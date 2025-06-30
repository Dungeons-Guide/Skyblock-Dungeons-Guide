package kr.syeyoung.modapi.v1_8_9.world;

import kr.syeyoung.modapi.world.UMapData;
import net.minecraft.util.Vec4b;
import net.minecraft.world.storage.MapData;

import java.util.HashMap;
import java.util.Map;

public class UMapDataImpl implements UMapData {
    private MapData delegate;
    public UMapDataImpl(MapData mapData) {
        this.delegate = mapData;
    }

    @Override
    public byte get(int x, int y) {
        if (x < 0 || y < 0 || x >= 128 || y >= 128) throw new IllegalArgumentException("Invalid coordinate: "+x+"/"+y);
        return delegate.colors[y * 128 + x];
    }

    @Override
    public Map<String, MapMarker> getMarkers() {
        Map<String, MapMarker> markers = new HashMap<>();
        for (Map.Entry<String, Vec4b> stringVec4bEntry : delegate.mapDecorations.entrySet()) {
            markers.put(stringVec4bEntry.getKey(), new MapMarker(
                    stringVec4bEntry.getValue().func_176110_a(),
                    stringVec4bEntry.getValue().func_176112_b(),
                    stringVec4bEntry.getValue().func_176113_c(),
                    stringVec4bEntry.getValue().func_176111_d()
            ));
        }
        return markers;
    }
}
