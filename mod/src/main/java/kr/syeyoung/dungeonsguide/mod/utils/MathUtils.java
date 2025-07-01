package kr.syeyoung.dungeonsguide.mod.utils;

public class MathUtils {
    public static int clamp_int(int val, int min, int max) {
        if (val < min) return min;
        if (val > max) return max;
        return val;
    }

    public static float clamp_float(float val, float min, float max) {
        if (val < min) return min;
        if (val > max) return max;
        return val;
    }

    public static double clamp_double(double val, double min, double max) {
        if (val < min) return min;
        if (val > max) return max;
        return val;
    }

    public static double frac(double val) {
        return val - Math.floor(val);
    }
}
