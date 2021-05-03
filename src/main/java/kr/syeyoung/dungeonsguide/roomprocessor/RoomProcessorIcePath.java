package kr.syeyoung.dungeonsguide.roomprocessor;

import com.google.common.base.Predicate;
import kr.syeyoung.dungeonsguide.config.Config;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.*;
import java.util.List;

public class RoomProcessorIcePath extends GeneralRoomProcessor {

    private int[][] map;
    private OffsetPoint[][] map2;
    private Set<OffsetPoint> endNode = new HashSet<OffsetPoint>();

    private final List<BlockPos> solution = new ArrayList<BlockPos>();

    private BlockPos lastSilverfishLoc;
    private int sameTick;

    private Entity silverfish;

    private boolean err;

    public RoomProcessorIcePath(DungeonRoom dungeonRoom) {
        super(dungeonRoom);
        findSilverFishanddoStuff();
    }

    public void findSilverFishanddoStuff() {
        final BlockPos low = getDungeonRoom().getMin();
        final BlockPos high = getDungeonRoom().getMax();
        List<EntitySilverfish> silverfishs = getDungeonRoom().getContext().getWorld().getEntities(EntitySilverfish.class, new Predicate<EntitySilverfish>() {
            @Override
            public boolean apply(@Nullable EntitySilverfish input) {
                if (input.isInvisible()) return false;
                BlockPos pos = input.getPosition();
                return low.getX() < pos.getX() && pos.getX() < high.getX()
                        && low.getZ() < pos.getZ() && pos.getZ() < high.getZ();
            }
        });

        if (!silverfishs.isEmpty()) silverfish = silverfishs.get(0);
        if (silverfishs.isEmpty()) {
            err = true;
            return;
        }
        try {
            buildMap();
            err = false;
        } catch (Exception e) {
            e.printStackTrace();;
            err = true;
            return;
        }
    }

    private void buildMap() {
        int width = (Integer) getDungeonRoom().getDungeonRoomInfo().getProperties().get("width");
        int height = (Integer) getDungeonRoom().getDungeonRoomInfo().getProperties().get("height");
        OffsetPointSet ops = (OffsetPointSet) getDungeonRoom().getDungeonRoomInfo().getProperties().get("board");
        OffsetPointSet endNodes = (OffsetPointSet) getDungeonRoom().getDungeonRoomInfo().getProperties().get("endnodes");
        map2 = new OffsetPoint[width][height];
        map = new int[width][height];
        for (int y = 0; y < height; y ++) {
            for (int x =0; x < width; x++) {
                OffsetPoint op = ops.getOffsetPointList().get(y * width + x);
                map2[y][x] = op;
                map[y][x] = op.getBlock(getDungeonRoom()) == Blocks.air ? 0 : 1;
            }
        }
        endNode.addAll(endNodes.getOffsetPointList());
    }

    public void tick() {
        super.tick();
        if (err || silverfish.isDead) {
            findSilverFishanddoStuff();
            if (err) return;
        }
        if (silverfish.getPosition().equals(lastSilverfishLoc)) {
            if (sameTick < 10) {
                sameTick ++;
                return;
            } else if (sameTick == 10) {
                sameTick ++;
                Point silverfish = getPointOfSilverFishOnMap(this.silverfish.getPosition());
                List<Point> tempSol = solve(map, silverfish.x, silverfish.y, new Predicate<Point>() {
                    @Override
                    public boolean apply(@Nullable Point input) {
                        return endNode.contains(map2[input.y][input.x]);
                    }
                });
                {
                    solution.clear();
                    for (Point point : tempSol) {
                        solution.add(map2[point.y][point.x].getBlockPos(getDungeonRoom()));
                    }
                }

            }
        } else {
            sameTick = 0;
        }

        lastSilverfishLoc = silverfish.getPosition();
    }


    @Override
    public void drawWorld(float partialTicks) {
        super.drawWorld(partialTicks);
        if (!FeatureRegistry.SOLVER_SILVERFISH.isEnabled()) return;
        if (!err)
        RenderUtils.drawLines(solution, new Color(0,255,0, 255), partialTicks, false);
    }

    public Point getPointOfSilverFishOnMap(BlockPos blockPos) {
        for (int y = 0; y < map.length; y ++) {
            for (int x = 0; x < map[0].length; x++) {
                if (map2[y][x].getBlockPos(getDungeonRoom()).equals(blockPos))
                    return new Point(x,y);
            }
        }
        return null;
    }


    // Taken from https://stackoverflow.com/a/55271133 and modified to suit our needs
    // Answer by ofekp (https://stackoverflow.com/users/4295037/ofekp)
    public static List<Point> solve(int[][] board, int startX, int startY, Predicate<Point> finishLinePredicate) {
        Point startPoint = new Point(startX, startY);

        LinkedList<Point> queue = new LinkedList<Point>();
        Point[][] boardSearch = new Point[board.length][board[0].length];

        queue.addLast(new Point(startX, startY));
        boardSearch[startY][startX] = startPoint;

        while (queue.size() != 0) {
            Point currPos = queue.pollFirst();
            for (Direction dir : Direction.values()) {
                Point nextPos = move(board, boardSearch, currPos, dir);
                if (nextPos != null) {
                    queue.addLast(nextPos);
                    boardSearch[nextPos.y][nextPos.x] = new Point(currPos.x, currPos.y);
                    if (finishLinePredicate.apply(nextPos)) {
                        List<Point> route = new ArrayList<Point>();
                        Point tmp = currPos;
                        route.add(nextPos);
                        route.add(currPos);
                        while (tmp != startPoint) {
                            tmp = boardSearch[tmp.y][tmp.x];
                            route.add(tmp);
                        }
                        return route;
                    }
                }
            }
        }
        return Collections.emptyList();
    }

    public static Point move(int[][] board, Point[][] boardSearch, Point currPos, Direction dir) {
        int x = currPos.x;
        int y = currPos.y;

        int diffX = dir.dx;
        int diffY = dir.dy;

        int i = 1;
        while (x + i * diffX >= 0 && x + i * diffX < board[0].length
                && y + i * diffY >= 0 && y + i * diffY < board.length
                && board[y + i * diffY][x + i * diffX] != 1) {
            i++;
        }
        i--;

        if (boardSearch[y + i * diffY][x + i * diffX] != null) {
            return null;
        }

        return new Point(x + i * diffX, y + i * diffY);
    }

    @Getter
    @AllArgsConstructor
    public enum Direction {
        LEFT(-1,0),
        RIGHT(1,0),
        UP(0,-1),
        DOWN(0,1);

        int dx, dy;
    }


    public static class Generator implements RoomProcessorGenerator<RoomProcessorIcePath> {
        @Override
        public RoomProcessorIcePath createNew(DungeonRoom dungeonRoom) {
            RoomProcessorIcePath defaultRoomProcessor = new RoomProcessorIcePath(dungeonRoom);
            return defaultRoomProcessor;
        }
    }
}
