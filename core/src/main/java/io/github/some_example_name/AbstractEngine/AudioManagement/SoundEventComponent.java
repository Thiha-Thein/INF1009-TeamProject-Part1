package io.github.some_example_name.AbstractEngine.AudioManagement;

// One-shot audio request attached to an entity — game logic writes to this component
// and AudioSystem reads and clears it each frame, keeping entities decoupled from SoundManager
public class SoundEventComponent {

    // Holds the ID of the sound to play — null means no request is pending
    public String playSfxId = null;

    // Sets a pending sound request — AudioSystem will fire this on the next update then clear it
    public void request(String sfxId) {
        this.playSfxId = sfxId;
    }
}
