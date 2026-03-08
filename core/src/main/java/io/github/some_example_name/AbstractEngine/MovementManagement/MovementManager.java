package io.github.some_example_name.AbstractEngine.MovementManagement;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;
import io.github.some_example_name.AbstractEngine.EntityManagement.*;

// Drives per-frame movement for all entities that have a MovementComponent attached
// Entities without a MovementComponent are skipped silently, so not every entity needs to move
public class MovementManager {

    // Internal list is unused — movement is iterated directly from the entity list passed to update()
    private List<MovementComponent> movementComponents = new ArrayList<>();

    // Calls move() on every entity's MovementComponent — direction must have been set before this call
    // by input systems or AI so the movement applied this frame reflects the latest intent
    public void update(List<AbstractEntity> entities, float deltaTime) {

        for (AbstractEntity entity : entities) {

            MovementComponent movement = entity.getComponent(MovementComponent.class);
            if (movement == null) continue;
            movement.move(deltaTime);
        }
    }
}
