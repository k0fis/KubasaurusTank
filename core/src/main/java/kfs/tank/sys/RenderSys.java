package kfs.tank.sys;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import kfs.tank.*;
import kfs.tank.comp.*;
import kfs.tank.ecs.Entity;
import kfs.tank.ecs.KfsSystem;

import java.util.*;

public class RenderSys implements KfsSystem {

    private final World world;
    private OrthographicCamera camera;
    private Texture whiteTex;
    private BitmapFont font;
    private BitmapFont fontSmall;

    // Tile textures keyed by Tile enum
    private final Map<Tile, Texture> tileTex = new EnumMap<>(Tile.class);

    // Tank body+turret textures keyed by sprite key (player, light, heavy, sniper, swarm, boss)
    private final Map<String, Texture> tankBodyTex = new HashMap<>();
    private final Map<String, Texture> tankTurretTex = new HashMap<>();

    // Bullet textures
    private final Map<String, Texture> bulletTex = new HashMap<>();

    // Pickup textures
    private final Map<String, Texture> pickupTex = new HashMap<>();

    // Explosion textures
    private Texture[] explosionTex;

    // Weapon icons
    private final Map<String, Texture> iconTex = new HashMap<>();

    public RenderSys(World world) {
        this.world = world;
    }

    @Override
    public void init() {
        camera = new OrthographicCamera(KfsConst.SCREEN_W, KfsConst.SCREEN_H);
        camera.position.set(0, 0, 0);

        // White pixel for bars etc
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        whiteTex = new Texture(pm);
        pm.dispose();

        // Load fonts
        try {
            font = new BitmapFont(Gdx.files.internal("fonts/PressStart2P-16.fnt"));
            fontSmall = new BitmapFont(Gdx.files.internal("fonts/PressStart2P-10.fnt"));
        } catch (Exception e) {
            font = new BitmapFont();
            fontSmall = new BitmapFont();
        }

        // Load tile textures
        loadTile(Tile.GRASS, "tile_grass.png");
        loadTile(Tile.TREE, "tile_tree.png");
        loadTile(Tile.WATER, "tile_water.png");
        loadTile(Tile.SAND, "tile_sand.png");
        loadTile(Tile.ICE, "tile_ice.png");
        loadTile(Tile.ROAD, "tile_road.png");
        loadTile(Tile.BUILDING, "tile_building.png");
        loadTile(Tile.WALL, "tile_wall.png");
        loadTile(Tile.FUEL_DEPOT, "tile_fuel.png");
        loadTile(Tile.UPGRADE_CRATE, "tile_upgrade.png");
        // Spawn tiles use the grass texture
        Texture grassTex = tileTex.get(Tile.GRASS);
        tileTex.put(Tile.PLAYER_SPAWN, grassTex);
        tileTex.put(Tile.ENEMY_SPAWN_1, grassTex);
        tileTex.put(Tile.ENEMY_SPAWN_2, grassTex);
        tileTex.put(Tile.ENEMY_SPAWN_3, grassTex);
        tileTex.put(Tile.ENEMY_SPAWN_4, grassTex);

        // Load tank textures
        for (String key : new String[]{"player", "light", "heavy", "sniper", "swarm", "boss"}) {
            loadTexSafe(tankBodyTex, key, "textures/tank_" + key + "_body.png");
            loadTexSafe(tankTurretTex, key, "textures/tank_" + key + "_turret.png");
        }

        // Load bullet textures
        for (String key : new String[]{"cannon", "mg", "rocket", "laser", "enemy"}) {
            loadTexSafe(bulletTex, "bullet_" + key, "textures/bullet_" + key + ".png");
        }

        // Load pickup textures
        for (String key : new String[]{"fuel", "weapon", "armor", "speed", "shield"}) {
            loadTexSafe(pickupTex, "pickup_" + key, "textures/pickup_" + key + ".png");
        }

        // Load explosion frames
        explosionTex = new Texture[5];
        for (int i = 0; i < 5; i++) {
            try {
                explosionTex[i] = new Texture(Gdx.files.internal("textures/explosion_" + i + ".png"));
            } catch (Exception e) {
                explosionTex[i] = whiteTex;
            }
        }

        // Load weapon icons
        for (String key : new String[]{"cannon", "mg", "rocket", "laser"}) {
            loadTexSafe(iconTex, key, "textures/icon_" + key + ".png");
        }
    }

