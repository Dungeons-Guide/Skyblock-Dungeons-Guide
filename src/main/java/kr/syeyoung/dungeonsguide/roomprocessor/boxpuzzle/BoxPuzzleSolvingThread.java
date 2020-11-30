package kr.syeyoung.dungeonsguide.roomprocessor.boxpuzzle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.util.*;
import java.util.List;

@Getter
@Setter
public class BoxPuzzleSolvingThread extends Thread {
    private byte[][] data;
    private int playerX;
    private int playerY;
    private Runnable callback;


    public BoxPuzzleSolvingThread(byte[][] data, int playerX, int playerY, Runnable onDone) {
        this.data = data;
        this.playerX = playerX;
        this.playerY = playerY;
        this.callback = onDone;
    }

    LinkedList<BoxMove> solution = new LinkedList<BoxMove>();

    private boolean solved = false;

    @Override
    public void run() {
        solved = false;
        solution = solve(data,playerX,playerY);
        callback.run();
        solved = true;
    }

    public static String stateString(byte[][] array) {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < array.length; y++)
            for(int x = 0; x < array[y].length; x++)
                sb.append(array[y][x]);

        return sb.toString();
    }


    private static LinkedList<BoxMove> solve(byte[][] board, int playerX, int playerY) {
        for (int i = 10; i < 20; i++) {
            LinkedList<BoxMove> solvedStateBoxMove = solve(new HashSet<String>(), board, playerX, playerY, 0, i);
            if (solvedStateBoxMove != null)
                return solvedStateBoxMove;
        }
        return null;
    }

    private static final java.util.List<Point> directions = Arrays.asList(new Point(-1,0), new Point(1,0), new Point(0,1), new Point(0,-1));
    private static LinkedList<BoxMove> solve(Set<String> prevStates, byte[][] board, int playerX, int playerY, int recursionLevel, int maxRecursion) { // result:: playerY == 0
        String stateId = stateString(board);
        if (maxRecursion < recursionLevel) return null;
        if (prevStates.contains(stateId)) return null;

        java.util.Queue<Point> points = new LinkedList<Point>();
        Set<Point> reached= new HashSet<Point>();
        List<BoxMove> possibleBoxMoves = new ArrayList<BoxMove>();
        points.add(new Point(playerX, playerY));

        while (!points.isEmpty()) {
            Point pt = points.poll();
            if (pt.y == 0) {
                return new LinkedList<BoxMove>();
            }
            if (reached.contains(pt)) continue;
            reached.add(pt);
            for (Point dir:directions) {
                int resX= pt.x + dir.x;
                int resY = pt.y + dir.y;
                if (resX < 0 || resY < 0 || resX >= board[0].length || resY >= board.length) {
                    continue;
                }
                if (board[resY][resX] > 0) {
                    possibleBoxMoves.add(new BoxMove(resX, resY, dir.x, dir.y));
                    continue;
                }
                points.add(new Point(resX, resY));
            }
        }

        prevStates.add(stateId);
        for (BoxMove possibleBoxMove : possibleBoxMoves) {
            byte[][] copied = new byte[board.length][];
            for (int y = 0; y < copied.length; y++)
                copied[y] = board[y].clone();

            if (push(copied, possibleBoxMove.x, possibleBoxMove.y, possibleBoxMove.dx, possibleBoxMove.dy)){
//                    System.out.println("------testing "+recursionLevel+"  "+possibleBoxMove.x+","+possibleBoxMove.y);
//                    print(copied);

                prevStates.add(stateId);
                LinkedList<BoxMove> moves = solve(prevStates, copied, possibleBoxMove.x, possibleBoxMove.y, recursionLevel +1, maxRecursion);
                if (moves != null) {
                    moves.addFirst(possibleBoxMove);
                    return moves;
                }
            }
        }
        prevStates.remove(stateId);

        return null;
    }


    private static boolean push(byte[][] board, int x,int y,int dx,int dy) {
        if (board[y][x] != 1) return false;
        int resultingX= x + dx;
        int resultingY = y +dy;
        if (resultingX < 0 || resultingY < 0 || resultingX >= board[0].length || resultingY >= board.length) return false;
        if (board[resultingY][resultingX] == 1 || resultingY == 6) return false;

        board[resultingY][resultingX] = 1;
        board[y][x] = 0;
        return true;
    }

    @Data
    @AllArgsConstructor
    public static class BoxMove {
        int x;
        int y;
        int dx;
        int dy;
    }
}
