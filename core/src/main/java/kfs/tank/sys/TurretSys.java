package kfs.tank.sys;

import kfs.tank.IsoUtil;
import kfs.tank.World;
import kfs.tank.comp.PositionComp;
import kfs.tank.comp.TankComp;
import kfs.tank.comp.PlayerComp;
import kfs.tank.ecs.Entity;
import kfs.tank.ecs.KfsSystem;

import java.util.List;

public class TurretSys implements KfsSystem {

    private final World world;
    private float targetAngle; // set by RenderSys from mouse position

    public TurretSys(World world) {
        this.world = world;
    }

    @Override
    public void update(float delta) {
        // Player turret follows mouse
        Entity player = world.getPlayer();
        if (player != null) {
            TankComp tank = world.getComponent(player, TankComp.class);
            if (tank != null) {
                tank.turretRotation = IsoUtil.lerpAngle(tank.turretRotation, targetAngle, tank.turretRotSpeed * delta);
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

    public void setTargetAngle(float angle) {
        this.targetAngle = angle;
    }
}
