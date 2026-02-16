package io.github.some_example_name.AbstractEngine.MovementManagement;

import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.AbstractEngine.EntityManagement.Transform;

public class MovementComponent {
    private Transform transform;
    private Vector2 direction = new Vector2();
    private float speed;

    public MovementComponent(Transform transform, float speed) {
        this.transform = transform;
        this.speed = speed;
    }

    // Called by entity
    public void setDirection(Vector2 newDirection) {
        direction.set(newDirection);
    }

    public void move(float deltaTime) {

        if (direction.isZero()) return;

        Vector2 scaled = direction.nor().scl(speed * deltaTime);
        transform.translate(scaled.x, scaled.y);
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getSpeed() {
        return speed;
    }
}
