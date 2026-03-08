package io.github.some_example_name.AbstractEngine.CollisionManagement;

import io.github.some_example_name.AbstractEngine.EntityManagement.Transform;

// Axis-Aligned Bounding Box (AABB) collider — reads size and position directly from the entity's Transform
// so the collision bounds always stay in sync with the entity's visual position without manual updates
public class Collider {

    private Transform transform;

    public Collider(Transform transform) {
        this.transform = transform;
    }

    // Standard AABB overlap test — checks all four edge conditions to confirm both axes overlap
    // Returns true only when neither axis has a gap between the two rectangles
    public boolean isColliding(Collider other) {

        return transform.getX() < other.transform.getX() + other.transform.getWidth() &&
            transform.getX() + transform.getWidth() > other.transform.getX() &&
            transform.getY() < other.transform.getY() + other.transform.getHeight() &&
            transform.getY() + transform.getHeight() > other.transform.getY();
    }
}
