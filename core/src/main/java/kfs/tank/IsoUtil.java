package kfs.tank;

public class IsoUtil {

    public static float worldToScreenX(float wx, float wy) {
        return (wx - wy) * (KfsConst.TILE_W / 2f);
    }

    public static float worldToScreenY(float wx, float wy) {
        return (wx + wy) * (KfsConst.TILE_H / 2f);
    }

    public static float screenToWorldX(float sx, float sy) {
        return (sx / (KfsConst.TILE_W / 2f) + sy / (KfsConst.TILE_H / 2f)) / 2f;
    }

    public static float screenToWorldY(float sx, float sy) {
        return (sy / (KfsConst.TILE_H / 2f) - sx / (KfsConst.TILE_W / 2f)) / 2f;
    }

    public static float depthKey(float wx, float wy) {
        return wy + wx * 0.01f;
    }

    /** Lerp angle in radians, shortest path */
    public static float lerpAngle(float from, float to, float t) {
        float diff = ((to - from + (float)Math.PI * 3) % ((float)Math.PI * 2)) - (float)Math.PI;
        return from + diff * t;
    }

    /** Normalize angle to [-PI, PI] */
    public static float normalizeAngle(float a) {
        while (a > Math.PI) a -= Math.PI * 2;
        while (a < -Math.PI) a += Math.PI * 2;
        return a;
    }
}
