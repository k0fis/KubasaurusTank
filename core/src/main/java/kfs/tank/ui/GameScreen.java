package kfs.tank.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import kfs.tank.KfsConst;
import kfs.tank.KfsMain;
import kfs.tank.World;
import kfs.tank.sys.*;

public class GameScreen extends ScreenAdapter {

    private final KfsMain game;
    private World world;
    private SpriteBatch batch;

    public GameScreen(KfsMain game) {
        this.game = game;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(null);
        batch = new SpriteBatch();

        world = new World();
        world.loadMap("maps/arena.txt");
        world.createPlayer();

        // Add systems in order
        world.addSys(new InputSys(world));
        world.addSys(new PhysicsSys(world));
        world.addSys(new TurretSys(world));
        world.addSys(new ShootingSys(world));
        world.addSys(new EnemyAISys(world));
        world.addSys(new ProjectileSys(world));
        world.addSys(new CollisionSys(world));
        world.addSys(new FuelSys(world));
        world.addSys(new WaveSys(world));
        world.addSys(new RenderSys(world));

        world.init();

        world.setGameOverCallback((score, won) -> {
            Gdx.app.postRunnable(() -> {
                game.setScreen(new GameOverScreen(game, score, won));
            });
        });
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 0.05f); // cap delta to avoid physics issues

        Gdx.gl.glClearColor(0.02f, 0.04f, 0.02f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!world.isGameOver()) {
            world.update(delta);
        }
        world.render(batch);
    }

    @Override
    public void dispose() {
        world.done();
        batch.dispose();
    }
}
