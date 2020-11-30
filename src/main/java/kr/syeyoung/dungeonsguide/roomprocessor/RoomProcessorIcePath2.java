package kr.syeyoung.dungeonsguide.roomprocessor;

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class RoomProcessorIcePath2 extends GeneralRoomProcessor {
    private boolean bugged = false;

    private List<List<BlockPos>> solution = new ArrayList<List<BlockPos>>();

    public RoomProcessorIcePath2(DungeonRoom dungeonRoom) {

        super(dungeonRoom);

        String levels = (String) dungeonRoom.getDungeonRoomInfo().getProperties().get("levels");
        if (levels == null) {
            bugged = true;
            return;
        }

        for (String s : levels.split(",")) {
            try {
                OffsetPointSet level = (OffsetPointSet) dungeonRoom.getDungeonRoomInfo().getProperties().get(s + "-board");
                String data = (String) dungeonRoom.getDungeonRoomInfo().getProperties().get(s + "-level");
                int width = Integer.parseInt(data.split(":")[0]);
                int height = Integer.parseInt(data.split(":")[1]);
                int startX = Integer.parseInt(data.split(":")[2]);
                int startY = Integer.parseInt(data.split(":")[3]);
                int endX = Integer.parseInt(data.split(":")[4]);
                int endY = Integer.parseInt(data.split(":")[5]);

                int[][] map = new int[height][width];
                BlockPos[][] map2 = new BlockPos[height][width];
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        map2[y][x] = level.getOffsetPointList().get(y * width + x).getBlockPos(dungeonRoom);
                        map[y][x] = level.getOffsetPointList().get(y * width + x).getBlock(dungeonRoom) == Blocks.air ? 0 : 1;
                    }
                }

                List<Point> hamiltonianPath = findFirstHamiltonianPath(map, startX, startY, endX, endY);
                if (hamiltonianPath == null) continue;
                hamiltonianPath.add(0,new Point(startX, startY));
                List<BlockPos> poses = new LinkedList<BlockPos>();
                for (int i = 0; i < hamiltonianPath.size(); i++) {
                    Point p = hamiltonianPath.get(i);
                    poses.add(map2[p.y][p.x]);
                }
                solution.add(poses);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    @Override
    public void drawWorld(float partialTicks) {
        for (List<BlockPos> solution:this.solution)
            RenderUtils.drawLines(solution, new Color(0,255,0, 255), partialTicks, true);
    }

    public static class Generator implements RoomProcessorGenerator<RoomProcessorIcePath2> {
        @Override
        public RoomProcessorIcePath2 createNew(DungeonRoom dungeonRoom) {
            RoomProcessorIcePath2 defaultRoomProcessor = new RoomProcessorIcePath2(dungeonRoom);
            return defaultRoomProcessor;
        }
    }

    private static List<Point> findFirstHamiltonianPath(int[][] map, int startX, int startY, int endX, int endY) {
        int emptySpace =0;
        for (int y = 0; y < map.length; y++)
            for (int x = 0; x < map[y].length; x++)
                if (map[y][x] == 0) emptySpace++;

                map[startY][startX] = 2;

        return findHamiltonianPath(map, startX, startY, endX, endY, 0, emptySpace-1);
    }


    private static final List<Point> directions = Arrays.asList(new Point(0,-1), new Point(-1,0), new Point(1,0), new Point(0,1));
    private static LinkedList<Point> findHamiltonianPath(int[][] map, int startX, int startY, int endX, int endY, int depth, int reqDepth) {
        if (endX == startX && endY == startY) {
            if (depth != reqDepth) return null;
            LinkedList<Point> path = new LinkedList<Point>();
            path.add(new Point(startX, startY));
            return path;
        }

        for (Point p : directions) {
            int y = p.y +startY,x=p.x + startX;
            if (y <0 || y >= map.length || x <0 || x >= map[0].length || map[y][x] != 0) continue;

            int[][] copiedMap = new int[map.length][map[0].length];
            for (int y2 = 0; y2 < copiedMap.length; y2++)
                copiedMap[y2] = map[y2].clone();
            copiedMap[y][x] = 2;

            LinkedList<Point> potentialRoute = findHamiltonianPath(copiedMap, x,y,endX,endY, depth +1, reqDepth);
            if (potentialRoute != null) {
                potentialRoute.addFirst(new Point(x,y));
                return potentialRoute;
            }
        }
        return null;
    }
}
