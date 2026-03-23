package kfs.tank.comp;

import kfs.tank.ecs.KfsComp;

public class WeaponComp implements KfsComp {
    public WeaponType type;
    public float fireRate;   // seconds between shots
    public float cooldown;   // current cooldown timer
    public float damage;
    public float bulletSpeed;
    public boolean firing;   // whether fire button is held

    public WeaponComp(WeaponType type, float fireRate, float damage, float bulletSpeed) {
        this.type = type;
        this.fireRate = fireRate;
        this.damage = damage;
        this.bulletSpeed = bulletSpeed;
    }
}
