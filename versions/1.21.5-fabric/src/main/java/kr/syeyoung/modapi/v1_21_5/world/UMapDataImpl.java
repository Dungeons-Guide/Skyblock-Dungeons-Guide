package kr.syeyoung.modapi.v1_21_5.world;

import kr.syeyoung.modapi.world.UMapData;
import net.minecraft.block.material.MapColor;
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
    public int getRGB(int x, int y) {
        if (y <0 || y>= 128 || x < 0 || x >= 128) return 0;
        int i = y * 128 +x;
        int j = get(x,y) & 255;

        int theColor;
        if (j / 4 == 0)
        {
            theColor = (i + i / 128 & 1) * 8 + 16 << 24;
        }
        else
        {
            theColor = MapColor.mapColorArray[j / 4].getMapColor(j & 3);
        }

        return theColor;
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
