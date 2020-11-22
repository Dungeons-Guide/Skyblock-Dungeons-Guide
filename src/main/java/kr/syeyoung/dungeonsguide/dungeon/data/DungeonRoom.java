package kr.syeyoung.dungeonsguide.dungeon.data;

import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import lombok.Getter;
import net.minecraft.util.BlockPos;

import java.awt.*;
import java.util.List;

@Getter
public class DungeonRoom {
    private final List<Point> unitPoints;
    private final short shape;
    private final byte color;

    private final BlockPos min;

    private final DungeonContext context;

    public DungeonRoom(List<Point> points, short shape, byte color, BlockPos min, DungeonContext context) {
        this.unitPoints = points;
        this.shape = shape;
        this.color = color;
        this.min = min;
        this.context = context;
    }
}
