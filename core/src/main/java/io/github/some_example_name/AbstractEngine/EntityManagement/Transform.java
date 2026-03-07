package io.github.some_example_name.AbstractEngine.EntityManagement;

import com.badlogic.gdx.math.Vector2;

public class Transform {

    private Vector2 position;
    private Vector2 size;

    public Transform(float x, float y, float width, float height) {
        this.position = new Vector2(x, y);
        this.size = new Vector2(width, height);
    }

    // Position
    public float getX() {
        return position.x;
    }

    public void setX(float x) {
        position.x = x;
    }

    public float getY() {
        return position.y;
    }

    public void setY(float y) {
        position.y = y;
    }

    // Size
    public float getWidth() {
        return size.x;
    }

    public void setWidth(float width) {
        size.x = width;
    }

    public float getHeight() {
        return size.y;
    }

    public void setHeight(float height) {
        size.y = height;
    }

    public void translate(float dx, float dy) {
        position.add(dx, dy);
    }

    public void setPosition(Vector2 position1){position = position1;}

    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getSize() {
        return size;
    }
}



