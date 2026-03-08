package io.github.some_example_name.AbstractEngine.AudioManagement;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

import java.util.HashMap;
import java.util.Map;

// Central audio resource manager — owns all loaded sounds and music tracks
// and applies master volume and mute state consistently across all of them
public class SoundManager {

    private float masterVolume = 1.0f;
    private boolean isMuted = false;
    private boolean currentMusicLoop = false; // remembered so unmute can resume with correct loop setting

    private final Map<String, SoundEffect> soundEffects = new HashMap<>();
    private final Map<String, MusicTrack> musicTracks = new HashMap<>();

    // Only one music track plays at a time — others are stopped when a new one starts
    private MusicTrack currentMusic = null;

    // Loads a short sound effect from the internal assets folder and registers it by ID
    public void loadSound(String id, String path) {
        Sound sound = Gdx.audio.newSound(Gdx.files.internal(path));
        soundEffects.put(id, new SoundEffect(id, sound));
    }

    // Loads a music track from the internal assets folder and registers it by ID
    public void loadMusic(String id, String path) {
        Music music = Gdx.audio.newMusic(Gdx.files.internal(path));
        musicTracks.put(id, new MusicTrack(id, music));
    }

    // Plays a registered sound effect — silently ignored if muted or if the ID is unknown
    public void playSound(String id) {
        SoundEffect sfx = soundEffects.get(id);
        if (sfx == null || isMuted) return;

        float finalVolume = clamp01(masterVolume * sfx.getVolume());
        sfx.play(finalVolume);
    }

    // Switches to a different music track, stopping whatever was playing before
    // Playback is deferred if currently muted — loop setting is stored for when unmute is called
    public void playMusic(String id, boolean loop) {
        MusicTrack track = musicTracks.get(id);
        if (track == null) return;

        if (currentMusic != null) currentMusic.stop();

        currentMusic = track;
        currentMusicLoop = loop;

        if (isMuted) return; // track is queued but won't play until unmuted

        currentMusic.play(loop, masterVolume);
    }

    // Returns the track object so callers can adjust per-track volume (e.g. fade in menu music)
    public MusicTrack getMusicTrack(String id) {
        return musicTracks.get(id);
    }

    // Stops current music and clears the reference — no resume after this
    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
        }
        currentMusic = null;
    }

    // Updates the master volume and immediately reapplies it to the currently playing track
    public void setVolume(float volume) {
        masterVolume = clamp01(volume);

        if (!isMuted && currentMusic != null) {
            currentMusic.setMasterVolume(masterVolume);
        }
    }

    // Pauses all audio — music is paused rather than stopped to preserve playback position
    public void mute() {
        isMuted = true;

        if (currentMusic != null) {
            currentMusic.pause();
        }
    }

    // Resumes audio — restores master volume and replays current music from where it paused
    public void unmute() {
        isMuted = false;

        if (currentMusic != null) {
            currentMusic.setMasterVolume(masterVolume);
            currentMusic.play(currentMusicLoop, masterVolume);
        }
    }

    // Releases all audio assets — must be called on shutdown to avoid native memory leaks
    public void dispose() {
        for (SoundEffect sfx : soundEffects.values()) {
            sfx.dispose();
        }
        soundEffects.clear();

        for (MusicTrack track : musicTracks.values()) {
            track.dispose();
        }
        musicTracks.clear();

        currentMusic = null;
    }

    // Prevents volume from going out of the 0–1 range accepted by LibGDX audio APIs
    private float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
