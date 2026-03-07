package io.github.some_example_name.AbstractEngine.EntityManagement;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

// Core lifecycle contract every entity must implement — EntityManager calls these at the appropriate stage
public interface iEntity {

    // Called once after the entity is added to the world and all dependencies are ready
    // Use this for setup that requires other entities or components to already exist (e.g. finding a parent transform)
    void start();

    // Called every frame with the elapsed time in seconds — all per-frame logic belongs here
    void update(float deltaTime);

    // Called when the window or viewport is resized — override to reposition UI or recalculate layout-dependent values
    void resize(int width, int height);
}
