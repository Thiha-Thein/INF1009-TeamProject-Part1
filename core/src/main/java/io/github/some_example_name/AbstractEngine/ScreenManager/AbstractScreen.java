package io.github.some_example_name.AbstractEngine.ScreenManager;

import java.util.ArrayList;
import io.github.some_example_name.AbstractEngine.EntityManagement.Entity;

public abstract class AbstractScreen {
    private boolean isActive = false;
    protected ScreenManager manager; // Add manager reference

    // Add constructor
    public AbstractScreen(ScreenManager manager) {
        this.manager = manager;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    // Lifecycle - updated signature
    public abstract void show(ArrayList<Entity> objects);
    public abstract void hide();
    public abstract void pause();
    public abstract void resume();

    // Game loop hooks
    public abstract void render();
    public abstract void update(float deltaTime);

    // Optional but useful for engine
    public void resize(int width, int height) {
        // default: do nothing
    }
}