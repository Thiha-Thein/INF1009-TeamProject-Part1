package io.github.some_example_name.AbstractEngine.AudioManagement;

import com.badlogic.gdx.audio.Sound;

public class SoundEffect {
    private final String id;
    private final Sound sound;
    private float volume = 1.0f;

    private long lastPlayId = -1;

    public SoundEffect(String id, Sound sound) {
        this.id = id;
        this.sound = sound;
    }

    public String getId() { return id; }

    public void setVolume(float volume) {
        this.volume = Math.max(0f, Math.min(1f, volume));
    }

    public float getVolume() { return volume; }

    public void play(float finalVolume) {
        lastPlayId = sound.play(finalVolume);
    }

    public void stop() {
        if (lastPlayId != -1) {
            sound.stop(lastPlayId);
        } else {
            sound.stop();
        }
    }

    public void dispose() {
        sound.dispose();
    }
}
