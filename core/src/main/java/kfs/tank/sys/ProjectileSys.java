package kfs.tank.sys;

import kfs.tank.World;
import kfs.tank.comp.PositionComp;
import kfs.tank.comp.ProjectileComp;
import kfs.tank.comp.VelocityComp;
import kfs.tank.ecs.Entity;
import kfs.tank.ecs.KfsSystem;

import java.util.ArrayList;
import java.util.List;

public class ProjectileSys implements KfsSystem {

    private final World world;

    public ProjectileSys(World world) {
        this.world = world;
    }

    @Override
    public void update(float delta) {
        List<Entity> toRemove = new ArrayList<>();
        List<Entity> projectiles = world.getEntitiesWith(ProjectileComp.class);

        for (Entity e : projectiles) {
            ProjectileComp proj = world.getComponent(e, ProjectileComp.class);
            PositionComp pos = world.getComponent(e, PositionComp.class);
            if (proj == null) continue;

            proj.lifetime -= delta;
            if (proj.lifetime <= 0) {
                toRemove.add(e);
                continue;
            }

            // Remove if out of map
            if (pos != null) {
                if (pos.x < 0 || pos.y < 0 || pos.x >= world.getMapWidth() || pos.y >= world.getMapHeight()) {
                    toRemove.add(e);
                    continue;
                }
                // Remove if hitting impassable tile
                if (!world.canPass(pos.x, pos.y)) {
                    // Destroy buildings
                    if (world.getTileAt(pos.x, pos.y) == kfs.tank.Tile.BUILDING) {
                        world.destroyBuilding((int) pos.x, (int) pos.y);
                    }
                    toRemove.add(e);
                }
            }
        }

        for (Entity e : toRemove) {
            world.deleteEntity(e);
        }
    }
}
