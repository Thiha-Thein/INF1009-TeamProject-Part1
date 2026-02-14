package io.github.some_example_name.AbstractEngine.Audio;

import com.badlogic.gdx.audio.Music;

public class MusicTrack {
    private final String id;
    private final Music music;

    public MusicTrack(String id, Music music) {
        this.id = id;
        this.music = music;
    }

    public String getId() { return id; }

    public void play(boolean loop, float finalVolume) {
        music.setLooping(loop);
        music.setVolume(finalVolume);
        music.play();
    }

    public void stop() {
        music.stop();
    }

    public void pause() {
        music.pause();
    }

    public void dispose() {
        music.dispose();
    }
}
