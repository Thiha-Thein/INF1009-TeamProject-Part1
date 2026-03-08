package io.github.some_example_name.AbstractEngine.ScreenManagement;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.Map;

// Contract every simulation world must fulfil
// ISimulation sits at the Manager layer — it is driven by SimulationScreen
// and is allowed to reference engine managers directly
public interface ISimulation {

    // Called once when the world is loaded — set up entities, fonts, UI here
    void initialize();

    // Called every frame to advance simulation logic
    void update(float deltaTime);

    // Called every frame to draw the simulation
    void render(SpriteBatch batch);

    // Called when the window is resized — update viewport and reposition UI
    void resize(int width, int height);

    // Returns the background texture drawn by SimulationScreen before render()
    Texture getBackground();

    // Releases all resources owned by this world
    void dispose();

    // Optional — worlds that support minigame launch callbacks override this
    // Default empty body means worlds that do not need callbacks ignore it silently
    // This avoids SimulationScreen needing to cast to a concrete type
    default void setGameCallbacks(Map<String, Runnable> callbacks) {}
}
