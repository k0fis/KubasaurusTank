package kfs.tank.comp;

import kfs.tank.ecs.Entity;
import kfs.tank.ecs.KfsComp;

public class ProjectileComp implements KfsComp {
    public float damage;
    public Entity owner;
    public float lifetime;
    public boolean isPlayerBullet;

    public ProjectileComp(float damage, Entity owner, float lifetime, boolean isPlayerBullet) {
        this.damage = damage;
        this.owner = owner;
        this.lifetime = lifetime;
        this.isPlayerBullet = isPlayerBullet;
    }
}
