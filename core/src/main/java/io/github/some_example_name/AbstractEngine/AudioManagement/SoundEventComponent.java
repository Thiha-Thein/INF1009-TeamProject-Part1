package io.github.some_example_name.AbstractEngine.AudioManagement;

public class SoundEventComponent {
    public String playSfxId = null;

    public void request(String sfxId) {
        this.playSfxId = sfxId;
    }
}