    private void loadTile(Tile tile, String filename) {
        try {
            tileTex.put(tile, new Texture(Gdx.files.internal("textures/" + filename)));
        } catch (Exception e) {
            tileTex.put(tile, whiteTex);
        }
    }

    private void loadTexSafe(Map<String, Texture> map, String key, String path) {
        try {
            map.put(key, new Texture(Gdx.files.internal(path)));
        } catch (Exception e) {
            map.put(key, whiteTex);
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        Entity player = world.getPlayer();
        if (player == null) return;

        PositionComp playerPos = world.getComponent(player, PositionComp.class);
        if (playerPos == null) return;

        // Camera follow player (smooth)
        float targetCamX = IsoUtil.worldToScreenX(playerPos.x, playerPos.y);
        float targetCamY = IsoUtil.worldToScreenY(playerPos.x, playerPos.y);
        camera.position.x += (targetCamX - camera.position.x) * KfsConst.CAMERA_SMOOTH;
        camera.position.y += (targetCamY - camera.position.y) * KfsConst.CAMERA_SMOOTH;
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Render tile map
        renderTiles(batch);

        // Collect and depth-sort entities
        renderEntities(batch);

        batch.end();

        // HUD (screen-space)
        renderHUD(batch);
    }

    private void renderTiles(SpriteBatch batch) {
        Tile[][] grid = world.getGrid();
        if (grid == null) return;

        int mw = world.getMapWidth();
        int mh = world.getMapHeight();
        float halfSW = KfsConst.SCREEN_W / 2f;
        float halfSH = KfsConst.SCREEN_H / 2f;

        for (int y = 0; y < mh; y++) {
            for (int x = 0; x < mw; x++) {
                Tile tile = grid[x][y];
                float sx = IsoUtil.worldToScreenX(x, y);
                float sy = IsoUtil.worldToScreenY(x, y);

                // Frustum cull
                if (sx + KfsConst.TILE_W < camera.position.x - halfSW - KfsConst.TILE_W
                    || sx - KfsConst.TILE_W > camera.position.x + halfSW + KfsConst.TILE_W
                    || sy + 64 < camera.position.y - halfSH - 64
                    || sy - KfsConst.TILE_H > camera.position.y + halfSH + KfsConst.TILE_H) {
                    continue;
                }

                Texture tex = tileTex.get(tile);
                if (tex == null || tex == whiteTex) {
                    // Fallback: tint white texture with tile color
                    batch.setColor(tile.color);
                    batch.draw(whiteTex, sx - KfsConst.TILE_W / 2f, sy - KfsConst.TILE_H / 2f,
                        KfsConst.TILE_W, KfsConst.TILE_H);
                } else {
                    batch.setColor(Color.WHITE);
                    // Tall tiles (tree, building, wall) need offset so base diamond sits at tile position
                    int texH = tex.getHeight();
                    int extraH = texH - KfsConst.TILE_H;
                    batch.draw(tex, sx - KfsConst.TILE_W / 2f, sy - KfsConst.TILE_H / 2f,
                        KfsConst.TILE_W, texH);
                }
            }
        }
        batch.setColor(Color.WHITE);
    }

    private void renderEntities(SpriteBatch batch) {
        List<Entity> renderables = world.getEntitiesWith(PositionComp.class, RenderComp.class);

        // Sort by depth (back to front)
        renderables.sort((a, b) -> {
            PositionComp pa = world.getComponent(a, PositionComp.class);
            PositionComp pb = world.getComponent(b, PositionComp.class);
            return Float.compare(IsoUtil.depthKey(pa.x, pa.y), IsoUtil.depthKey(pb.x, pb.y));
        });

        for (Entity e : renderables) {
            PositionComp pos = world.getComponent(e, PositionComp.class);
            RenderComp render = world.getComponent(e, RenderComp.class);
            if (pos == null || render == null) continue;

            float sx = IsoUtil.worldToScreenX(pos.x, pos.y);
            float sy = IsoUtil.worldToScreenY(pos.x, pos.y);

            String key = render.spriteKey;
            TankComp tank = world.getComponent(e, TankComp.class);
            ProjectileComp proj = world.getComponent(e, ProjectileComp.class);
            PickupComp pickup = world.getComponent(e, PickupComp.class);

            if (tank != null && key != null) {
                // Render tank (body + turret)
                renderTank(batch, sx, sy, key, tank, render);
            } else if (proj != null && key != null) {
                // Render projectile
                renderBullet(batch, sx, sy, key, pos);
            } else if (pickup != null && key != null) {
                // Render pickup (bouncing)
                renderPickup(batch, sx, sy, key);
            } else {
                // Fallback: colored rectangle
                float w = render.width * KfsConst.TILE_W;
                float h = render.height * KfsConst.TILE_H;
                batch.setColor(render.color);
                batch.draw(whiteTex, sx - w / 2f, sy - h / 2f, w, h);
                batch.setColor(Color.WHITE);
            }
        }
    }

    private void renderTank(SpriteBatch batch, float sx, float sy, String key, TankComp tank, RenderComp render) {
        Texture body = tankBodyTex.get(key);
        Texture turret = tankTurretTex.get(key);

        if (body == null || body == whiteTex) {
            // Fallback colored rect
            float w = render.width * KfsConst.TILE_W;
            float h = render.height * KfsConst.TILE_H;
            batch.setColor(render.color);
            batch.draw(whiteTex, sx - w / 2f, sy - h / 2f, w / 2f, h / 2f, w, h, 1, 1,
                (float) Math.toDegrees(tank.bodyRotation), 0, 0, 1, 1, false, false);
            batch.setColor(Color.WHITE);
            return;
        }

        float bodyW = body.getWidth();
        float bodyH = body.getHeight();

        // Iso sprites are pre-rendered facing right-down (screen +X direction).
        // Screen X of movement = cos(θ) - sin(θ).  Flip when moving iso-left.
        float bodyRot = tank.bodyRotation;
        boolean flipBody = Math.cos(bodyRot) < Math.sin(bodyRot);
        // FlipX mirrors BEFORE rotation in LibGDX, so render angle = bodyRot + π
        float bodyRenderAngle = flipBody ? bodyRot + (float) Math.PI : bodyRot;
        float bodyDeg = worldAngleToIsoDeg(bodyRenderAngle);

        batch.setColor(Color.WHITE);
        batch.draw(body,
            sx - bodyW / 2f, sy - bodyH / 2f,
            bodyW / 2f, bodyH / 2f,
            bodyW, bodyH,
            1f, 1f,
            bodyDeg,
            0, 0, (int) bodyW, (int) bodyH, flipBody, false);

        // Turret on top
        if (turret != null && turret != whiteTex) {
            float turW = turret.getWidth();
            float turH = turret.getHeight();
            float turRot = tank.turretRotation;
            boolean flipTurret = Math.cos(turRot) < Math.sin(turRot);
            float turRenderAngle = flipTurret ? turRot + (float) Math.PI : turRot;
            float turretDeg = worldAngleToIsoDeg(turRenderAngle);

            batch.draw(turret,
                sx - turW / 2f, sy - turH / 2f + 2,
                turW / 2f, turH / 2f - 2,
                turW, turH,
                1f, 1f,
                turretDeg,
                0, 0, (int) turW, (int) turH, flipTurret, false);
        }

        // Shield effect: blue tinted circle
        PlayerComp pc = world.getComponent(world.getPlayer(), PlayerComp.class);
        Entity player = world.getPlayer();
        if (pc != null && pc.shieldTimer > 0 && key.equals("player")) {
            batch.setColor(0.3f, 0.3f, 1f, 0.3f + 0.1f * (float) Math.sin(System.currentTimeMillis() * 0.01));
            float shieldSize = bodyW * 1.5f;
            batch.draw(whiteTex, sx - shieldSize / 2f, sy - shieldSize / 2f, shieldSize, shieldSize);
            batch.setColor(Color.WHITE);
        }
    }

    private float worldAngleToIsoDeg(float radians) {
        // In iso view, X axis goes right-down, Y axis goes left-down
        // Convert world angle to screen rotation accounting for iso distortion
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);
        // Map through iso transform
        float isoX = (cos - sin) * 0.5f;
        float isoY = (cos + sin) * 0.25f;
        return (float) Math.toDegrees(Math.atan2(isoY, isoX));
    }

