package kr.syeyoung.dungeonsguide.roomprocessor;

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class RoomProcessorBoxSolver extends GeneralRoomProcessor {


    private BlockPos[][] poses = new BlockPos[6][7];
    private boolean bugged= true;
    public RoomProcessorBoxSolver(DungeonRoom dungeonRoom) {
        super(dungeonRoom);

        OffsetPointSet ops = (OffsetPointSet) dungeonRoom.getDungeonRoomInfo().getProperties().get("board");
        try {
            if (ops != null) {
                for (int y = 0; y < 6; y++) {
                    for (int x = 0; x < 7; x++) {
                        poses[y][x] = ops.getOffsetPointList().get(y * 7 + x).getBlockPos(dungeonRoom);
                    }
                }
                bugged = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[][] buildCurrentState() {
        World w = getDungeonRoom().getContext().getWorld();
        byte[][] board = new byte[poses.length][poses[0].length];
        for (int y = 0; y < poses.length; y++) {
            for (int x = 0; x < poses[0].length; x++) {
                BlockPos pos = poses[y][x];
                Block b = w.getChunkFromBlockCoords(pos).getBlock(pos);
                if (b == Blocks.air)
                    board[y][x] = 0;
                else
                    board[y][x] = 1;
            }
        }
        return board;
    }

    private byte[][] lastboard;

    private static final List<Point> directions = Arrays.asList(new Point(0,-1), new Point(-1,0), new Point(1,0), new Point(0,1));
    private LinkedList<Action> solve(byte[][] board, int playerX, int playerY) { // result:: playerY == 0
        if (playerY == 0) {
            LinkedList<Action> moves = new LinkedList<Action>();
            return moves;
        }
        for (Point dir:directions) {
            int resX = playerX + dir.x;
            int resY = playerY + dir.y;
            if (resX < 0 || resY < 0 || resX >= board[0].length|| resY>=board.length || board[resY][resX] == 2) continue;

            byte[][] copied = new byte[board.length][];
            for (int y = 0; y < copied.length; y++)
                copied[y] = board[y].clone();

            LinkedList<Action> solved;
            boolean pushed = false;
            copied[playerY][playerX] = 2;
            if (board[resY][resX] == 1) {
                if (!push(copied, resX, resY, dir.x, dir.y)) {
                     continue;
                }
                pushed = true;
                solved = solve(copied, playerX, playerY);
            } else {
                solved = solve(copied, resX, resY);
            }
            if (solved != null) {
                if (pushed) {
                    solved.addFirst(new Push(dir.x, dir.y));
                } else {
                    solved.addFirst(new Move(resX, resY));
                }
                return solved;
            }
        }
        return null;
    }

    public static interface Action { }
    @Data
    @AllArgsConstructor
    public static class Move implements Action {
        private int x;
        private int y;
    }
    @Data
    @AllArgsConstructor
    public static class Push implements Action {
        private int dx;
        private int dy;
    }

    private boolean push(byte[][] board, int x,int y,int dx,int dy) {
        if (board[y][x] != 1) return false;
        int resultingX= x + dx;
        int resultingY = y +dy;
        if (resultingX < 0 || resultingY < 0 || resultingX >= board[0].length || resultingY >= board.length) return false;
        if (board[resultingY][resultingX] != 0) return false;

        board[resultingY][resultingX] = 1;
        board[y][x] = 0;
        return true;
    }

    private int lastPlayerY = 0;
    private Point lastPlayer = null;

    @Override
    public void tick() {
        super.tick();
        if (bugged) return;
        byte[][] currboard = buildCurrentState();
        Point playerPos = getPlayerPos(currboard);
        boolean calculate = lastboard == null || lastPlayerY != Minecraft.getMinecraft().thePlayer.getPosition().getY() || (Minecraft.getMinecraft().thePlayer.getPosition().getY() < 68 && !playerPos.equals(lastPlayer));
        if (!calculate) {
            label:
            for (int y = 0; y < 6; y ++) {
                for (int x = 0; x < 7; x++)
                    if (currboard[y][x] != lastboard[y][x]) {
                        calculate = true;
                        break label;
                    }
            }
        }
        if (calculate) {
            if (Minecraft.getMinecraft().thePlayer.getPosition().getY() < 68) {
                try {
                    LinkedList<Action> semiSolution;
                    semiSolution = solve(currboard, playerPos.x, playerPos.y);
                    if (semiSolution != null) {
                        semiSolution.addFirst(new Move(playerPos.x, playerPos.y));
                        solution = semiSolution;
                    }
                } catch (Error e) {
                    e.printStackTrace();
                }
            } else {
                for (int i = 0; i < 7; i++) {
                    if (currboard[5][i] == 0) {
                        try {
                            LinkedList<Action> semiSolution;
                            semiSolution = solve(currboard, i, 5);
                            if (semiSolution != null) {
                                semiSolution.addFirst(new Move(i, 5));
                                solution = semiSolution;
                                break;
                            }
                        } catch (Error e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        lastPlayerY = Minecraft.getMinecraft().thePlayer.getPosition().getY();
        lastPlayer = playerPos;
        lastboard = currboard;
    }

    private LinkedList<Action> solution;

    public Point getPlayerPos(byte[][] map) {
        BlockPos playerPos = Minecraft.getMinecraft().thePlayer.getPosition();
        int minDir = Integer.MAX_VALUE;
        Point pt = null;
        for (int y = 0; y < poses.length; y++) {
            for (int x = 0; x < poses[0].length; x++) {
                if (map[y][x] == 1) continue;
                int dir = (int) poses[y][x].distanceSq(playerPos);
                if (dir < minDir) {
                    minDir = dir;
                    pt = new Point(x,y);
                }
            }
        }
        return pt;
    }

    @Override
    public void drawWorld(float partialTicks) {
        super.drawWorld(partialTicks);
        if (bugged) return;
        if (solution == null) return;
        try {
            List<BlockPos> line = new ArrayList<BlockPos>();
            List<BlockPos> push = new ArrayList<BlockPos>();
            BlockPos lastLoc2 = null;
            Move lastLoc = null;
            for (Action action : solution) {
                if (action instanceof Move) {
                    BlockPos pos = poses[((Move) action).getY()][((Move) action).getX()];
                    line.add(pos.add(0, -1, 0));
                    lastLoc = (Move) action;
                    lastLoc2 = pos;
                } else if (action instanceof Push) {
                    int y = lastLoc.getY() + ((Push) action).getDy();
                    int x = lastLoc.getX() + ((Push) action).getDx();

                    BlockPos vec = poses[y][x].subtract(lastLoc2);
                    if (vec.getZ() > 1 || vec.getZ() < -1) vec = new BlockPos(vec.getX(),0,vec.getZ() >1 ? 1:-1);
                    if (vec.getX() > 1 || vec.getX() < -1) vec = new BlockPos(vec.getX() >1 ? 1:-1,0,vec.getZ());
                    push.add(lastLoc2.add(vec));
                }
            }
            boolean depth = Minecraft.getMinecraft().thePlayer.getPosition().getY() < 68;

            RenderUtils.drawLines(line, new Color(0, 255, 0, 255), partialTicks, depth);
            for (BlockPos b2:push)
                RenderUtils.highlightBlock(b2, new Color(0, 255, 0, 50), partialTicks, depth);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class Generator implements RoomProcessorGenerator<RoomProcessorBoxSolver> {
        @Override
        public RoomProcessorBoxSolver createNew(DungeonRoom dungeonRoom) {
            RoomProcessorBoxSolver defaultRoomProcessor = new RoomProcessorBoxSolver(dungeonRoom);
            return defaultRoomProcessor;
        }
    }
}
