package kfs.tank;

public class KfsConst {
    // Iso tile dimensions
    public static final int TILE_W = 64;
    public static final int TILE_H = 32;

    // Map dimensions (tiles)
    public static final int MAP_W = 60;
    public static final int MAP_H = 40;

    // Display
    public static final int SCREEN_W = 1024;
    public static final int SCREEN_H = 768;
    public static final float CAMERA_SMOOTH = 0.08f;

    // Tank
    public static final float PLAYER_SPEED = 4f;
    public static final float PLAYER_ACCEL = 12f;
    public static final float PLAYER_FRICTION = 6f;
    public static final float BODY_ROT_SPEED = 5f;
    public static final float TURRET_ROT_SPEED = 8f;
    public static final int PLAYER_HP = 100;
    public static final float PLAYER_RADIUS = 0.4f;

    // Fuel
    public static final float MAX_FUEL = 100f;
    public static final float FUEL_DRAIN_IDLE = 0.3f;
    public static final float FUEL_DRAIN_MOVE = 1.0f;
    public static final float FUEL_PICKUP_AMOUNT = 30f;
    public static final float FUEL_WARNING = 15f; // warn when fuel below this

    // Weapons
    public static final float CANNON_FIRE_RATE = 0.8f;
    public static final float CANNON_DAMAGE = 30f;
    public static final float CANNON_BULLET_SPEED = 8f;
    public static final float MG_FIRE_RATE = 0.15f;
    public static final float MG_DAMAGE = 8f;
    public static final float MG_BULLET_SPEED = 12f;
    public static final float ROCKET_FIRE_RATE = 1.5f;
    public static final float ROCKET_DAMAGE = 60f;
    public static final float ROCKET_BULLET_SPEED = 6f;
    public static final float LASER_FIRE_RATE = 0.05f;
    public static final float LASER_DAMAGE = 4f;
    public static final float LASER_BULLET_SPEED = 20f;
    public static final float BULLET_LIFETIME = 3f;
    public static final float BULLET_RADIUS = 0.15f;

    // Enemies
    public static final float LIGHT_SPEED = 3f;
    public static final int LIGHT_HP = 30;
    public static final float LIGHT_AGGRO = 12f;
    public static final float LIGHT_ATTACK = 5f;
    public static final float LIGHT_FIRE_RATE = 1.2f;
    public static final float LIGHT_DAMAGE = 8f;

    public static final float HEAVY_SPEED = 1.2f;
    public static final int HEAVY_HP = 100;
    public static final float HEAVY_AGGRO = 15f;
    public static final float HEAVY_ATTACK = 8f;
    public static final float HEAVY_FIRE_RATE = 2f;
    public static final float HEAVY_DAMAGE = 25f;

    public static final float SNIPER_SPEED = 0.5f;
    public static final int SNIPER_HP = 50;
    public static final float SNIPER_AGGRO = 20f;
    public static final float SNIPER_ATTACK = 15f;
    public static final float SNIPER_FIRE_RATE = 2.5f;
    public static final float SNIPER_DAMAGE = 20f;
    public static final float SNIPER_KEEP_DIST = 10f;

    public static final float SWARM_SPEED = 5f;
    public static final int SWARM_HP = 15;
    public static final float SWARM_AGGRO = 10f;
    public static final float SWARM_ATTACK = 3f;
    public static final float SWARM_FIRE_RATE = 0.8f;
    public static final float SWARM_DAMAGE = 5f;

    public static final float ENEMY_RADIUS = 0.35f;
    public static final float ENEMY_BULLET_SPEED = 6f;

    // Scoring
    public static final int SCORE_LIGHT = 50;
    public static final int SCORE_HEAVY = 150;
    public static final int SCORE_SNIPER = 100;
    public static final int SCORE_SWARM = 25;
    public static final int SCORE_FUEL = 10;
    public static final int SCORE_UPGRADE = 25;
    public static final int SCORE_WAVE = 200;

    // Waves
    public static final float WAVE_PAUSE = 10f;
    public static final int BOSS_WAVE_INTERVAL = 5;
    public static final int BOSS_HP = 300;
    public static final float BOSS_SPEED = 1.5f;
    public static final float BOSS_DAMAGE = 40f;

    // Pickups
    public static final float SPEED_BOOST_MULT = 1.5f;
    public static final float SPEED_BOOST_DURATION = 10f;
    public static final float SHIELD_DURATION = 10f;
    public static final float ARMOR_BONUS = 0.2f;
    public static final float PICKUP_RADIUS = 0.3f;

    // Tile movement modifiers
    public static final float ICE_FRICTION = 0.5f;
    public static final float ROAD_SPEED_MULT = 1.3f;

    // UI
    public static final float BUTTON_TRANSPARENCY = 0.75f;
}
