package kfs.tank.sys;

import kfs.tank.KfsConst;
import kfs.tank.World;
import kfs.tank.comp.*;
import kfs.tank.ecs.Entity;
import kfs.tank.ecs.KfsSystem;

import java.util.List;

public class EnemyAISys implements KfsSystem {

    private final World world;

    public EnemyAISys(World world) {
        this.world = world;
    }

    @Override
    public void update(float delta) {
        Entity player = world.getPlayer();
        if (player == null || world.isGameOver()) return;

        PositionComp playerPos = world.getComponent(player, PositionComp.class);
        if (playerPos == null) return;

        List<Entity> enemies = world.getEntitiesWith(EnemyComp.class);
        for (Entity e : enemies) {
            EnemyComp enemy = world.getComponent(e, EnemyComp.class);
            PositionComp pos = world.getComponent(e, PositionComp.class);
            VelocityComp vel = world.getComponent(e, VelocityComp.class);
            TankComp tank = world.getComponent(e, TankComp.class);
            WeaponComp weapon = world.getComponent(e, WeaponComp.class);
            if (enemy == null || pos == null || vel == null) continue;

            float dx = playerPos.x - pos.x;
            float dy = playerPos.y - pos.y;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);

            // State transitions
            if (dist <= enemy.attackRange) {
                enemy.state = AIState.ATTACK;
            } else if (dist <= enemy.aggroRange) {
                if (enemy.type == EnemyType.SNIPER && dist < KfsConst.SNIPER_KEEP_DIST) {
                    enemy.state = AIState.KEEP_DISTANCE;
                } else {
                    enemy.state = AIState.CHASE;
                }
            } else {
                enemy.state = AIState.IDLE;
            }

            // Behavior based on state
            float moveX = 0, moveY = 0;
            boolean wantFire = false;

            switch (enemy.state) {
                case CHASE:
                    if (dist > 0.5f) {
                        moveX = dx / dist;
                        moveY = dy / dist;
                    }
                    break;

                case ATTACK:
                    wantFire = true;
                    // Light tanks keep moving while attacking
                    if (enemy.type == EnemyType.LIGHT || enemy.type == EnemyType.SWARM) {
                        if (dist > 1.5f) {
                            moveX = dx / dist;
                            moveY = dy / dist;
                        }
                    }
                    break;

                case KEEP_DISTANCE:
                    wantFire = true;
                    if (dist < KfsConst.SNIPER_KEEP_DIST * 0.7f) {
                        // Move away from player
                        moveX = -dx / dist;
                        moveY = -dy / dist;
                    }
                    break;

                case IDLE:
                    // Random wander
                    vel.vx *= 0.95f;
                    vel.vy *= 0.95f;
                    break;
            }

            // Apply movement
            if (moveX != 0 || moveY != 0) {
                vel.vx += moveX * vel.acceleration * delta;
                vel.vy += moveY * vel.acceleration * delta;
                float curSpd = (float) Math.sqrt(vel.vx * vel.vx + vel.vy * vel.vy);
                if (curSpd > vel.maxSpeed) {
                    vel.vx = vel.vx / curSpd * vel.maxSpeed;
                    vel.vy = vel.vy / curSpd * vel.maxSpeed;
                }

                if (tank != null) {
                    float targetRot = (float) Math.atan2(moveY, moveX);
                    tank.bodyRotation = kfs.tank.IsoUtil.lerpAngle(tank.bodyRotation, targetRot, KfsConst.BODY_ROT_SPEED * delta);
                }
            }

            // Fire
            if (weapon != null) {
                weapon.firing = wantFire;
            }
        }
    }
}
