package kfs.tank.comp;

import kfs.tank.ecs.KfsComp;

public class TankComp implements KfsComp {
    public float bodyRotation;   // radians, direction body faces
    public float turretRotation; // radians, direction turret faces
    public float turretRotSpeed;

    public TankComp(float turretRotSpeed) {
        this.turretRotSpeed = turretRotSpeed;
    }
}
