package kfs.tank.sys;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import kfs.tank.*;
import kfs.tank.comp.*;
import kfs.tank.ecs.Entity;
import kfs.tank.ecs.KfsSystem;

import java.util.*;

public class RenderSys implements KfsSystem {

    private final World world;
    private OrthographicCamera camera;
    private Texture whiteTex;
    private Texture diamondTex;
    private BitmapFont font;
    private BitmapFont fontSmall;
    private final Map<Color, Texture> tileTextures = new HashMap<>();

    public RenderSys(World world) {
        this.world = world;
    }

    @Override
    public void init() {
        camera = new OrthographicCamera(KfsConst.SCREEN_W, KfsConst.SCREEN_H);
        camera.position.set(0, 0, 0);

        // Create white pixel texture
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        whiteTex = new Texture(pm);
        pm.dispose();

        // Create diamond tile texture (64x32)
        diamondTex = createDiamondTexture(KfsConst.TILE_W, KfsConst.TILE_H, Color.WHITE);

        // Load fonts
        try {
            font = new BitmapFont(Gdx.files.internal("fonts/PressStart2P-16.fnt"));
            fontSmall = new BitmapFont(Gdx.files.internal("fonts/PressStart2P-10.fnt"));
        } catch (Exception e) {
            font = new BitmapFont();
            fontSmall = new BitmapFont();
        }
    }

    private Texture createDiamondTexture(int w, int h, Color color) {
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pm.setColor(color);

        // Draw diamond shape
        int cx = w / 2;
        int cy = h / 2;
        for (int y = 0; y < h; y++) {
            int dy = Math.abs(y - cy);
            int span = (int)((1.0f - (float)dy / cy) * cx);
            for (int x = cx - span; x <= cx + span; x++) {
                if (x >= 0 && x < w) pm.drawPixel(x, y);
            }
        }

        Texture tex = new Texture(pm);
        pm.dispose();
        return tex;
    }

    @Override
    public void render(SpriteBatch batch) {
        Entity player = world.getPlayer();
        if (player == null) return;

        PositionComp playerPos = world.getComponent(player, PositionComp.class);
        if (playerPos == null) return;

        // Update turret aim angle from mouse
        updateTurretAim(playerPos);

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

    private void updateTurretAim(PositionComp playerPos) {
        // Convert screen mouse to world coordinates
        Vector3 mouseScreen = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouseScreen);

        float mouseWorldX = IsoUtil.screenToWorldX(mouseScreen.x, mouseScreen.y);
        float mouseWorldY = IsoUtil.screenToWorldY(mouseScreen.x, mouseScreen.y);

        float dx = mouseWorldX - playerPos.x;
        float dy = mouseWorldY - playerPos.y;
        float angle = (float) Math.atan2(dy, dx);

        TurretSys turretSys = world.getSystem(TurretSys.class);
        if (turretSys != null) turretSys.setTargetAngle(angle);
    }

    private void renderTiles(SpriteBatch batch) {
        Tile[][] grid = world.getGrid();
        if (grid == null) return;

        // Calculate visible tile range based on camera
        int mw = world.getMapWidth();
        int mh = world.getMapHeight();

        for (int y = 0; y < mh; y++) {
            for (int x = 0; x < mw; x++) {
                Tile tile = grid[x][y];
                float sx = IsoUtil.worldToScreenX(x, y);
                float sy = IsoUtil.worldToScreenY(x, y);

                // Simple frustum cull
                if (sx + KfsConst.TILE_W < camera.position.x - KfsConst.SCREEN_W / 2f - KfsConst.TILE_W
                    || sx - KfsConst.TILE_W > camera.position.x + KfsConst.SCREEN_W / 2f + KfsConst.TILE_W
                    || sy + KfsConst.TILE_H < camera.position.y - KfsConst.SCREEN_H / 2f - KfsConst.TILE_H
                    || sy - KfsConst.TILE_H > camera.position.y + KfsConst.SCREEN_H / 2f + KfsConst.TILE_H) {
                    continue;
                }

                batch.setColor(tile.color);
                batch.draw(diamondTex, sx - KfsConst.TILE_W / 2f, sy - KfsConst.TILE_H / 2f,
                    KfsConst.TILE_W, KfsConst.TILE_H);
            }
        }
        batch.setColor(Color.WHITE);
    }

