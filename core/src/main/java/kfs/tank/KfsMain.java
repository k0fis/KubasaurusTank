package kfs.tank;

import com.badlogic.gdx.Game;
import kfs.tank.ui.MainScreen;

public class KfsMain extends Game {

    public MusicManager music;
    public SoundManager sound;

    @Override
    public void create() {
        music = new MusicManager("music/");
        sound = new SoundManager();
        setScreen(new MainScreen(this));
    }

    @Override
    public void dispose() {
        if (music != null) music.dispose();
        if (sound != null) sound.dispose();
    }
}
