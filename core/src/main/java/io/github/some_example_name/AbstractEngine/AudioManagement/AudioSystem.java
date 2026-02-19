package io.github.some_example_name.AbstractEngine.AudioManagement;

import java.util.List;
import io.github.some_example_name.AbstractEngine.EntityManagement.AbstractEntity;

public class AudioSystem {
    private final SoundManager soundManager;

    public AudioSystem(SoundManager soundManager) {
        this.soundManager = soundManager;
    }

    public void update(List<AbstractEntity> entities) {
        for (AbstractEntity e : entities) {
            SoundEventComponent ev = e.getComponent(SoundEventComponent.class);
            if (ev == null) continue;

            if (ev.playSfxId != null) {
                soundManager.playSound(ev.playSfxId);
                ev.playSfxId = null; 
            }
        }
    }
}
