package kr.syeyoung.dungeonsguide.dungeon;

import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.roomprocessor.RoomProcessor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DungeonContext {
    @Getter
    private World world;
    @Getter
    private MapProcessor mapProcessor;

    @Getter
    @Setter
    private BlockPos dungeonMin;

    @Getter
    private Map<Point, DungeonRoom> roomMapper = new HashMap<Point, DungeonRoom>();
    @Getter
    private List<DungeonRoom> dungeonRoomList = new ArrayList<DungeonRoom>();

    @Getter
    private List<RoomProcessor> globalRoomProcessors = new ArrayList<RoomProcessor>();

    public DungeonContext(World world) {
        this.world = world;
        mapProcessor = new MapProcessor(this);
    }


    public void tick() {
        mapProcessor.tick();
    }

}
