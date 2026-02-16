package io.github.some_example_name.AbstractEngine.ScreenManagement;

import com.badlogic.gdx.utils.Disposable;

import java.util.*;

public class ScreenManager implements Disposable {

    private final Map<String, AbstractScreen> screens = new HashMap<>();
    private final Deque<AbstractScreen> screenStack = new ArrayDeque<>();

    private AbstractScreen currentScreen;
    private boolean transitionState = false;

    public AbstractScreen getCurrentScreen() {
        return currentScreen;
    }

    public void resize(int width, int height) {
        if (currentScreen != null) {
            currentScreen.resize(width, height);
        }
    }

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

    public void setScreen(String name) {
        AbstractScreen next = screens.get(name);
        if (next == null)
            throw new IllegalArgumentException("No screen registered: " + name);

        beginTransition();

        if (currentScreen != null) {
            currentScreen.hide();
            currentScreen.setActive(false);
        }

        screenStack.clear();

        currentScreen = next;
        currentScreen.setActive(true);
        currentScreen.show();

        endTransition();
    }

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

    public void update(float deltaTime) {
        if (!transitionState && currentScreen != null)
            currentScreen.update(deltaTime);
    }

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
    public void dispose() {
        for (AbstractScreen screen : screens.values()) {
            screen.dispose();
        }
        screens.clear();
    }
}

