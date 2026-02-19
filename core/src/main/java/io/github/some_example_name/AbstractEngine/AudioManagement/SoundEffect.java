package io.github.some_example_name.AbstractEngine.AudioManagement;

import com.badlogic.gdx.audio.Sound;

public class SoundEffect {
    private final String id;
    private final Sound sound;
    private float volume = 1.0f;

    public SoundEffect(String id, Sound sound) {
        this.id = id;
        this.sound = sound;
    }

    public float getVolume() { return volume; }

    public void setVolume(float volume) {
        this.volume = clamp01(volume);
    }

    public void play(float finalVolume) {
        sound.play(clamp01(finalVolume));
    }

    public void dispose() {
        sound.dispose();
    }

    private float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
