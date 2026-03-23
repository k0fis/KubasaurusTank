package kfs.tank.sys;

import kfs.tank.SoundManager;
import kfs.tank.World;
import kfs.tank.comp.*;
import kfs.tank.ecs.Entity;
import kfs.tank.ecs.KfsSystem;

import java.util.List;

public class ShootingSys implements KfsSystem {

    private final World world;

    public ShootingSys(World world) {
        this.world = world;
    }

    @Override
    public void update(float delta) {
        List<Entity> shooters = world.getEntitiesWith(WeaponComp.class, TankComp.class);
        for (Entity e : shooters) {
            WeaponComp weapon = world.getComponent(e, WeaponComp.class);
            TankComp tank = world.getComponent(e, TankComp.class);
            PositionComp pos = world.getComponent(e, PositionComp.class);
            if (weapon == null || tank == null || pos == null) continue;

            weapon.cooldown -= delta;

            boolean isPlayer = world.getComponent(e, PlayerComp.class) != null;
            boolean shouldFire = weapon.firing;

            if (shouldFire && weapon.cooldown <= 0) {
                weapon.cooldown = weapon.fireRate;
                fire(e, pos, tank, weapon, isPlayer);
            }
        }
    }

    private void fire(Entity owner, PositionComp pos, TankComp tank, WeaponComp weapon, boolean isPlayer) {
        float angle = tank.turretRotation;
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        float spawnDist = 0.6f;
        float bx = pos.x + cos * spawnDist;
        float by = pos.y + sin * spawnDist;

        float vx = cos * weapon.bulletSpeed;
        float vy = sin * weapon.bulletSpeed;

        world.spawnProjectile(bx, by, vx, vy, weapon.damage, owner, isPlayer, weapon.type);

        // Play weapon sound
        SoundManager sound = world.getSoundManager();
        if (sound != null && isPlayer) {
            switch (weapon.type) {
                case MG: sound.play("mg"); break;
                case ROCKET: sound.play("rocket"); break;
                default: sound.play("cannon"); break;
            }
        }
    }
}
