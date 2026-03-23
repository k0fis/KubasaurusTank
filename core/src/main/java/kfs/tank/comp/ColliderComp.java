package kfs.tank.comp;

import kfs.tank.ecs.KfsComp;

public class ColliderComp implements KfsComp {
    public float radius;

    public ColliderComp(float radius) {
        this.radius = radius;
    }
}
