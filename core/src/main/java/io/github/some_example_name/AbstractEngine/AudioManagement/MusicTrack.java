package io.github.some_example_name.AbstractEngine.AudioManagement;

import com.badlogic.gdx.audio.Music;

// Wraps a LibGDX Music object and combines a per-track volume with the global master volume
// so individual tracks can have independent loudness without losing master volume control
public class MusicTrack {

    private final String id;
    private final Music music;
    private float volume = 1.0f; // per-track volume multiplier, combined with master at playback time

    public MusicTrack(String id, Music music) {
        this.id = id;
        this.music = music;
    }

    // Starts playback applying both master and per-track volume — looping determines whether it repeats
    public void play(boolean loop, float masterVolume) {
        music.setLooping(loop);
        music.setVolume(clamp01(masterVolume * volume));
        music.play();
    }

    // Pauses without resetting position so it can resume from the same point
    public void pause() {
        if (music.isPlaying()) music.pause();
    }

    public void stop() {
        music.stop();
    }

    // Re-applies the combined volume when the master slider changes — called by SoundManager
    public void setMasterVolume(float masterVolume) {
        music.setVolume(clamp01(masterVolume * volume));
    }

    // Sets per-track volume — does not affect other tracks
    public void setVolume(float volume) {
        this.volume = clamp01(volume);
    }

    public float getVolume() {
        return volume;
    }

    public void dispose() {
        music.dispose();
    }

    // Prevents volume from going above 1 or below 0 which would cause undefined LibGDX behaviour
    private float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
