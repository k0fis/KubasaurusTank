package kfs.tank.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import kfs.tank.KfsMain;

public class BaseScreen extends ScreenAdapter {

    protected final KfsMain game;
    protected final Texture background;
    protected final Image backgroundImage;

    protected final Stage stage;
    protected final Skin skin;
    protected final BitmapFont fontSmall;
    protected final BitmapFont fontMiddle;
    protected final BitmapFont fontBig;

    protected BaseScreen(KfsMain game, boolean darkBackground) {
        this.game = game;

        Texture bgTex;
        try {
            bgTex = new Texture(Gdx.files.internal("textures/bg_idea.jpg"));
        } catch (Exception e) {
            Pixmap bgPm = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
            bgPm.setColor(new Color(0.05f, 0.1f, 0.05f, 1));
            bgPm.fill();
            bgTex = new Texture(bgPm);
            bgPm.dispose();
        }
        this.background = bgTex;

        this.backgroundImage = new Image(background);
        stage = new Stage(new ScreenViewport());

        backgroundImage.setWidth(stage.getWidth());
        backgroundImage.setHeight(stage.getHeight());
        stage.addActor(backgroundImage);

        if (darkBackground) {
            Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pm.setColor(Color.WHITE);
            pm.fill();
            Texture overlay = new Texture(pm);
            pm.dispose();
            Image darkOverlay = new Image(overlay);
            darkOverlay.setColor(0, 0, 0, 0.75f);
            darkOverlay.setFillParent(true);
            stage.addActor(darkOverlay);
        }

        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        fontBig = new BitmapFont(Gdx.files.internal("fonts/PressStart2P-32.fnt"));
        fontMiddle = new BitmapFont(Gdx.files.internal("fonts/PressStart2P-16.fnt"));
        fontSmall = new BitmapFont(Gdx.files.internal("fonts/PressStart2P-10.fnt"));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.03f, 0.06f, 0.03f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        backgroundImage.setWidth(stage.getWidth());
        backgroundImage.setHeight(stage.getHeight());
    }

    @Override
    public void dispose() {
        background.dispose();
        stage.dispose();
        skin.dispose();
        fontSmall.dispose();
        fontBig.dispose();
        fontMiddle.dispose();
    }

    protected TextButton.TextButtonStyle getTextButtonStyle(BitmapFont font, Color fontColor) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.2f, 0.35f, 0.2f, 0.85f));
        pixmap.fill();

        Texture pixmapTex = new Texture(pixmap);
        pixmap.dispose();
        TextureRegionDrawable drawable = new TextureRegionDrawable(new TextureRegion(pixmapTex));

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = font;
        buttonStyle.up = drawable;
        buttonStyle.down = drawable.tint(Color.YELLOW);
        buttonStyle.fontColor = fontColor;
        buttonStyle.downFontColor = fontColor;

        return buttonStyle;
    }
}
