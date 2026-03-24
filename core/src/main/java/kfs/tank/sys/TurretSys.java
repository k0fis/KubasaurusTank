package kfs.tank.sys;

import kfs.tank.IsoUtil;
import kfs.tank.World;
import kfs.tank.comp.EnemyComp;
import kfs.tank.comp.PositionComp;
import kfs.tank.comp.TankComp;
import kfs.tank.comp.PlayerComp;
import kfs.tank.comp.VelocityComp;
import kfs.tank.ecs.Entity;
import kfs.tank.ecs.KfsSystem;

import java.util.List;

public class TurretSys implements KfsSystem {

    private final World world;

    public TurretSys(World world) {
        this.world = world;
    }

    @Override
    public void update(float delta) {
        // Player turret: auto-aim at nearest enemy
        Entity player = world.getPlayer();
        if (player != null) {
            TankComp tank = world.getComponent(player, TankComp.class);
            if (tank != null) {
                float autoAngle = computeAutoAimAngle(player);
                tank.turretRotation = IsoUtil.lerpAngle(tank.turretRotation, autoAngle, tank.turretRotSpeed * delta);
            }
        }

        // Enemy turrets aim at player
        List<Entity> enemies = world.getEntitiesWith(TankComp.class);
        for (Entity e : enemies) {
            if (e == player) continue;
            PlayerComp pc = world.getComponent(e, PlayerComp.class);
            if (pc != null) continue; // skip player

            TankComp tank = world.getComponent(e, TankComp.class);
            PositionComp ePos = world.getComponent(e, PositionComp.class);
            if (tank == null || ePos == null || player == null) continue;

            PositionComp pPos = world.getComponent(player, PositionComp.class);
            if (pPos == null) continue;

            float dx = pPos.x - ePos.x;
            float dy = pPos.y - ePos.y;
            float angle = (float) Math.atan2(dy, dx);
            tank.turretRotation = IsoUtil.lerpAngle(tank.turretRotation, angle, tank.turretRotSpeed * delta);
        }
    }

    /** Compute auto-aim angle toward nearest enemy, or movement direction if no enemies. */
    private float computeAutoAimAngle(Entity player) {
        PositionComp playerPos = world.getComponent(player, PositionComp.class);
        if (playerPos == null) return 0;

        // Find nearest enemy
        List<Entity> enemies = world.getEntitiesWith(EnemyComp.class);
        Entity nearest = null;
        float nearestDist = Float.MAX_VALUE;
        for (Entity e : enemies) {
            PositionComp ep = world.getComponent(e, PositionComp.class);
            if (ep == null) continue;
            float dx = ep.x - playerPos.x;
            float dy = ep.y - playerPos.y;
            float dist = dx * dx + dy * dy;
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = e;
            }
        }

        if (nearest != null) {
            PositionComp ep = world.getComponent(nearest, PositionComp.class);
            float dx = ep.x - playerPos.x;
            float dy = ep.y - playerPos.y;
            return (float) Math.atan2(dy, dx);
        }

        // No enemies — aim in movement direction
        VelocityComp vel = world.getComponent(player, VelocityComp.class);
        if (vel != null && (Math.abs(vel.vx) > 0.1f || Math.abs(vel.vy) > 0.1f)) {
            return (float) Math.atan2(vel.vy, vel.vx);
        }

        // Fallback: keep current
        TankComp tank = world.getComponent(player, TankComp.class);
        return tank != null ? tank.turretRotation : 0;
    }
}
