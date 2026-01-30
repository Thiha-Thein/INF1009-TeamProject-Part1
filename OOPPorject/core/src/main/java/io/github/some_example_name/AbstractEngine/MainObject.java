package io.github.some_example_name.AbstractEngine;

import io.github.some_example_name.AbstractEngine.EntityManagement.AbstractEntity;
import io.github.some_example_name.AbstractEngine.EntityManagement.Transform;

public class MainObject extends AbstractEntity {

    Transform transform;
    @Override
    public void start() {
        setTag("player");
        transform = new Transform(0,0,32,32);


    }

    @Override
    public void update(float deltaTime) {
        //transform.translate(1 * deltaTime,0);
        //System.out.println("X: " + transform.getX() + "Y: "+ transform.getY());//
    }

    @Override
    public void render() {

    }
}

