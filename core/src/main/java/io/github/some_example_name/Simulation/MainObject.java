package io.github.some_example_name.Simulation;

import io.github.some_example_name.AbstractEngine.EntityManagement.*;

public class MainObject extends AbstractEntity{
    @Override
    public void start() {

        setTag("player");

        // Use inherited transform
        transform = new Transform(0, 0, 32, 32);

    }

    @Override
    public void update(float deltaTime) {

    }
}


