package kfs.tank.comp;

import kfs.tank.ecs.KfsComp;

public class PickupComp implements KfsComp {
    public PickupType type;

    public PickupComp(PickupType type) {
        this.type = type;
    }
}
