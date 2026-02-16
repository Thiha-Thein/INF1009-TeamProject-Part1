package io.github.some_example_name.AbstractEngine.ScreenManager;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class ScreenManager {
	private final Map<String, AbstractScreen> screens = new HashMap<>();
	private final Deque<AbstractScreen> screenStack = new ArrayDeque<>();

	private AbstractScreen currentScreen = null;
	private boolean transitionState = false;

	public void addScreen(String name, AbstractScreen screen) {
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("Screen name cannot be null/blank");
		}
		if (screen == null) {
			throw new IllegalArgumentException("Screen cannot be null");
		}
		screens.put(name, screen);
	}

	// ADD THIS METHOD - it's in your UML
	public AbstractScreen getScreen(String name) {
		return screens.get(name);
	}

	public boolean removeScreen(String name) {
		if (name == null)
			return false;
		AbstractScreen removed = screens.remove(name);

		if (removed != null && removed == currentScreen) {
			beginTransition();
			safeHide(currentScreen);
			currentScreen = null;
			endTransition();
		}

		return removed != null;
	}

	public void pushScreen(AbstractScreen screen) {
		if (screen == null) {
			throw new IllegalArgumentException("Screen cannot be null");
		}

		beginTransition();

		// Pause/hide current
		if (currentScreen != null) {
			currentScreen.pause();
			safeHide(currentScreen);
			screenStack.push(currentScreen);
		}

		// Activate new
		currentScreen = screen;
		safeShow(currentScreen);

		endTransition();
	}

	public AbstractScreen popScreen() {
		if (screenStack.isEmpty()) {
			return null;
		}

		beginTransition();

		// Remove current
		if (currentScreen != null) {
			safeHide(currentScreen);
		}

		// Restore previous
		currentScreen = screenStack.pop();
		currentScreen.resume();
		safeShow(currentScreen);

		endTransition();
		return currentScreen;
	}

	public void setScreen(String name) {
		AbstractScreen next = screens.get(name);
		if (next == null) {
			throw new IllegalArgumentException("No screen registered with name: " + name);
		}

		beginTransition();

		if (currentScreen != null) {
			safeHide(currentScreen);
		}

		// Clear stack when you directly set a screen (typical engine behavior)
		screenStack.clear();

		currentScreen = next;
		safeShow(currentScreen);

		endTransition();
	}

	public AbstractScreen getCurrentScreen() {
		return currentScreen;
	}

	public void resize(int width, int height) {
		if (currentScreen != null) {
			currentScreen.resize(width, height);
		}
	}

	public void handleTransition() {
		// In your UML it exists, so we keep it.
		// If you later add fade/animations, you implement them here.
		// For now: no-op.
	}

	// ---- Optional engine loop helpers (recommended) ----
	public void update(float deltaTime) {
		if (transitionState)
			return; // prevents mid-transition updates
		if (currentScreen != null)
			currentScreen.update(deltaTime);
	}

	public void render() {
		if (transitionState)
			return;
		if (currentScreen != null)
			currentScreen.render();
	}

	// ---- Transition helpers ----
	private void beginTransition() {
		transitionState = true;
	}

	private void endTransition() {
		transitionState = false;
	}

	private void safeShow(AbstractScreen screen) {
	    screen.setActive(true);
	    screen.show(null);  // Pass null since ScreenManager doesn't have entities
	}

	private void safeHide(AbstractScreen screen) {
		screen.hide();
		screen.setActive(false);
	}
}