package kfs.tank;

import com.badlogic.gdx.graphics.Color;

public enum Tile {
    GRASS('.', new Color(0.3f, 0.6f, 0.2f, 1), true, false),
    TREE('#', new Color(0.1f, 0.35f, 0.1f, 1), false, false),
    WATER('~', new Color(0.1f, 0.3f, 0.7f, 1), false, false),
    SAND(',', new Color(0.85f, 0.75f, 0.45f, 1), true, false),
    ICE(':', new Color(0.7f, 0.85f, 0.95f, 1), true, false),
    ROAD('=', new Color(0.4f, 0.4f, 0.4f, 1), true, false),
    BUILDING('B', new Color(0.55f, 0.45f, 0.35f, 1), false, true),
    WALL('W', new Color(0.5f, 0.5f, 0.5f, 1), false, false),
    FUEL_DEPOT('F', new Color(0.9f, 0.2f, 0.2f, 1), true, false),
    UPGRADE_CRATE('U', new Color(0.2f, 0.6f, 0.9f, 1), true, false),
    PLAYER_SPAWN('P', new Color(0.3f, 0.6f, 0.2f, 1), true, false),
    ENEMY_SPAWN_1('1', new Color(0.3f, 0.6f, 0.2f, 1), true, false),
    ENEMY_SPAWN_2('2', new Color(0.85f, 0.75f, 0.45f, 1), true, false),
    ENEMY_SPAWN_3('3', new Color(0.4f, 0.4f, 0.4f, 1), true, false),
    ENEMY_SPAWN_4('4', new Color(0.7f, 0.85f, 0.95f, 1), true, false);

    public final char code;
    public final Color color;
    public final boolean passable;
    public final boolean destructible;

    Tile(char code, Color color, boolean passable, boolean destructible) {
        this.code = code;
        this.color = color;
        this.passable = passable;
        this.destructible = destructible;
    }

    public static Tile fromCode(char c) {
        for (Tile t : values()) {
            if (t.code == c) return t;
        }
        return GRASS;
    }

    public boolean isSpawnPoint() {
        return this == PLAYER_SPAWN || this == ENEMY_SPAWN_1 || this == ENEMY_SPAWN_2
            || this == ENEMY_SPAWN_3 || this == ENEMY_SPAWN_4;
    }
}
