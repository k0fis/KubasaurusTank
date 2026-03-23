package kfs.tank;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

import java.util.HashMap;
import java.util.Map;

public class SoundManager {

    private final Map<String, Sound> sounds = new HashMap<>();
    private boolean enabled = true;

    public SoundManager() {
        loadSound("cannon", "sounds/cannon_fire.wav");
        loadSound("mg", "sounds/mg_fire.wav");
        loadSound("rocket", "sounds/rocket_fire.wav");
        loadSound("explosion", "sounds/explosion.wav");
        loadSound("pickup", "sounds/pickup.wav");
        loadSound("hit", "sounds/hit.wav");
        loadSound("game_over", "sounds/game_over.wav");
        loadSound("tank_move", "sounds/tank_move.wav");
    }

    private void loadSound(String name, String path) {
        try {
            if (Gdx.files.internal(path).exists()) {
                sounds.put(name, Gdx.audio.newSound(Gdx.files.internal(path)));
            }
        } catch (Exception e) {
            Gdx.app.log("SoundManager", "Could not load " + path);
        }
    }

    public void play(String name) {
        if (!enabled) return;
        Sound s = sounds.get(name);
        if (s != null) s.play(0.6f);
    }

    public void toggle() {
        enabled = !enabled;
    }

    public boolean isEnabled() { return enabled; }

    public void dispose() {
        sounds.values().forEach(Sound::dispose);
        sounds.clear();
    }
}
