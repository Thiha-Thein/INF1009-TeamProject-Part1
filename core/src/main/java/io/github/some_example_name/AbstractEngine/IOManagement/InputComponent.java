package io.github.some_example_name.AbstractEngine.IOManagement;

import java.util.HashMap;
import java.util.Map;

// Entity-level input state cache — maps entity-specific action names to their current frame state
// Entities define their own action vocabulary (e.g. "moveLeft") which InputSystem translates from IOManager actions (e.g. "a")
// This indirection means an entity's logic never references physical key codes directly
public class InputComponent {

    // Maps entity action name → IOManager action name (e.g. "moveLeft" → "a")
    private final Map<String, String> bindings = new HashMap<>();

    // Per-frame state snapshots written by InputSystem each update — entities read these in their own update()
    private final Map<String, Boolean> down = new HashMap<>();
    private final Map<String, Boolean> pressed = new HashMap<>();
    private final Map<String, Boolean> released = new HashMap<>();

    // Registers a mapping from a local entity action to a global IOManager action
    public void bind(String entityAction, String ioAction) {
        bindings.put(entityAction, ioAction);
    }

    // Returns all bindings so InputSystem can iterate and populate state without knowing entity internals
    public Map<String, String> getBindings() {
        return bindings;
    }

    // Written by InputSystem every frame — stores the three distinct states for a given action
    public void setState(String entityAction, boolean isDown, boolean isPressed, boolean isReleased) {
        down.put(entityAction, isDown);
        pressed.put(entityAction, isPressed);
        released.put(entityAction, isReleased);
    }

    // Returns true while the action's key or button is held down
    public boolean isDown(String entityAction) {
        Boolean v = down.get(entityAction);
        return v != null && v;
    }

    // Returns true only on the frame the action was first pressed
    public boolean wasPressed(String entityAction) {
        Boolean v = pressed.get(entityAction);
        return v != null && v;
    }

    // Returns true only on the frame the action was released
    public boolean wasReleased(String entityAction) {
        Boolean v = released.get(entityAction);
        return v != null && v;
    }
}
