package io.github.some_example_name.AbstractEngine.AIManagement;

import io.github.some_example_name.AbstractEngine.EntityManagement.AbstractEntity;
import java.util.List;

// Drives AI logic for all entities that have an AIComponent attached
public class AIManager {

    // Iterates all entities each frame and ticks their AI component.
    // AIComponent.tick() guarantees start() runs exactly once on the first frame,
    // then calls update() every subsequent frame — no more start() being called every frame.
    public void update(List<AbstractEntity> entities, float deltaTime) {
        for (AbstractEntity entity : entities) {
            AIComponent ai = entity.getComponent(AIComponent.class);
            if (ai == null) continue;
            ai.tick(entity, deltaTime);
        }
    }
}
