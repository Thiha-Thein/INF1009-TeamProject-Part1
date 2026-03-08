package io.github.some_example_name.AbstractEngine.AudioManagement;

import java.util.List;
import io.github.some_example_name.AbstractEngine.EntityManagement.AbstractEntity;

// Bridges entity state and the SoundManager — entities request sounds via SoundEventComponent
// rather than calling SoundManager directly, keeping game logic decoupled from audio
public class AudioSystem {
    private final SoundManager soundManager;

    public AudioSystem(SoundManager soundManager) {
        this.soundManager = soundManager;
    }

    // Scans all entities for pending sound requests, fires them, then clears the request
    // so the same sound does not repeat on subsequent frames
    public void update(List<AbstractEntity> entities) {
        for (AbstractEntity e : entities) {
            SoundEventComponent ev = e.getComponent(SoundEventComponent.class);
            if (ev == null) continue;

            if (ev.playSfxId != null) {
                soundManager.playSound(ev.playSfxId);
                ev.playSfxId = null; // consume the event so it only fires once
            }
        }
    }
}