    private void renderBullet(SpriteBatch batch, float sx, float sy, String key, PositionComp pos) {
        Texture tex = bulletTex.get(key);
        if (tex == null || tex == whiteTex) {
            batch.setColor(key.contains("enemy") ? Color.RED : Color.YELLOW);
            batch.draw(whiteTex, sx - 3, sy - 3, 6, 6);
            batch.setColor(Color.WHITE);
            return;
        }

        float w = tex.getWidth();
        float h = tex.getHeight();
        batch.setColor(Color.WHITE);
        batch.draw(tex, sx - w / 2f, sy - h / 2f, w, h);
    }

    private void renderPickup(SpriteBatch batch, float sx, float sy, String key) {
        Texture tex = pickupTex.get(key);
        if (tex == null || tex == whiteTex) {
            batch.setColor(Color.CYAN);
            batch.draw(whiteTex, sx - 8, sy - 8, 16, 16);
            batch.setColor(Color.WHITE);
            return;
        }

        float w = tex.getWidth();
        float h = tex.getHeight();
        float bounce = (float) Math.sin(System.currentTimeMillis() * 0.005) * 4;
        float pulse = 1.0f + 0.1f * (float) Math.sin(System.currentTimeMillis() * 0.003);

        batch.setColor(Color.WHITE);
        batch.draw(tex,
            sx - w * pulse / 2f, sy - h * pulse / 2f + bounce + 8,
            w * pulse, h * pulse);
    }

