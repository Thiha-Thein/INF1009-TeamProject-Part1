package io.github.some_example_name.AbstractEngine.IOManagement;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Gdx;

public class IOManager {

    private final Map<String, Set<InputBinding>> bindings = new HashMap<>();
    private final Mouse mouse = new Mouse();

    private final Set<String> downActions = new HashSet<>();
    private final Set<String> justPressed = new HashSet<>();
    private final Set<String> justReleased = new HashSet<>();

    public void bindKey(String action, int keyCode) {
        bindings
            .computeIfAbsent(action, k -> new HashSet<>())
            .add(InputBinding.key(action, keyCode));
    }


    public void bindMouse(String action, int button) {
        bindings
            .computeIfAbsent(action, k -> new HashSet<>())
            .add(InputBinding.mouse(action, button));
    }


    public void update() {
        justPressed.clear();
        justReleased.clear();

        mouse.update();

        for (Map.Entry<String, Set<InputBinding>> entry : bindings.entrySet()) {

            String action = entry.getKey();
            boolean isDownNow = false;

            for (InputBinding b : entry.getValue()) {
                if (isBindingDown(b)) {
                    isDownNow = true;
                    break;
                }
            }

            boolean wasDownBefore = downActions.contains(action);

            if (isDownNow && !wasDownBefore) {
                justPressed.add(action);
                downActions.add(action);
            }
            else if (!isDownNow && wasDownBefore) {
                justReleased.add(action);
                downActions.remove(action);
            }
        }

    }

    private boolean isBindingDown(InputBinding b) {
        switch (b.type) {
            case KEY:
                return Gdx.input.isKeyPressed(b.keyCode);
            case MOUSE_BUTTON:
                return Gdx.input.isButtonPressed(b.mouseButton);
        }
        return false;
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

    public int getMouseX() {
        return mouse.getX();
    }

    public int getMouseY() {
        return mouse.getY();
    }

    public Mouse getMouse() {
        return mouse;
    }
}

