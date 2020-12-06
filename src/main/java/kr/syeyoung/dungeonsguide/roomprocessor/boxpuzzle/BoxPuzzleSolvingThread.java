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
        System.out.println("new solving thread");
    }

    Route solution = null;

    private boolean solved = false;

    @Override
    public void run() {
        solved = false;
        solution = solve(data,playerX,playerY, 20);
        solved = true;
        callback.run();
    }


    public static String stateString(byte[][] array) {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < array.length; y++)
            for(int x = 0; x < array[y].length; x++)
                sb.append(array[y][x]);

        return sb.toString();
    }


    private static final List<Point> directions = Arrays.asList(new Point(-1,0), new Point(1,0), new Point(0,1), new Point(0,-1));
    private static Route solve(byte[][] boardStart, int playerX, int playerY, int maxRecursion) { // result:: playerY == 0
        Queue<Route> routes = new LinkedList<Route>();
        Set<String> globalStatesBeen = new HashSet<String>();

        Route r = new Route();
        r.currentState = boardStart;
        routes.add(r);

        while (!routes.isEmpty()) {
            Route route = routes.poll();

            if (routes.size() > 3000000) return null;

            String stateId = stateString(route.currentState);
            if (maxRecursion < route.boxMoves.size()) continue;
            if (globalStatesBeen.contains(stateId)) continue;

            Queue<Point> points = new LinkedList<Point>();
            Set<Point> reached= new HashSet<Point>();
            List<BoxMove> possibleBoxMoves = new ArrayList<BoxMove>();
            points.add(new Point(playerX, playerY));

            while (!points.isEmpty()) {
                Point pt = points.poll();
                if (pt.y == 0) {
                    return route;
                }
                if (reached.contains(pt)) continue;
                reached.add(pt);
                for (Point dir:directions) {
                    int resX= pt.x + dir.x;
                    int resY = pt.y + dir.y;
                    if (resX < 0 || resY < 0 || resX >= route.currentState[0].length || resY >= route.currentState.length) {
                        continue;
                    }
                    if (route.currentState[resY][resX] > 0) {
                        possibleBoxMoves.add(new BoxMove(resX, resY, dir.x, dir.y));
                        continue;
                    }
                    points.add(new Point(resX, resY));
                }
            }

            globalStatesBeen.add(stateId);
            for (BoxMove possibleBoxMove : possibleBoxMoves) {
                byte[][] copied = new byte[route.currentState.length][];
                for (int y = 0; y < copied.length; y++)
                    copied[y] = route.currentState[y].clone();

                if (push(copied, possibleBoxMove.x, possibleBoxMove.y, possibleBoxMove.dx, possibleBoxMove.dy)){
                    String stateId2 = stateString(copied);
                    if (globalStatesBeen.contains(stateId2)) continue;

                    Route route2 = new Route();
                    route2.boxMoves = new ArrayList<BoxMove>(route.boxMoves);
                    route2.boxMoves.add(possibleBoxMove);
                    route2.currentState = copied;

                    routes.add(route2);
                }
            }
        }


        return null;
    }


    public static boolean push(byte[][] board, int x,int y,int dx,int dy) {
        if (board[y][x] != 1) return false;
        int resultingX= x + dx;
        int resultingY = y +dy;
        if (resultingX < 0 || resultingY < 0 || resultingX >= board[0].length || resultingY >= board.length) return false;
        if (board[resultingY][resultingX] == 1 || resultingY == 6) return false;

        board[resultingY][resultingX] = 1;
        board[y][x] = 0;
        return true;
    }

    public static class BoxMove {
        int x;
        int y;
        int dx;
        int dy;

        public BoxMove(int x, int y, int dx, int dy) {
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getDx() {
            return dx;
        }

        public int getDy() {
            return dy;
        }
    }

    public static class Route {
        List<BoxMove> boxMoves = new LinkedList<BoxMove>();
        byte[][] currentState;
    }
}
