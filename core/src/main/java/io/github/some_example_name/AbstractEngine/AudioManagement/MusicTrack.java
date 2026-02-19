package io.github.some_example_name.AbstractEngine.AudioManagement;

import com.badlogic.gdx.audio.Music;

public class MusicTrack {

    private final String id;
    private final Music music;
    private float volume = 1.0f; 

    public MusicTrack(String id, Music music) {
        this.id = id;
        this.music = music;
    }

    public void play(boolean loop, float masterVolume) {
        music.setLooping(loop);
        music.setVolume(clamp01(masterVolume * volume));
        music.play();
    }

    public void pause() {
        if (music.isPlaying()) music.pause();
    }

    public void stop() {
        music.stop();
    }

    public void setMasterVolume(float masterVolume) {
        music.setVolume(clamp01(masterVolume * volume));
    }

    public void setVolume(float volume) {
        this.volume = clamp01(volume);
    }

    public float getVolume() {
        return volume;
    }

    public void dispose() {
        music.dispose();
    }

    private float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
