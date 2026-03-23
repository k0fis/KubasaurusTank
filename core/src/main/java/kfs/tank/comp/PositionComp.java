package kfs.tank.comp;

import kfs.tank.ecs.KfsComp;

public class PositionComp implements KfsComp {
    public float x, y;
    public float rotation; // radians

    public PositionComp(float x, float y) {
        this.x = x;
        this.y = y;
        this.rotation = 0;
    }
}
