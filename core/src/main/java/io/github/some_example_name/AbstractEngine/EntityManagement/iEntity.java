package io.github.some_example_name.AbstractEngine.EntityManagement;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface iEntity {

    void start();

    // Update method - called every frame
    // Abstract update: subclasses MUST implement this
    void update(float deltaTime);

    void resize(int width, int height);
}
