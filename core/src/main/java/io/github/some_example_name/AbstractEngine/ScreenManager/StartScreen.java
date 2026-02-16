package io.github.some_example_name.AbstractEngine.ScreenManager;

import java.util.ArrayList;

import io.github.some_example_name.AbstractEngine.EntityManagement.Entity;

public class StartScreen extends AbstractScreen {

    // CHANGED: This should probably be a configuration object, not StartScreen itself
    // If your UML really shows StartScreen, it might be a UML error
    // For now, keeping it as-is but consider changing to a proper config class
    private Object startOptions;  // Changed from StartScreen to Object for flexibility

    public StartScreen(ScreenManager manager) {
        super(manager);
    }

    @Override
    public void show(ArrayList<Entity> objects) {
        setActive(true);
        // For a start screen, you may ignore objects.
        // You can display UI/menu here.
    }

    @Override
    public void hide() {
        setActive(false);
    }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void update(float deltaTime) {
        if (!isActive()) return;
    }

    @Override
    public void render() {
        if (!isActive()) return;
    }

    // CHANGED: Updated getter/setter to match the new type
    public Object getStartOptions() { return startOptions; }
    public void setStartOptions(Object startOptions) { this.startOptions = startOptions; }
}