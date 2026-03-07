package io.github.some_example_name.AbstractEngine.AudioManagement;

import com.badlogic.gdx.audio.Sound;

// Wraps a LibGDX Sound object — short, fire-and-forget clips like UI clicks and hits
// Unlike MusicTrack, Sound supports multiple overlapping instances playing simultaneously
public class SoundEffect {
    private final String id;
    private final Sound sound;
    private float volume = 1.0f; // per-effect volume, multiplied with master before playback

    public SoundEffect(String id, Sound sound) {
        this.id = id;
        this.sound = sound;
    }

    public float getVolume() { return volume; }

    // Sets per-effect volume so individual sounds can be louder or quieter than others
    public void setVolume(float volume) {
        this.volume = clamp01(volume);
    }

    // Plays the sound at the given pre-calculated final volume (master * track volume)
    public void play(float finalVolume) {
        sound.play(clamp01(finalVolume));
    }

    public void dispose() {
        sound.dispose();
    }

    // Prevents volume from going out of the 0–1 range accepted by LibGDX
    private float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
