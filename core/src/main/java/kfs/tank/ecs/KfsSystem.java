package kfs.tank.ecs;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface KfsSystem {
    default void init() {}
    default void done() {}
    default void update(float delta) {}
    default void render(SpriteBatch batch) {}
}
