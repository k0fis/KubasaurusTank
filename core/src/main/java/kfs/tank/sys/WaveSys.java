package kfs.tank.sys;

import kfs.tank.KfsConst;
import kfs.tank.World;
import kfs.tank.comp.EnemyComp;
import kfs.tank.comp.EnemyType;
import kfs.tank.comp.PickupType;
import kfs.tank.ecs.KfsSystem;

import java.util.List;

public class WaveSys implements KfsSystem {

    private final World world;
    private boolean waveActive;
    private boolean betweenWaves;

    public WaveSys(World world) {
        this.world = world;
    }

    @Override
    public void init() {
        // Start first wave after a brief pause
        world.setWaveTimer(3f);
        betweenWaves = true;
        waveActive = false;
    }

    @Override
    public void update(float delta) {
        if (world.isGameOver()) return;

        if (betweenWaves) {
            float timer = world.getWaveTimer() - delta;
            world.setWaveTimer(timer);

            if (timer <= 0) {
                betweenWaves = false;
                startNextWave();
            }
            return;
        }

        if (waveActive) {
            // Check if all enemies are dead
            List<?> enemies = world.getEntitiesWith(EnemyComp.class);
            if (enemies.isEmpty()) {
                waveActive = false;
                betweenWaves = true;
                world.setWaveTimer(KfsConst.WAVE_PAUSE);
                world.addScore(KfsConst.SCORE_WAVE);
                spawnBetweenWavePickups();
            }
        }
    }

    private void startNextWave() {
        int wave = world.getCurrentWave() + 1;
        world.setCurrentWave(wave);
        waveActive = true;

        List<float[]> spawns = world.getEnemySpawnZones();
        if (spawns.isEmpty()) return;

        boolean isBoss = wave % KfsConst.BOSS_WAVE_INTERVAL == 0;

        if (isBoss) {
            // Boss wave - one very strong enemy
            float[] sp = spawns.get(wave % spawns.size());
            kfs.tank.ecs.Entity boss = world.spawnEnemy(EnemyType.HEAVY, sp[0], sp[1]);
            kfs.tank.comp.HealthComp bossHp = world.getComponent(boss, kfs.tank.comp.HealthComp.class);
            if (bossHp != null) {
                bossHp.maxHp = KfsConst.BOSS_HP + wave * 20;
                bossHp.hp = bossHp.maxHp;
            }
            kfs.tank.comp.VelocityComp bossVel = world.getComponent(boss, kfs.tank.comp.VelocityComp.class);
            if (bossVel != null) bossVel.maxSpeed = KfsConst.BOSS_SPEED;
            kfs.tank.comp.RenderComp bossRender = world.getComponent(boss, kfs.tank.comp.RenderComp.class);
            if (bossRender != null) {
                bossRender.width = 1.2f;
                bossRender.height = 1.2f;
                bossRender.color = com.badlogic.gdx.graphics.Color.MAGENTA;
            }
        } else {
            // Normal wave - increasing difficulty
            int lightCount = 0, heavyCount = 0, sniperCount = 0, swarmCount = 0;

            if (wave <= 2) {
                lightCount = wave + 1;
            } else if (wave <= 4) {
                lightCount = 2;
                heavyCount = wave - 2;
            } else if (wave <= 6) {
                swarmCount = wave;
                sniperCount = 1;
            } else {
                lightCount = wave / 3;
                heavyCount = wave / 4;
                sniperCount = wave / 5 + 1;
                swarmCount = wave / 2;
            }

            spawnEnemies(EnemyType.LIGHT, lightCount, spawns, wave);
            spawnEnemies(EnemyType.HEAVY, heavyCount, spawns, wave);
            spawnEnemies(EnemyType.SNIPER, sniperCount, spawns, wave);
            spawnEnemies(EnemyType.SWARM, swarmCount, spawns, wave);
        }
    }

    private void spawnEnemies(EnemyType type, int count, List<float[]> spawns, int wave) {
        for (int i = 0; i < count; i++) {
            float[] sp = spawns.get((wave + i) % spawns.size());
            // Offset slightly to avoid stacking
            float ox = (float)(Math.random() * 4 - 2);
            float oy = (float)(Math.random() * 4 - 2);
            float sx = sp[0] + ox;
            float sy = sp[1] + oy;
            // Clamp
            sx = Math.max(1, Math.min(world.getMapWidth() - 1, sx));
            sy = Math.max(1, Math.min(world.getMapHeight() - 1, sy));
            if (world.canPass(sx, sy)) {
                world.spawnEnemy(type, sx, sy);
            } else {
                world.spawnEnemy(type, sp[0], sp[1]);
            }
        }
    }

    private void spawnBetweenWavePickups() {
        // Respawn fuel at map depot locations
        world.spawnMapPickups();

        // Extra fuel at random locations
        for (int i = 0; i < 2; i++) {
            float x = 2 + (float)(Math.random() * (world.getMapWidth() - 4));
            float y = 2 + (float)(Math.random() * (world.getMapHeight() - 4));
            if (world.canPass(x, y)) {
                world.spawnPickup(PickupType.FUEL, x, y);
            }
        }

        // Random upgrade
        PickupType[] upgrades = {PickupType.WEAPON, PickupType.ARMOR, PickupType.SPEED, PickupType.SHIELD};
        PickupType upgrade = upgrades[(int)(Math.random() * upgrades.length)];
        float x = 2 + (float)(Math.random() * (world.getMapWidth() - 4));
        float y = 2 + (float)(Math.random() * (world.getMapHeight() - 4));
        if (world.canPass(x, y)) {
            world.spawnPickup(upgrade, x, y);
        }
    }
}
