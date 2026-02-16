package io.github.some_example_name.AbstractEngine.ScreenManagement;

import com.badlogic.gdx.utils.Disposable;

public abstract class AbstractScreen implements Disposable {
    public abstract void dispose();

    protected final ScreenManager manager;
    private boolean active = false;

    public AbstractScreen(ScreenManager manager) {
        this.manager = manager;
    }

    public boolean isActive() {
        return active;
    }

    void setActive(boolean active) {
        this.active = active;
    }

    // Lifecycle
    public abstract void show();
    public abstract void hide();
    public abstract void pause();
    public abstract void resume();

    // Loop hooks
    public abstract void update(float deltaTime);
    public abstract void render();

    public void resize(int width, int height) {
        // optional override
    }
}

