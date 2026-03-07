package io.github.some_example_name.AbstractEngine.ScreenManagement;

import com.badlogic.gdx.utils.Disposable;

import java.util.*;

// Manages the active screen and supports both direct switching and a push/pop stack for overlaid screens
// A transitionState flag blocks update/render during transitions to prevent partial-frame rendering artifacts
public class ScreenManager implements Disposable {

    private final Map<String, AbstractScreen> screens = new HashMap<>();
    private final Deque<AbstractScreen> screenStack = new ArrayDeque<>(); // used for push/pop overlay flow

    private AbstractScreen currentScreen;
    private boolean transitionState = false; // true while a transition is in progress — blocks update and render

    public AbstractScreen getCurrentScreen() {
        return currentScreen;
    }

    // Forwards resize to the active screen so it can update its viewport and UI layout
    public void resize(int width, int height) {
        if (currentScreen != null) {
            currentScreen.resize(width, height);
        }
    }

    // Registers a screen under a string key — must be called before setScreen() can reference it
    public void addScreen(String name, AbstractScreen screen) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Screen name invalid");

        if (screen == null)
            throw new IllegalArgumentException("Screen cannot be null");

        screens.put(name, screen);
    }

    public AbstractScreen getScreen(String name) {
        return screens.get(name);
    }

    // Replaces the current screen entirely — clears the stack so there is no screen to pop back to
    // Calls hide() on the old screen and show() on the new one so both can manage their lifecycle
    public void setScreen(String name) {
        AbstractScreen next = screens.get(name);
        if (next == null)
            throw new IllegalArgumentException("No screen registered: " + name);

        beginTransition();

        if (currentScreen != null) {
            currentScreen.hide();
            currentScreen.setActive(false);
        }

        screenStack.clear(); // a hard switch discards any previously pushed overlays

        currentScreen = next;
        currentScreen.setActive(true);
        currentScreen.show();

        endTransition();
    }

    // Pushes a new screen on top of the current one — current screen is paused and hidden but not disposed
    // Useful for pause menus or pop-up overlays that need to return to the previous screen on close
    public void pushScreen(AbstractScreen screen) {
        if (screen == null)
            throw new IllegalArgumentException("Screen cannot be null");

        beginTransition();

        if (currentScreen != null) {
            currentScreen.pause();
            currentScreen.hide();
            screenStack.push(currentScreen);
        }

        currentScreen = screen;
        currentScreen.setActive(true);
        currentScreen.show();

        endTransition();
    }

    // Removes the top screen and restores the one underneath — returns the restored screen
    // Returns null if the stack is empty (i.e. there is nowhere to go back to)
    public AbstractScreen popScreen() {
        if (screenStack.isEmpty())
            return null;

        beginTransition();

        if (currentScreen != null) {
            currentScreen.hide();
        }

        currentScreen = screenStack.pop();
        currentScreen.resume();
        currentScreen.setActive(true);

        endTransition();
        return currentScreen;
    }

    // Skips update if a transition is in progress to prevent logic running on a partially-initialized screen
    public void update(float deltaTime) {
        if (!transitionState && currentScreen != null)
            currentScreen.update(deltaTime);
    }

    // Skips render if a transition is in progress to prevent drawing a screen before show() completes
    public void render() {
        if (!transitionState && currentScreen != null)
            currentScreen.render();
    }

    private void beginTransition() {
        transitionState = true;
    }

    private void endTransition() {
        transitionState = false;
    }

    @Override
    // Disposes all registered screens — called at shutdown, not between screen switches
    public void dispose() {
        for (AbstractScreen screen : screens.values()) {
            screen.dispose();
        }
        screens.clear();
    }
}
