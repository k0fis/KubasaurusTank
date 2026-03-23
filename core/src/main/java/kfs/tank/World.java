package kfs.tank;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import kfs.tank.comp.*;
import kfs.tank.ecs.Entity;
import kfs.tank.ecs.KfsWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class World extends KfsWorld {

    private Tile[][] grid;
    private int mapWidth, mapHeight;
    private Entity player;
    private final List<float[]> enemySpawnZones = new ArrayList<>();
    private float playerStartX, playerStartY;
    private boolean gameOver;
    private boolean playerWon;
    private int currentWave;
    private float waveTimer;
    private int enemiesAlive;
    private BiConsumer<Integer, Boolean> gameOverCallback; // (score, won)
    private SoundManager sound;

    public World() {
        super();
    }

    public void setSoundManager(SoundManager sound) { this.sound = sound; }
    public SoundManager getSoundManager() { return sound; }

    public void loadMap(String file) {
        reset();
        enemySpawnZones.clear();
        gameOver = false;
        playerWon = false;
        currentWave = 0;
        waveTimer = 0;
        enemiesAlive = 0;

        String text = Gdx.files.internal(file).readString();
        String[] lines = text.split("\n");

        mapHeight = lines.length;
        mapWidth = 0;
        for (String line : lines) {
            mapWidth = Math.max(mapWidth, line.length());
        }

        grid = new Tile[mapWidth][mapHeight];

        for (int y = 0; y < mapHeight; y++) {
            String line = lines[y];
            for (int x = 0; x < mapWidth; x++) {
                char c = x < line.length() ? line.charAt(x) : '.';
                Tile tile = Tile.fromCode(c);
                grid[x][y] = tile;

                if (tile == Tile.PLAYER_SPAWN) {
                    playerStartX = x + 0.5f;
                    playerStartY = y + 0.5f;
                } else if (tile == Tile.ENEMY_SPAWN_1 || tile == Tile.ENEMY_SPAWN_2
                        || tile == Tile.ENEMY_SPAWN_3 || tile == Tile.ENEMY_SPAWN_4) {
                    enemySpawnZones.add(new float[]{x + 0.5f, y + 0.5f});
                }
            }
        }

        // Fill missing tiles
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                if (grid[x][y] == null) grid[x][y] = Tile.GRASS;
            }
        }
    }

    public Entity createPlayer() {
        player = createEntity();
        addComponent(player, new PositionComp(playerStartX, playerStartY));
        addComponent(player, new VelocityComp(KfsConst.PLAYER_SPEED, KfsConst.PLAYER_ACCEL, KfsConst.PLAYER_FRICTION));
        addComponent(player, new TankComp(KfsConst.TURRET_ROT_SPEED));
        addComponent(player, new PlayerComp(KfsConst.MAX_FUEL, KfsConst.FUEL_DRAIN_IDLE, KfsConst.FUEL_DRAIN_MOVE));
        addComponent(player, new HealthComp(KfsConst.PLAYER_HP));
        addComponent(player, new WeaponComp(WeaponType.CANNON, KfsConst.CANNON_FIRE_RATE, KfsConst.CANNON_DAMAGE, KfsConst.CANNON_BULLET_SPEED));
        addComponent(player, new ColliderComp(KfsConst.PLAYER_RADIUS));
        addComponent(player, new RenderComp(Color.GREEN, 0.8f, 0.8f, "player"));
        return player;
    }

    public Entity spawnEnemy(EnemyType type, float x, float y) {
        Entity e = createEntity();
        addComponent(e, new PositionComp(x, y));

        float speed, aggroRange, attackRange, fireRate, damage;
        int hp;
        Color color;

        switch (type) {
            case HEAVY:
                speed = KfsConst.HEAVY_SPEED; hp = KfsConst.HEAVY_HP;
                aggroRange = KfsConst.HEAVY_AGGRO; attackRange = KfsConst.HEAVY_ATTACK;
                fireRate = KfsConst.HEAVY_FIRE_RATE; damage = KfsConst.HEAVY_DAMAGE;
                color = Color.DARK_GRAY;
                break;
            case SNIPER:
                speed = KfsConst.SNIPER_SPEED; hp = KfsConst.SNIPER_HP;
                aggroRange = KfsConst.SNIPER_AGGRO; attackRange = KfsConst.SNIPER_ATTACK;
                fireRate = KfsConst.SNIPER_FIRE_RATE; damage = KfsConst.SNIPER_DAMAGE;
                color = Color.PURPLE;
                break;
            case SWARM:
                speed = KfsConst.SWARM_SPEED; hp = KfsConst.SWARM_HP;
                aggroRange = KfsConst.SWARM_AGGRO; attackRange = KfsConst.SWARM_ATTACK;
                fireRate = KfsConst.SWARM_FIRE_RATE; damage = KfsConst.SWARM_DAMAGE;
                color = Color.ORANGE;
                break;
            default: // LIGHT
                speed = KfsConst.LIGHT_SPEED; hp = KfsConst.LIGHT_HP;
                aggroRange = KfsConst.LIGHT_AGGRO; attackRange = KfsConst.LIGHT_ATTACK;
                fireRate = KfsConst.LIGHT_FIRE_RATE; damage = KfsConst.LIGHT_DAMAGE;
                color = Color.RED;
                break;
        }

        addComponent(e, new VelocityComp(speed, speed * 3, speed * 2));
        addComponent(e, new TankComp(KfsConst.TURRET_ROT_SPEED));
        addComponent(e, new HealthComp(hp));
        addComponent(e, new EnemyComp(type, aggroRange, attackRange, fireRate, damage));
        addComponent(e, new WeaponComp(WeaponType.CANNON, fireRate, damage, KfsConst.ENEMY_BULLET_SPEED));
        addComponent(e, new ColliderComp(KfsConst.ENEMY_RADIUS));
        addComponent(e, new RenderComp(color, 0.7f, 0.7f, type.name().toLowerCase()));

        enemiesAlive++;
        return e;
    }

    public Entity spawnProjectile(float x, float y, float vx, float vy, float damage, Entity owner, boolean isPlayerBullet, WeaponType weaponType) {
        Entity e = createEntity();
        addComponent(e, new PositionComp(x, y));
        VelocityComp vel = new VelocityComp(999, 999, 0);
        vel.vx = vx;
        vel.vy = vy;
        addComponent(e, vel);
        addComponent(e, new ProjectileComp(damage, owner, KfsConst.BULLET_LIFETIME, isPlayerBullet));
        addComponent(e, new ColliderComp(KfsConst.BULLET_RADIUS));
        Color bulletColor = isPlayerBullet ? Color.YELLOW : Color.RED;
        String spriteKey;
        if (!isPlayerBullet) {
            spriteKey = "bullet_enemy";
        } else {
            switch (weaponType) {
                case MG: spriteKey = "bullet_mg"; break;
                case ROCKET: spriteKey = "bullet_rocket"; break;
                case LASER: spriteKey = "bullet_laser"; break;
                default: spriteKey = "bullet_cannon"; break;
            }
        }
        addComponent(e, new RenderComp(bulletColor, 0.2f, 0.2f, spriteKey));
        return e;
    }

    public Entity spawnPickup(PickupType type, float x, float y) {
        Entity e = createEntity();
        addComponent(e, new PositionComp(x, y));
        addComponent(e, new PickupComp(type));
        addComponent(e, new ColliderComp(KfsConst.PICKUP_RADIUS));

        Color color;
        switch (type) {
            case FUEL: color = Color.RED; break;
            case WEAPON: color = Color.CYAN; break;
            case ARMOR: color = Color.GRAY; break;
            case SPEED: color = Color.YELLOW; break;
            case SHIELD: color = Color.BLUE; break;
            default: color = Color.WHITE; break;
        }
        addComponent(e, new RenderComp(color, 0.5f, 0.5f, "pickup_" + type.name().toLowerCase()));
        return e;
    }

    public boolean canPass(float x, float y) {
        int tx = (int) x;
        int ty = (int) y;
        if (tx < 0 || ty < 0 || tx >= mapWidth || ty >= mapHeight) return false;
        return grid[tx][ty].passable;
    }

    public Tile getTileAt(int tx, int ty) {
        if (tx < 0 || ty < 0 || tx >= mapWidth || ty >= mapHeight) return Tile.WALL;
        return grid[tx][ty];
    }

    public Tile getTileAt(float x, float y) {
        return getTileAt((int) x, (int) y);
    }

    public void destroyBuilding(int tx, int ty) {
        if (tx >= 0 && ty >= 0 && tx < mapWidth && ty < mapHeight && grid[tx][ty] == Tile.BUILDING) {
            grid[tx][ty] = Tile.ROAD;
        }
    }

    public int getScore() {
        if (player == null) return 0;
        PlayerComp pc = getComponent(player, PlayerComp.class);
        return pc != null ? pc.score : 0;
    }

    public void addScore(int points) {
        if (player == null) return;
        PlayerComp pc = getComponent(player, PlayerComp.class);
        if (pc != null) pc.score += points;
    }

    public Entity getPlayer() { return player; }
    public int getMapWidth() { return mapWidth; }
    public int getMapHeight() { return mapHeight; }
    public Tile[][] getGrid() { return grid; }
    public List<float[]> getEnemySpawnZones() { return enemySpawnZones; }
    public boolean isGameOver() { return gameOver; }
    public boolean isPlayerWon() { return playerWon; }
    public int getCurrentWave() { return currentWave; }
    public void setCurrentWave(int w) { this.currentWave = w; }
    public float getWaveTimer() { return waveTimer; }
    public void setWaveTimer(float t) { this.waveTimer = t; }
    public int getEnemiesAlive() { return enemiesAlive; }
    public void setEnemiesAlive(int n) { this.enemiesAlive = n; }

    public void enemyKilled() {
        enemiesAlive = Math.max(0, enemiesAlive - 1);
    }

    public void gameOver(boolean won) {
        if (gameOver) return;
        gameOver = true;
        playerWon = won;
        if (sound != null) sound.play("game_over");
        if (gameOverCallback != null) {
            gameOverCallback.accept(getScore(), won);
        }
    }

    public void setGameOverCallback(BiConsumer<Integer, Boolean> cb) {
        this.gameOverCallback = cb;
    }
}
