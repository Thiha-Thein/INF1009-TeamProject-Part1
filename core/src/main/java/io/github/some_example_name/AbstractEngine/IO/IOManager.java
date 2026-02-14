package io.github.some_example_name.AbstractEngine.IO;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Gdx;

public class IOManager {
    private final Map<String, InputBinding> bindings = new HashMap<>();
    private final Keyboard keyboard = new Keyboard();
    private final Mouse mouse = new Mouse();

    private final Set<String> downActions = new HashSet<>();
    private final Set<String> justPressed = new HashSet<>();
    private final Set<String> justReleased = new HashSet<>();

    public void bindKey(String action, int keyCode) {
        bindings.put(action, InputBinding.key(action, keyCode));
    }

    public void bindMouse(String action, int button) {
        bindings.put(action, InputBinding.mouse(action, button));
    }

    public void update() {
        justPressed.clear();
        justReleased.clear();

        keyboard.update();
        mouse.update();

        for (InputBinding b : bindings.values()) {
            boolean isDownNow = isBindingDown(b);
            boolean wasDownBefore = downActions.contains(b.action);

            if (isDownNow && !wasDownBefore) {
                justPressed.add(b.action);
                downActions.add(b.action);
            } else if (!isDownNow && wasDownBefore) {
                justReleased.add(b.action);
                downActions.remove(b.action);
            }
        }
    }

    private boolean isBindingDown(InputBinding b) {
        switch (b.type) {
            case KEY:
                return Gdx.input.isKeyPressed(b.keyCode);
            case MOUSE_BUTTON:
                return Gdx.input.isButtonPressed(b.mouseButton);
            default:
                return false;
        }
    }

    public boolean isDown(String action) {
        return downActions.contains(action);
    }

    public boolean wasPressed(String action) {
        return justPressed.contains(action);
    }

    public boolean wasReleased(String action) {
        return justReleased.contains(action);
    }

    public int getMouseX() { return mouse.getX(); }
    public int getMouseY() { return mouse.getY(); }

    public Keyboard getKeyboard() { return keyboard; }
    public Mouse getMouse() { return mouse; }
}
