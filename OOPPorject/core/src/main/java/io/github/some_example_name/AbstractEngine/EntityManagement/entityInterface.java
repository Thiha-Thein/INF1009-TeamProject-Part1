package io.github.some_example_name.AbstractEngine.EntityManagement;

public interface entityInterface {

    // Update method - called every frame
    // Abstract update: subclasses MUST implement this
    void update(float deltaTime);

    // Render method - called every frame
    void render();
}
