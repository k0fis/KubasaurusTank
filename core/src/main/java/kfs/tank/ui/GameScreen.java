package kfs.tank.ui;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import kfs.tank.KfsConst;
import kfs.tank.KfsMain;
import kfs.tank.World;
import kfs.tank.sys.*;

public class GameScreen extends ScreenAdapter {

    private final KfsMain game;
    private World world;
    private SpriteBatch batch;
    private Stage touchStage;
    private Touchpad touchpad;
    private Texture touchpadBgTex;
    private Texture touchpadKnobTex;
    private Texture btnWeaponTex;

    public GameScreen(KfsMain game) {
        this.game = game;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(null);
        batch = new SpriteBatch();

        world = new World();
        world.setSoundManager(game.sound);
        world.loadMap("maps/arena.txt");
        world.createPlayer();
        world.spawnMapPickups();

        // Detect touch mode
        boolean touchMode = Gdx.app.getType() == Application.ApplicationType.Android
            || Gdx.app.getType() == Application.ApplicationType.iOS
            || (Gdx.app.getType() == Application.ApplicationType.WebGL
                && Gdx.input.isPeripheralAvailable(Input.Peripheral.MultitouchScreen));
        world.setTouchMode(touchMode);

        // Add systems in order
        InputSys inputSys = new InputSys(world);
        world.addSys(inputSys);
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

        // Touch UI setup
        if (touchMode) {
            setupTouchUI(inputSys);
        }

        world.setGameOverCallback((score, won) -> {
            Gdx.app.postRunnable(() -> {
                game.setScreen(new GameOverScreen(game, score, won));
            });
        });
    }

    private void setupTouchUI(InputSys inputSys) {
        touchStage = new Stage(new ScreenViewport());

        // Load touchpad textures
        try {
            touchpadBgTex = new Texture(Gdx.files.internal("textures/touchpad_bg.png"));
            touchpadKnobTex = new Texture(Gdx.files.internal("textures/touchpad_knob.png"));
        } catch (Exception e) {
            // Fallback: skip touch UI if textures missing
            touchStage = null;
            return;
        }

        // Create Touchpad
        Touchpad.TouchpadStyle tpStyle = new Touchpad.TouchpadStyle();
        tpStyle.background = new TextureRegionDrawable(touchpadBgTex);
        tpStyle.knob = new TextureRegionDrawable(touchpadKnobTex);
        touchpad = new Touchpad(10, tpStyle);
        touchpad.setSize(150, 150);
        touchpad.setPosition(20, 20);
        touchpad.getColor().a = 0.4f;
        touchStage.addActor(touchpad);

        // Pass touchpad to InputSys
        inputSys.setTouchpad(touchpad);

        // Weapon switch button (right side)
        try {
            btnWeaponTex = new Texture(Gdx.files.internal("textures/btn_weapon.png"));
            ImageButton.ImageButtonStyle btnStyle = new ImageButton.ImageButtonStyle();
            btnStyle.imageUp = new TextureRegionDrawable(btnWeaponTex);
            ImageButton weaponBtn = new ImageButton(btnStyle);
            weaponBtn.setSize(72, 72);
            weaponBtn.setPosition(Gdx.graphics.getWidth() - 92, 20);
            weaponBtn.getColor().a = 0.5f;
            weaponBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    inputSys.cyclePlayerWeapon();
                }
            });
            touchStage.addActor(weaponBtn);
        } catch (Exception e) {
            // Skip weapon button if texture missing
        }

        // InputMultiplexer: touch stage first, then existing
        InputMultiplexer mux = new InputMultiplexer();
        mux.addProcessor(touchStage);
        Gdx.input.setInputProcessor(mux);
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

        // Draw touch UI on top
        if (touchStage != null) {
            touchStage.act(delta);
            touchStage.draw();
        }
    }

    @Override
    public void dispose() {
        world.done();
        batch.dispose();
        if (touchStage != null) touchStage.dispose();
        if (touchpadBgTex != null) touchpadBgTex.dispose();
        if (touchpadKnobTex != null) touchpadKnobTex.dispose();
        if (btnWeaponTex != null) btnWeaponTex.dispose();
    }
}
