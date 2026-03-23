package kfs.tank.comp;

import kfs.tank.ecs.KfsComp;

public class VelocityComp implements KfsComp {
    public float vx, vy;
    public float maxSpeed;
    public float acceleration;
    public float friction;

    public VelocityComp(float maxSpeed, float acceleration, float friction) {
        this.maxSpeed = maxSpeed;
        this.acceleration = acceleration;
        this.friction = friction;
    }
}
