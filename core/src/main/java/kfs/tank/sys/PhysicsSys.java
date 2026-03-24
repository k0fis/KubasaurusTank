package kfs.tank.sys;

import kfs.tank.KfsConst;
import kfs.tank.Tile;
import kfs.tank.World;
import kfs.tank.comp.ColliderComp;
import kfs.tank.comp.PositionComp;
import kfs.tank.comp.VelocityComp;
import kfs.tank.ecs.Entity;
import kfs.tank.ecs.KfsSystem;

import java.util.List;

public class PhysicsSys implements KfsSystem {

    private final World world;

    public PhysicsSys(World world) {
        this.world = world;
    }

    @Override
    public void update(float delta) {
        List<Entity> entities = world.getEntitiesWith(PositionComp.class, VelocityComp.class);
        for (Entity e : entities) {
            PositionComp pos = world.getComponent(e, PositionComp.class);
            VelocityComp vel = world.getComponent(e, VelocityComp.class);
            if (pos == null || vel == null) continue;

            // Apply friction
            Tile tile = world.getTileAt(pos.x, pos.y);
            float friction = vel.friction;
            if (tile == Tile.ICE) {
                friction *= KfsConst.ICE_FRICTION;
            }

            if (vel.vx != 0 || vel.vy != 0) {
                float speed = (float) Math.sqrt(vel.vx * vel.vx + vel.vy * vel.vy);
                float decel = friction * delta;
                if (decel >= speed) {
                    vel.vx = 0;
                    vel.vy = 0;
                } else {
                    float factor = (speed - decel) / speed;
                    vel.vx *= factor;
                    vel.vy *= factor;
                }
            }

            // Apply velocity
            float newX = pos.x + vel.vx * delta;
            float newY = pos.y + vel.vy * delta;

            // Tile collision - check with entity radius for proper edge stopping
            ColliderComp col = world.getComponent(e, ColliderComp.class);
            float r = col != null ? col.radius : 0f;

            if (world.canPassRadius(newX, pos.y, r)) {
                pos.x = newX;
            } else {
                vel.vx = 0;
            }

            if (world.canPassRadius(pos.x, newY, r)) {
                pos.y = newY;
            } else {
                vel.vy = 0;
            }

            // Clamp to map bounds
            pos.x = Math.max(0.5f, Math.min(world.getMapWidth() - 0.5f, pos.x));
            pos.y = Math.max(0.5f, Math.min(world.getMapHeight() - 0.5f, pos.y));
        }
    }
}
