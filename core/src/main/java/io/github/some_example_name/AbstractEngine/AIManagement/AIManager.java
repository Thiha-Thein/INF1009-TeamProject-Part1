package io.github.some_example_name.AbstractEngine.AIManagement;

import io.github.some_example_name.AbstractEngine.EntityManagement.AbstractEntity;
import java.util.List;

public class AIManager {

    public void update(List<AbstractEntity> entities, float deltaTime) {
        for (AbstractEntity entity : entities) {
            AIComponent ai = entity.getComponent(AIComponent.class);
            if (ai == null) continue;
            ai.start(entity);
        }
    }
}
