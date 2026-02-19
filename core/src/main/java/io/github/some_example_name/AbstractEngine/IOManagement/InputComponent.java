package io.github.some_example_name.AbstractEngine.IOManagement;

import java.util.HashMap;
import java.util.Map;

public class InputComponent {

    private final Map<String, String> bindings = new HashMap<>();

    private final Map<String, Boolean> down = new HashMap<>();
    private final Map<String, Boolean> pressed = new HashMap<>();
    private final Map<String, Boolean> released = new HashMap<>();

    public void bind(String entityAction, String ioAction) {
        bindings.put(entityAction, ioAction);
    }

    public Map<String, String> getBindings() {
        return bindings;
    }

    public void setState(String entityAction, boolean isDown, boolean isPressed, boolean isReleased) {
        down.put(entityAction, isDown);
        pressed.put(entityAction, isPressed);
        released.put(entityAction, isReleased);
    }

    public boolean isDown(String entityAction) {
        Boolean v = down.get(entityAction);
        return v != null && v;
    }

    public boolean wasPressed(String entityAction) {
        Boolean v = pressed.get(entityAction);
        return v != null && v;
    }

    public boolean wasReleased(String entityAction) {
        Boolean v = released.get(entityAction);
        return v != null && v;
    }
}
