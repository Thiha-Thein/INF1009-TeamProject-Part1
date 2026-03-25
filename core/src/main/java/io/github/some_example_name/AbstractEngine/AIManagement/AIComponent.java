package io.github.some_example_name.AbstractEngine.AIManagement;

import io.github.some_example_name.AbstractEngine.EntityManagement.AbstractEntity;
import io.github.some_example_name.AbstractEngine.MovementManagement.MovementComponent;

// Base class for all AI behaviours — subclass this to define how a specific entity moves autonomously
public class AIComponent {

    // Movement is injected so AI can reposition the entity without knowing how movement works internally
    private final MovementComponent movement;

    // Guards start() so it is only ever executed once, regardless of how many times AIManager calls it
    private boolean started = false;

    public AIComponent(MovementComponent movement) {
        this.movement = movement;
    }

    // Called once on the first frame the entity is processed — override for one-time setup (pathing, target acquisition, etc.)
    public void start(AbstractEntity self) {}

    // Called every frame after start() has run — override for per-frame AI logic (steering, orbit tracking, adaptive difficulty, etc.)
    public void update(AbstractEntity self, float deltaTime) {}

    // Internal: invoked by AIManager each frame. Ensures start() runs exactly once, then delegates to update().
    final void tick(AbstractEntity self, float deltaTime) {
        if (!started) {
            start(self);
            started = true;
        }
        update(self, deltaTime);
    }

    // Instructs the entity to jump to an absolute world position — used by subclasses after calculating a destination
    public void move(float x, float y) {
        movement.moveTo(x, y);
    }
}