    private void renderHUD(SpriteBatch batch) {
        Entity player = world.getPlayer();
        if (player == null) return;

        PlayerComp pc = world.getComponent(player, PlayerComp.class);
        HealthComp hc = world.getComponent(player, HealthComp.class);
        WeaponComp wc = world.getComponent(player, WeaponComp.class);
        if (pc == null || hc == null) return;

        OrthographicCamera hudCam = new OrthographicCamera(KfsConst.SCREEN_W, KfsConst.SCREEN_H);
        hudCam.position.set(KfsConst.SCREEN_W / 2f, KfsConst.SCREEN_H / 2f, 0);
        hudCam.update();

        batch.setProjectionMatrix(hudCam.combined);
        batch.begin();

        float y = KfsConst.SCREEN_H - 10;
        float barW = 150;
        float barH = 16;
        float pad = 10;

        // HP bar
        float hpFrac = (float) hc.hp / hc.maxHp;
        drawBar(batch, pad, y - barH, barW, barH, hpFrac, Color.RED, Color.DARK_GRAY);
        fontSmall.setColor(Color.WHITE);
        fontSmall.draw(batch, "HP", pad, y + 2);

        // Fuel bar
        float fuelFrac = pc.fuel / pc.maxFuel;
        Color fuelColor = fuelFrac < 0.25f ? Color.RED : Color.ORANGE;
        drawBar(batch, pad + barW + 20, y - barH, barW, barH, fuelFrac, fuelColor, Color.DARK_GRAY);
        fontSmall.draw(batch, "FUEL", pad + barW + 20, y + 2);

        // Wave
        font.setColor(Color.CYAN);
        font.draw(batch, "WAVE " + world.getCurrentWave(), KfsConst.SCREEN_W / 2f - 60, y);

        // Score
        font.setColor(Color.YELLOW);
        font.draw(batch, "SCORE: " + pc.score, KfsConst.SCREEN_W - 300, y);

        // Weapon icon + name
        if (wc != null) {
            String weaponName = wc.type.name();
            Texture icon = iconTex.get(weaponName.toLowerCase());
            float iconX = pad;
            float iconY = y - barH - 42;
            if (icon != null && icon != whiteTex) {
                batch.setColor(Color.WHITE);
                batch.draw(icon, iconX, iconY, 32, 32);
                fontSmall.setColor(Color.LIME);
                fontSmall.draw(batch, weaponName, iconX + 38, iconY + 22);
            } else {
                fontSmall.setColor(Color.LIME);
                fontSmall.draw(batch, weaponName, iconX, iconY + 22);
            }
        }

        // Armor
        if (hc.armor > 0) {
            fontSmall.setColor(Color.GRAY);
            fontSmall.draw(batch, "ARM: " + (int)(hc.armor * 100) + "%", pad + 160, y - barH - 20);
        }

        // Shield indicator
        if (pc.shieldTimer > 0) {
            fontSmall.setColor(Color.BLUE);
            fontSmall.draw(batch, "SHIELD " + (int) pc.shieldTimer + "s", pad + 280, y - barH - 20);
        }

        // Speed boost indicator
        if (pc.speedBoostTimer > 0) {
            fontSmall.setColor(Color.YELLOW);
            fontSmall.draw(batch, "SPEED! " + (int) pc.speedBoostTimer + "s", pad + 440, y - barH - 20);
        }

        // Low fuel warning (blinking)
        if (pc.fuel > 0 && pc.fuel < KfsConst.FUEL_WARNING) {
            boolean blink = (System.currentTimeMillis() / 300) % 2 == 0;
            if (blink) {
                font.setColor(Color.RED);
                int secs = (int)(pc.fuel / pc.fuelDrainMove);
                font.draw(batch, "LOW FUEL! ~" + secs + "s",
                    KfsConst.SCREEN_W / 2f - 120, KfsConst.SCREEN_H / 2f);
            }
        }

        // Between waves message
        if (world.getWaveTimer() > 0 && world.getCurrentWave() > 0) {
            font.setColor(Color.GREEN);
            font.draw(batch, "NEXT WAVE IN " + (int)(world.getWaveTimer() + 1),
                KfsConst.SCREEN_W / 2f - 130, KfsConst.SCREEN_H / 2f + 40);
        }

        // Enemies alive count
        fontSmall.setColor(Color.WHITE);
        fontSmall.draw(batch, "ENEMIES: " + world.getEntitiesWith(EnemyComp.class).size(),
            KfsConst.SCREEN_W - 200, y - barH - 20);

        batch.end();
    }

