package kfs.tank.sys;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import kfs.tank.IsoUtil;
import kfs.tank.KfsConst;
import kfs.tank.World;
import kfs.tank.comp.*;
import kfs.tank.ecs.Entity;
import kfs.tank.ecs.KfsSystem;

public class InputSys implements KfsSystem {

    private final World world;
    private float aimAngle;

    public InputSys(World world) {
        this.world = world;
    }

    @Override
    public void update(float delta) {
        Entity player = world.getPlayer();
        if (player == null) return;

        VelocityComp vel = world.getComponent(player, VelocityComp.class);
        TankComp tank = world.getComponent(player, TankComp.class);
        WeaponComp weapon = world.getComponent(player, WeaponComp.class);
        PlayerComp pc = world.getComponent(player, PlayerComp.class);
        PositionComp pos = world.getComponent(player, PositionComp.class);
        if (vel == null || tank == null || pos == null) return;

        // Don't move if no fuel
        if (pc != null && pc.fuel <= 0) {
            vel.vx = 0;
            vel.vy = 0;
            return;
        }

        // WASD movement
        float inputX = 0, inputY = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) inputY = -1;
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) inputY = 1;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) inputX = -1;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) inputX = 1;

        // Normalize diagonal
        float len = (float) Math.sqrt(inputX * inputX + inputY * inputY);
        if (len > 0) {
            inputX /= len;
            inputY /= len;

            // Speed boost
            float speedMult = 1f;
            if (pc != null && pc.speedBoostTimer > 0) speedMult = KfsConst.SPEED_BOOST_MULT;

            // Road boost
            if (world.getTileAt(pos.x, pos.y) == kfs.tank.Tile.ROAD) {
                speedMult *= KfsConst.ROAD_SPEED_MULT;
            }

            vel.vx += inputX * vel.acceleration * delta;
            vel.vy += inputY * vel.acceleration * delta;

            float maxSpd = vel.maxSpeed * speedMult;
            float curSpd = (float) Math.sqrt(vel.vx * vel.vx + vel.vy * vel.vy);
            if (curSpd > maxSpd) {
                vel.vx = vel.vx / curSpd * maxSpd;
                vel.vy = vel.vy / curSpd * maxSpd;
            }

            // Body rotation toward movement direction
            float targetRot = (float) Math.atan2(inputY, inputX);
            tank.bodyRotation = IsoUtil.lerpAngle(tank.bodyRotation, targetRot, KfsConst.BODY_ROT_SPEED * delta);
        }

        // Turret aim toward mouse (in world coords)
        // We need camera position from RenderSys, so we store the aim angle here
        // The TurretSys will use this
        weapon.firing = Gdx.input.isButtonPressed(Input.Buttons.LEFT)
            || Gdx.input.isKeyPressed(Input.Keys.SPACE);

        // Weapon switching
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) || Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            cycleWeapon(weapon);
        }
    }

    private void cycleWeapon(WeaponComp weapon) {
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
            case LASER:
                weapon.type = WeaponType.CANNON;
                weapon.fireRate = KfsConst.CANNON_FIRE_RATE;
                weapon.damage = KfsConst.CANNON_DAMAGE;
                weapon.bulletSpeed = KfsConst.CANNON_BULLET_SPEED;
                break;
        }
    }

    public float getAimAngle() { return aimAngle; }
    public void setAimAngle(float a) { this.aimAngle = a; }
}
