package io.github.some_example_name.AbstractEngine.AIManagement;

import io.github.some_example_name.AbstractEngine.EntityManagement.AbstractEntity;
import io.github.some_example_name.AbstractEngine.MovementManagement.MovementComponent;

// Base class for all AI behaviours — subclass this to define how a specific entity moves autonomously
public class AIComponent {

    // Movement is injected so AI can reposition the entity without knowing how movement works internally
    private final MovementComponent movement;

    public AIComponent(MovementComponent movement) {
        this.movement = movement;
    }

    // Called once when the entity is first activated — override to run one-time setup logic like pathing or target acquisition
    public void start(AbstractEntity self) {}

    // Instructs the entity to jump to an absolute world position — used by subclasses after calculating a destination
    public void move(float x, float y) {
        movement.moveTo(x, y);
    }
}
