package kr.syeyoung.modapi.v1_21_5.world;

import kr.syeyoung.modapi.world.UMapData;
import net.minecraft.block.MapColor;
import net.minecraft.item.map.MapDecoration;
import net.minecraft.item.map.MapState;

import java.util.HashMap;
import java.util.Map;

public class UMapDataImpl implements UMapData {
    private MapState delegate;
    public UMapDataImpl(MapState mapData) {
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
            theColor = MapColor.getRenderColor(j);
        }

        return theColor;
    }

    @Override
    public Map<String, MapMarker> getMarkers() {
        Map<String, MapMarker> markers = new HashMap<>();
        for (MapDecoration decoration : delegate.getDecorations()) {
            markers.put(decoration.name().get().getString(), new MapMarker(
                    2 , // TODO: migrate markeri d
                    decoration.x(),
                    decoration.z(),
                    decoration.rotation()
            ));
        }
        return markers;
    }
}
