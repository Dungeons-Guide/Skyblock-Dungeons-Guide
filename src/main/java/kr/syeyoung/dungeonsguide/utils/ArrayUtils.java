package kr.syeyoung.dungeonsguide.utils;

public class ArrayUtils {
    public static int[][] rotateCounterClockwise(int[][] arr) {
        int[][] res = new int[arr[0].length][arr.length];
        for(int y=0; y<arr.length; y++) {
            for (int x = 0; x< arr[0].length; x++) {
                res[res.length - x - 1][y] = arr[y][x];
            }
        }
        return res;
    }
    public static int[][] rotateClockwise(int[][] arr) {
        int[][] res = new int[arr[0].length][arr.length];
        for(int y=0; y<arr.length; y++) {
            for (int x = 0; x< arr[0].length; x++) {
                res[x][res[0].length - y - 1] = arr[y][x];
            }
        }
        return res;
    }
}
