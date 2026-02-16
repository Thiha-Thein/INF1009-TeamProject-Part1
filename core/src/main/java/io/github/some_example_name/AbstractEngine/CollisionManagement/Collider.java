package io.github.some_example_name.AbstractEngine.CollisionManagement;

import io.github.some_example_name.AbstractEngine.EntityManagement.Transform;

public class Collider {

    private Transform transform;

    public Collider(Transform transform) {
        this.transform = transform;
    }

    public boolean isColliding(Collider other) {

        return transform.getX() < other.transform.getX() + other.transform.getWidth() &&
            transform.getX() + transform.getWidth() > other.transform.getX() &&
            transform.getY() < other.transform.getY() + other.transform.getHeight() &&
            transform.getY() + transform.getHeight() > other.transform.getY();
    }
}

