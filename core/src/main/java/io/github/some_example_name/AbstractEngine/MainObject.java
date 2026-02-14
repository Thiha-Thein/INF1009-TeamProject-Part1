package io.github.some_example_name.AbstractEngine;

import io.github.some_example_name.AbstractEngine.Audio.SoundManager;
import io.github.some_example_name.AbstractEngine.EntityManagement.Entity;
import io.github.some_example_name.AbstractEngine.EntityManagement.Transform;
import io.github.some_example_name.AbstractEngine.IO.IOManager;

public class MainObject extends Entity {

    private final IOManager io;
    private final SoundManager sound;

    private float moveSpeed = 200f; 

    public MainObject(IOManager io, SoundManager sound) {
        this.io = io;
        this.sound = sound;
    }

    @Override
    public void start() {
        setTag("Player");
        setActive(true);

        transform = new Transform(100, 100, 32, 32);

    }

    @Override
    public void update(float deltaTime) {
        float dx = 0;
        float dy = 0;

        if (io.isDown("right")) dx += moveSpeed * deltaTime;
        if (io.isDown("left"))  dx -= moveSpeed * deltaTime;
        if (io.isDown("up"))    dy += moveSpeed * deltaTime;
        if (io.isDown("down"))  dy -= moveSpeed * deltaTime;

        transform.translate(dx, dy);

        if (io.wasPressed("jump")) {
            System.out.println("Jump pressed!");
        }

        System.out.println("MainObject Position: (" + transform.getX() + ", " + transform.getY() + ")");
    }

    @Override
    public void render() {
    }
}
