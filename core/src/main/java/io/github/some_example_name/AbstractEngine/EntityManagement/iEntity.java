package io.github.some_example_name.AbstractEngine.EntityManagement;

public interface iEntity {

    void start();
    // Update method - called every frame
    // Abstract update: subclasses MUST implement this
    void update(float deltaTime);

<<<<<<< Updated upstream
    // Render method - called every frame
    void render();
=======
    void resize(int width, int height);
>>>>>>> Stashed changes
}
