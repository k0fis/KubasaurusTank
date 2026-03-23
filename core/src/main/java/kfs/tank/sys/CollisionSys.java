package kfs.tank.sys;

import kfs.tank.KfsConst;
import kfs.tank.SoundManager;
import kfs.tank.World;
import kfs.tank.comp.*;
import kfs.tank.ecs.Entity;
import kfs.tank.ecs.KfsSystem;

import java.util.ArrayList;
import java.util.List;

public class CollisionSys implements KfsSystem {

    private final World world;

    public CollisionSys(World world) {
        this.world = world;
    }

    @Override
    public void update(float delta) {
        Entity player = world.getPlayer();
        if (player == null || world.isGameOver()) return;

        PositionComp playerPos = world.getComponent(player, PositionComp.class);
        ColliderComp playerCol = world.getComponent(player, ColliderComp.class);
        HealthComp playerHealth = world.getComponent(player, HealthComp.class);
        PlayerComp playerComp = world.getComponent(player, PlayerComp.class);

        if (playerPos == null || playerCol == null) return;

        SoundManager sound = world.getSoundManager();

        // Update invincibility timers
        if (playerHealth != null && playerHealth.invTimer > 0) {
            playerHealth.invTimer -= delta;
        }

        // Bullet collisions
        List<Entity> projectiles = world.getEntitiesWith(ProjectileComp.class);
        List<Entity> toRemove = new ArrayList<>();

        for (Entity proj : projectiles) {
            ProjectileComp pc = world.getComponent(proj, ProjectileComp.class);
            PositionComp pPos = world.getComponent(proj, PositionComp.class);
            ColliderComp pCol = world.getComponent(proj, ColliderComp.class);
            if (pc == null || pPos == null) continue;

            float pRad = pCol != null ? pCol.radius : 0.1f;

            if (pc.isPlayerBullet) {
                // Player bullet → enemies
                List<Entity> enemies = world.getEntitiesWith(EnemyComp.class);
                for (Entity enemy : enemies) {
                    PositionComp ePos = world.getComponent(enemy, PositionComp.class);
                    ColliderComp eCol = world.getComponent(enemy, ColliderComp.class);
                    HealthComp eHealth = world.getComponent(enemy, HealthComp.class);
                    if (ePos == null || eCol == null || eHealth == null) continue;

                    if (circleOverlap(pPos.x, pPos.y, pRad, ePos.x, ePos.y, eCol.radius)) {
                        eHealth.takeDamage((int) pc.damage);
                        toRemove.add(proj);

                        if (eHealth.isDead()) {
                            EnemyComp ec = world.getComponent(enemy, EnemyComp.class);
                            int score = getScoreForEnemy(ec != null ? ec.type : EnemyType.LIGHT);
                            world.addScore(score);
                            if (playerComp != null) playerComp.kills++;
                            world.enemyKilled();
                            world.deleteEntity(enemy);
                            if (sound != null) sound.play("explosion");
                        } else {
                            if (sound != null) sound.play("hit");
                        }
                        break;
                    }
                }
            } else {
                // Enemy bullet → player
                if (circleOverlap(pPos.x, pPos.y, pRad, playerPos.x, playerPos.y, playerCol.radius)) {
                    if (playerHealth != null) {
                        if (playerComp != null && playerComp.shieldTimer > 0) {
                            // Shield absorbs hit
                        } else {
                            playerHealth.takeDamage((int) pc.damage);
                            if (sound != null) sound.play("hit");
                        }
                    }
                    toRemove.add(proj);

                    if (playerHealth != null && playerHealth.isDead()) {
                        world.gameOver(false);
                    }
                }
            }
        }

        for (Entity e : toRemove) {
            world.deleteEntity(e);
        }

        // Player ↔ Pickup collisions
        List<Entity> pickups = world.getEntitiesWith(PickupComp.class);
        List<Entity> pickedUp = new ArrayList<>();

        for (Entity pickup : pickups) {
            PositionComp pkPos = world.getComponent(pickup, PositionComp.class);
            ColliderComp pkCol = world.getComponent(pickup, ColliderComp.class);
            PickupComp pkComp = world.getComponent(pickup, PickupComp.class);
            if (pkPos == null || pkComp == null) continue;

            float pkRad = pkCol != null ? pkCol.radius : 0.3f;

            if (circleOverlap(playerPos.x, playerPos.y, playerCol.radius, pkPos.x, pkPos.y, pkRad)) {
                applyPickup(pkComp.type, playerComp, playerHealth, world.getComponent(player, WeaponComp.class), world.getComponent(player, VelocityComp.class));
                pickedUp.add(pickup);
                if (sound != null) sound.play("pickup");
            }
        }

        for (Entity e : pickedUp) {
            world.deleteEntity(e);
        }
    }

    private void applyPickup(PickupType type, PlayerComp pc, HealthComp health, WeaponComp weapon, VelocityComp vel) {
        switch (type) {
            case FUEL:
                if (pc != null) {
                    pc.fuel = Math.min(pc.maxFuel, pc.fuel + KfsConst.FUEL_PICKUP_AMOUNT);
                    pc.fuelEmptyTimer = 0;
                }
                world.addScore(KfsConst.SCORE_FUEL);
                break;
            case WEAPON:
                if (weapon != null) {
                    // Upgrade to next weapon
                    switch (weapon.type) {
                        case CANNON:
                            weapon.type = WeaponType.MG;
                            weapon.fireRate = KfsConst.MG_FIRE_RATE;
                            weapon.damage = KfsConst.MG_DAMAGE;
                            weapon.bulletSpeed = KfsConst.MG_BULLET_SPEED;
                            break;
                        case MG:
                            weapon.type = WeaponType.ROCKET;
                            weapon.fireRate = KfsConst.ROCKET_FIRE_RATE;
                            weapon.damage = KfsConst.ROCKET_DAMAGE;
                            weapon.bulletSpeed = KfsConst.ROCKET_BULLET_SPEED;
                            break;
                        case ROCKET:
                            weapon.type = WeaponType.LASER;
                            weapon.fireRate = KfsConst.LASER_FIRE_RATE;
                            weapon.damage = KfsConst.LASER_DAMAGE;
                            weapon.bulletSpeed = KfsConst.LASER_BULLET_SPEED;
                            break;
                        default:
                            break;
                    }
                }
                world.addScore(KfsConst.SCORE_UPGRADE);
                break;
            case ARMOR:
                if (health != null) {
                    health.armor = Math.min(0.8f, health.armor + KfsConst.ARMOR_BONUS);
                }
                world.addScore(KfsConst.SCORE_UPGRADE);
                break;
            case SPEED:
                if (pc != null) pc.speedBoostTimer = KfsConst.SPEED_BOOST_DURATION;
                world.addScore(KfsConst.SCORE_UPGRADE);
                break;
            case SHIELD:
                if (pc != null) pc.shieldTimer = KfsConst.SHIELD_DURATION;
                world.addScore(KfsConst.SCORE_UPGRADE);
                break;
        }
    }

    private int getScoreForEnemy(EnemyType type) {
        switch (type) {
            case HEAVY: return KfsConst.SCORE_HEAVY;
            case SNIPER: return KfsConst.SCORE_SNIPER;
            case SWARM: return KfsConst.SCORE_SWARM;
            default: return KfsConst.SCORE_LIGHT;
        }
    }

    private boolean circleOverlap(float x1, float y1, float r1, float x2, float y2, float r2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        float dist = dx * dx + dy * dy;
        float radSum = r1 + r2;
        return dist < radSum * radSum;
    }
}
