package kfs.tank.sys;

import kfs.tank.KfsConst;
import kfs.tank.World;
import kfs.tank.comp.PlayerComp;
import kfs.tank.comp.VelocityComp;
import kfs.tank.ecs.Entity;
import kfs.tank.ecs.KfsSystem;

public class FuelSys implements KfsSystem {

    private final World world;

    public FuelSys(World world) {
        this.world = world;
    }

    @Override
    public void update(float delta) {
        Entity player = world.getPlayer();
        if (player == null || world.isGameOver()) return;

        PlayerComp pc = world.getComponent(player, PlayerComp.class);
        VelocityComp vel = world.getComponent(player, VelocityComp.class);
        if (pc == null) return;

        // Update timers
        if (pc.speedBoostTimer > 0) pc.speedBoostTimer -= delta;
        if (pc.shieldTimer > 0) pc.shieldTimer -= delta;

        // Fuel drain
        boolean moving = vel != null && (Math.abs(vel.vx) > 0.1f || Math.abs(vel.vy) > 0.1f);
        float drain = moving ? pc.fuelDrainMove : pc.fuelDrainIdle;
        pc.fuel = Math.max(0, pc.fuel - drain * delta);

        // Game over immediately when fuel empty
        if (pc.fuel <= 0) {
            world.gameOver(false);
        }
    }
}
