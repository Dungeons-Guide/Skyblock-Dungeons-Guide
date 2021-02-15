package kr.syeyoung.dungeonsguide.utils;

import javax.vecmath.Vector2d;

public class VectorUtils {
    // Ior rooms, different coordinate system is used. Y Increses as marker goes down. X is same.


    public static Vector2d rotateCounterClockwise(Vector2d vector2d) {
        return new Vector2d(vector2d.y, -vector2d.x);
    }
    public static Vector2d rotateClockwise(Vector2d vector2d) {
        return new Vector2d(-vector2d.y, vector2d.x);
    }
}
