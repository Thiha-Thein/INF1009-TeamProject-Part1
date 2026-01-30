package io.github.some_example_name.AbstractEngine.EntityManagement;

public class Transform {
    public float x, y;
    public float width, height;

    public Transform(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void translate(float dx, float dy) {
        x += dx;
        y += dy;
    }
}

