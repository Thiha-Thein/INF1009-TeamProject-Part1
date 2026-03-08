package io.github.some_example_name.AbstractEngine.ScreenManagement;

import com.badlogic.gdx.utils.Disposable;

// Base class for all game screens — subclasses implement the lifecycle and loop hooks
// ScreenManager calls these methods at the right time so screens never need to manage their own activation
public abstract class AbstractScreen implements Disposable {

    // Holds a reference to the manager so screens can trigger their own transitions (e.g. "go to simulation")
    protected final ScreenManager manager;
    private boolean active = false;

    public AbstractScreen(ScreenManager manager) {
        this.manager = manager;
    }

    public boolean isActive() {
        return active;
    }

    // Package-private so only ScreenManager can flip the active flag — screens should not set themselves active
    void setActive(boolean active) {
        this.active = active;
    }

    // Lifecycle hooks — called by ScreenManager during screen transitions
    public abstract void show();    // screen is becoming visible — load assets and register UI here
    public abstract void hide();    // screen is going offscreen — pause audio or animations if needed
    public abstract void pause();   // app has lost focus (e.g. pushed to background)
    public abstract void resume();  // app has regained focus

    // Per-frame hooks called by ScreenManager every frame while this screen is active
    public abstract void update(float deltaTime);
    public abstract void render();

    // Optional — override to reposition viewport-dependent UI when the window size changes
    public void resize(int width, int height) {}

    public abstract void dispose();
}
