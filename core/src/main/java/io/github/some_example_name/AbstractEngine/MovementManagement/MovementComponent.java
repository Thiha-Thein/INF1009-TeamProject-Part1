package io.github.some_example_name.AbstractEngine.MovementManagement;

import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.AbstractEngine.EntityManagement.Transform;

// Holds movement intent for an entity and applies it to the Transform each frame
// Direction is set by the entity or input system; actual displacement is calculated here using speed and deltaTime
public class MovementComponent {
    private Transform transform;
    private Vector2 direction = new Vector2(); // normalized direction set externally each frame
    private float speed;                        // world units per second

    public MovementComponent(Transform transform, float speed) {
        this.transform = transform;
        this.speed = speed;
    }

    // Sets the direction the entity should move — does not move the entity immediately
    public void setDirection(Vector2 newDirection) {
        direction.set(newDirection);
    }

    // Advances the entity along its current direction scaled by speed and deltaTime
    // Zero-direction check prevents unnecessary normalization when the entity is stationary
    public void move(float deltaTime) {
        if (direction.isZero()) return;

        // Normalize to prevent diagonal movement from being faster, then scale by speed and frame time
        Vector2 scaled = direction.cpy().nor().scl(speed * deltaTime);
        transform.translate(scaled.x, scaled.y);
    }

    // Teleports the entity to an absolute world position — used by AI and orbital components
    // that calculate exact target positions rather than directional velocity
    public void moveTo(float x, float y) {
        transform.setX(x);
        transform.setY(y);
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getSpeed() {
        return speed;
    }
}
