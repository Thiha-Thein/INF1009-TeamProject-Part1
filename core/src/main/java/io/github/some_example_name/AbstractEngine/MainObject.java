package io.github.some_example_name.AbstractEngine;
import io.github.some_example_name.AbstractEngine.EntityManagement.Entity;
import io.github.some_example_name.AbstractEngine.EntityManagement.Transform;

public class MainObject extends Entity{


    @Override
    public void start() {
        setTag("Player");
        setActive(true);
        Transform transform1 = new Transform(100,100,32,32);
    }

    @Override
    public void update(float deltaTime) {
        transform.translate(1,-2);
        System.out.println("MainObject: " + transform.getX() + transform.getY());
    }

    @Override
    public void render() {

    }
}

