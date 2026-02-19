package io.github.some_example_name.AbstractEngine.AudioManagement;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

import java.util.HashMap;
import java.util.Map;

public class SoundManager {

    private float masterVolume = 1.0f;
    private boolean isMuted = false;
    private boolean currentMusicLoop = false;

    private final Map<String, SoundEffect> soundEffects = new HashMap<>();
    private final Map<String, MusicTrack> musicTracks = new HashMap<>();

    private MusicTrack currentMusic = null;

    public void loadSound(String id, String path) {
        Sound sound = Gdx.audio.newSound(Gdx.files.internal(path));
        soundEffects.put(id, new SoundEffect(id, sound));
    }

    public void loadMusic(String id, String path) {
        Music music = Gdx.audio.newMusic(Gdx.files.internal(path));
        musicTracks.put(id, new MusicTrack(id, music));
    }

    public void playSound(String id) {
        SoundEffect sfx = soundEffects.get(id);
        if (sfx == null || isMuted) return;

        float finalVolume = clamp01(masterVolume * sfx.getVolume());
        sfx.play(finalVolume);
    }

    public void playMusic(String id, boolean loop) {
        MusicTrack track = musicTracks.get(id);
        if (track == null) return;

        if (currentMusic != null) currentMusic.stop();

        currentMusic = track;
        currentMusicLoop = loop;

        if (isMuted) return;

        currentMusic.play(loop, masterVolume);
    }
    
    public MusicTrack getMusicTrack(String id) {
        return musicTracks.get(id);
    }

    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
        }
        currentMusic = null;
    }

    public void setVolume(float volume) {
        masterVolume = clamp01(volume);

        if (!isMuted && currentMusic != null) {
            currentMusic.setMasterVolume(masterVolume);
        }
    }

    public void mute() {
        isMuted = true;

        if (currentMusic != null) {
            currentMusic.pause();
        }
    }

    public void unmute() {
        isMuted = false;

        if (currentMusic != null) {
            currentMusic.setMasterVolume(masterVolume);
            currentMusic.play(currentMusicLoop, masterVolume); 
        }
    }

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

    private float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
