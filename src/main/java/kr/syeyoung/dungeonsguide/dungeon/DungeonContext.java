package kr.syeyoung.dungeonsguide.dungeon;

import kr.syeyoung.dungeonsguide.dungeon.data.DungeonRoom;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.BlockPackedIce;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

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

    public DungeonContext(World world) {
        this.world = world;
        mapProcessor = new MapProcessor(this);
    }


    public void tick() {
        mapProcessor.tick();
    }

}
