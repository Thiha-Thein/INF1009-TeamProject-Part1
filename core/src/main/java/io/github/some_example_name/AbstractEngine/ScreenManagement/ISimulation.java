package io.github.some_example_name.AbstractEngine.ScreenManagement;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface ISimulation {

    // Called once when the world is loaded
    void initialize();

    // Update simulation logic
    void update(float deltaTime);

    // Render simulation visuals (planets, orbits, etc)
    void render(SpriteBatch batch);

    // Resize world
    void resize(int width, int height);

    // Background texture
    Texture getBackground();

    // Clean up resources
    void dispose();
}

