package io.github.some_example_name.AbstractEngine.AIManagement;

import io.github.some_example_name.AbstractEngine.EntityManagement.AbstractEntity;
import java.util.List;

// Drives AI logic for all entities that have an AIComponent attached
public class AIManager {

    // Iterates all entities each frame and runs their AI — entities without an AIComponent are skipped
    // Note: currently calls start() every frame rather than a dedicated update(); this should be revisited
    // when AI behaviours that need per-frame logic (e.g. orbit tracking, steering) are added
    public void update(List<AbstractEntity> entities, float deltaTime) {
        for (AbstractEntity entity : entities) {
            AIComponent ai = entity.getComponent(AIComponent.class);
            if (ai == null) continue;
            ai.start(entity);
        }
    }
}
