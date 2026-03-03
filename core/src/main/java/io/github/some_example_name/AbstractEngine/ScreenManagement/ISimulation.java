package io.github.some_example_name.AbstractEngine.ScreenManagement;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface ISimulation {
    void initialize();                // create entities
    void update(float deltaTime);     // run systems
    void render(SpriteBatch batch);   // draw entities
    void resize(int width, int height);
    void dispose();                   // cleanup
    Texture getBackground();
}

