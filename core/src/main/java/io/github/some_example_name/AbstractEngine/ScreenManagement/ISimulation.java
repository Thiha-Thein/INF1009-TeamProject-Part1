package io.github.some_example_name.AbstractEngine.ScreenManagement;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

// Contract for simulation worlds hosted inside SimulationScreen
// Separating worlds behind this interface means SimulationScreen can swap between different simulations
// without knowing anything about what is inside them
public interface ISimulation {

    // Called once when the world is first loaded — spawn entities and set up the scene here
    void initialize();

    // Called every frame to advance simulation logic — physics, orbits, interactions
    void update(float deltaTime);

    // Called every frame to draw simulation visuals — planets, orbits, effects
    void render(SpriteBatch batch);

    // Called when the window resizes — recalculate viewport-dependent positions here
    void resize(int width, int height);

    // Returns the background texture so SimulationScreen can draw it before rendering entities
    Texture getBackground();

    // Releases all resources owned by this world — called before switching to a different world or screen
    void dispose();
}