    private void renderEntities(SpriteBatch batch) {
        // Collect all renderable entities
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
            TankComp tank = world.getComponent(e, TankComp.class);
            if (pos == null || render == null) continue;

            float sx = IsoUtil.worldToScreenX(pos.x, pos.y);
            float sy = IsoUtil.worldToScreenY(pos.x, pos.y);

            float w = render.width * KfsConst.TILE_W;
            float h = render.height * KfsConst.TILE_H;

            // Draw body
            batch.setColor(render.color);
            batch.draw(whiteTex, sx - w / 2f, sy - h / 2f, w / 2f, h / 2f, w, h, 1, 1,
                tank != null ? (float) Math.toDegrees(tank.bodyRotation) : 0, 0, 0, 1, 1, false, false);

            // Draw turret (if tank)
            if (tank != null) {
                float tw = w * 0.4f;
                float th = h * 1.2f;
                Color turretColor = new Color(render.color.r * 0.7f, render.color.g * 0.7f, render.color.b * 0.7f, 1);
                batch.setColor(turretColor);
                batch.draw(whiteTex, sx - tw / 2f, sy - th / 4f, tw / 2f, th / 4f, tw, th, 1, 1,
                    (float) Math.toDegrees(tank.turretRotation), 0, 0, 1, 1, false, false);
            }

            // Draw pickup marker (bouncing)
            PickupComp pickup = world.getComponent(e, PickupComp.class);
            if (pickup != null) {
                float bounce = (float) Math.sin(System.currentTimeMillis() * 0.005) * 4;
                batch.setColor(render.color);
                batch.draw(diamondTex, sx - w / 2f, sy - h / 2f + bounce + 8, w, h);
            }
        }
        batch.setColor(Color.WHITE);
    }

    private void renderHUD(SpriteBatch batch) {
        Entity player = world.getPlayer();
        if (player == null) return;

        PlayerComp pc = world.getComponent(player, PlayerComp.class);
        HealthComp hc = world.getComponent(player, HealthComp.class);
        WeaponComp wc = world.getComponent(player, WeaponComp.class);
        if (pc == null || hc == null) return;

        // Switch to screen coordinates
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
        drawBar(batch, pad + barW + 20, y - barH, barW, barH, fuelFrac, Color.ORANGE, Color.DARK_GRAY);
        fontSmall.draw(batch, "FUEL", pad + barW + 20, y + 2);

        // Wave
        font.setColor(Color.CYAN);
        font.draw(batch, "WAVE " + world.getCurrentWave(), KfsConst.SCREEN_W / 2f - 60, y);

        // Score
        font.setColor(Color.YELLOW);
        font.draw(batch, "SCORE: " + pc.score, KfsConst.SCREEN_W - 300, y);

        // Weapon type
        fontSmall.setColor(Color.LIME);
        String weaponName = wc != null ? wc.type.name() : "---";
        fontSmall.draw(batch, weaponName, pad, y - barH - 18);

        // Armor
        if (hc.armor > 0) {
            fontSmall.setColor(Color.GRAY);
            fontSmall.draw(batch, "ARM: " + (int)(hc.armor * 100) + "%", pad + 120, y - barH - 18);
        }

        // Shield indicator
        if (pc.shieldTimer > 0) {
            fontSmall.setColor(Color.BLUE);
            fontSmall.draw(batch, "SHIELD " + (int) pc.shieldTimer + "s", pad + 250, y - barH - 18);
        }

        // Speed boost indicator
        if (pc.speedBoostTimer > 0) {
            fontSmall.setColor(Color.YELLOW);
            fontSmall.draw(batch, "SPEED! " + (int) pc.speedBoostTimer + "s", pad + 420, y - barH - 18);
        }

        // Fuel warning
        if (pc.fuel <= 0) {
            font.setColor(Color.RED);
            int remaining = (int)(KfsConst.FUEL_GAMEOVER_DELAY - pc.fuelEmptyTimer);
            font.draw(batch, "NO FUEL! " + remaining + "s", KfsConst.SCREEN_W / 2f - 100, KfsConst.SCREEN_H / 2f);
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
            KfsConst.SCREEN_W - 200, y - barH - 18);

        batch.end();
    }

    private void drawBar(SpriteBatch batch, float x, float y, float w, float h, float frac, Color fillColor, Color bgColor) {
        // Background
        batch.setColor(bgColor);
        batch.draw(whiteTex, x, y, w, h);
        // Fill
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
        if (diamondTex != null) diamondTex.dispose();
        if (font != null) font.dispose();
        if (fontSmall != null) fontSmall.dispose();
        tileTextures.values().forEach(Texture::dispose);
    }
}
