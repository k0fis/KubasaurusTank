package kfs.tank;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;

import java.util.ArrayList;
import java.util.List;

public class MusicManager {

    private final List<Music> tracks = new ArrayList<>();
    private int currentTrack = 0;

    public MusicManager(String folderPath) {
        FileHandle folder = Gdx.files.internal(folderPath);
        if (folder.exists() && folder.isDirectory()) {
            for (FileHandle file : folder.list()) {
                if (file.extension().equalsIgnoreCase("mp3")) {
                    Music music = Gdx.audio.newMusic(file);
                    music.setLooping(false);
                    music.setVolume(0.5f);
                    tracks.add(music);
                }
            }
        }

        for (Music music : tracks) {
            music.setOnCompletionListener(this::playNextTrack);
        }
        Gdx.app.log("MusicManager", "tracks loaded: " + tracks.size());
    }

    public void play() {
        if (!tracks.isEmpty()) {
            currentTrack = 0;
            tracks.get(currentTrack).play();
        }
    }

    private void playNextTrack(Music completed) {
        completed.stop();
        currentTrack = (currentTrack + 1) % tracks.size();
        tracks.get(currentTrack).play();
    }

    public void stop() {
        for (Music music : tracks) {
            music.stop();
        }
    }

    public boolean isPlaying() {
        for (Music music : tracks) {
            if (music.isPlaying()) return true;
        }
        return false;
    }

    public void dispose() {
        for (Music music : tracks) {
            music.dispose();
        }
        tracks.clear();
    }
}
