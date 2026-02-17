package io.github.some_example_name.AbstractEngine.MovementManagement;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;
import io.github.some_example_name.AbstractEngine.EntityManagement.*;

public class MovementManager {
    private List<MovementComponent> movementComponents = new ArrayList<>();

    public void update(List<AbstractEntity> entities, float deltaTime) {

        for (AbstractEntity entity : entities) {

            MovementComponent movement = entity.getComponent(MovementComponent.class);
            if (movement == null) continue;
            movement.move(deltaTime);
        }
    }
}
