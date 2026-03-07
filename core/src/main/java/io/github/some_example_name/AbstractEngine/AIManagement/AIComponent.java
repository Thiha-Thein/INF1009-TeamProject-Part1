package io.github.some_example_name.AbstractEngine.AIManagement;

import io.github.some_example_name.AbstractEngine.EntityManagement.AbstractEntity;
import io.github.some_example_name.AbstractEngine.MovementManagement.MovementComponent;

public class AIComponent {

    private final MovementComponent movement;

    public AIComponent(MovementComponent movement) {
        this.movement = movement;
    }

    // Called once when the entity starts, not every entity needs it
    public void start(AbstractEntity self) {}

    // Applies a calculated position through MovementComponent
    public void move(float x, float y) {
        movement.moveTo(x, y);
    }
}