    private void drawBar(SpriteBatch batch, float x, float y, float w, float h, float frac, Color fillColor, Color bgColor) {
        batch.setColor(bgColor);
        batch.draw(whiteTex, x, y, w, h);
        batch.setColor(fillColor);
        batch.draw(whiteTex, x, y, w * Math.max(0, Math.min(1, frac)), h);
        batch.setColor(Color.WHITE);
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    @Override
    public void done() {
        if (whiteTex != null) whiteTex.dispose();
        if (font != null) font.dispose();
        if (fontSmall != null) fontSmall.dispose();
        // Dispose loaded textures (but not whiteTex aliases)
        for (Texture t : tileTex.values()) {
            if (t != null && t != whiteTex) t.dispose();
        }
        for (Texture t : tankBodyTex.values()) {
            if (t != null && t != whiteTex) t.dispose();
        }
        for (Texture t : tankTurretTex.values()) {
            if (t != null && t != whiteTex) t.dispose();
        }
        for (Texture t : bulletTex.values()) {
            if (t != null && t != whiteTex) t.dispose();
        }
        for (Texture t : pickupTex.values()) {
            if (t != null && t != whiteTex) t.dispose();
        }
        for (Texture t : iconTex.values()) {
            if (t != null && t != whiteTex) t.dispose();
        }
        if (explosionTex != null) {
            for (Texture t : explosionTex) {
                if (t != null && t != whiteTex) t.dispose();
            }
        }
    }
}
