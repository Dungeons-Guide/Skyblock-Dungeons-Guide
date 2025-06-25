package kr.syeyoung.modapi.v1_8_9.world;

import kr.syeyoung.modapi.world.UMapData;
import net.minecraft.world.storage.MapData;

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
}
