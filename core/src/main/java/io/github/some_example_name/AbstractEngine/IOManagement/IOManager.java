package io.github.some_example_name.AbstractEngine.IOManagement;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Gdx;

// Translates raw LibGDX key and mouse input into named logical actions
// Systems and entities query actions by name (e.g. "leftClick") rather than physical codes,
// so rebinding a key only requires changing one binding here rather than searching the codebase
public class IOManager {

    private final Map<String, InputBinding> bindings = new HashMap<>();
    private final Keyboard keyboard = new Keyboard();
    private final Mouse mouse = new Mouse();

    // Action name sets that track the three distinct input states per frame
    private final Set<String> downActions = new HashSet<>();    // currently held
    private final Set<String> justPressed = new HashSet<>();    // first frame down
    private final Set<String> justReleased = new HashSet<>();   // first frame up

    // Maps a logical action name to a keyboard key code
    public void bindKey(String action, int keyCode) {
        bindings.put(action, InputBinding.key(action, keyCode));
    }

    // Maps a logical action name to a mouse button code
    public void bindMouse(String action, int button) {
        bindings.put(action, InputBinding.mouse(action, button));
    }

    // Must be called once per frame before any system reads input state
    // Clears single-frame events from last frame then re-evaluates all bindings against current hardware state
    public void update() {
        justPressed.clear();
        justReleased.clear();

        keyboard.update();
        mouse.update();

        for (InputBinding b : bindings.values()) {
            boolean downNow = isBindingDown(b);
            boolean downBefore = downActions.contains(b.action);

            if (downNow && !downBefore) {
                // Input went from up to down — first press frame
                downActions.add(b.action);
                justPressed.add(b.action);
            } else if (!downNow && downBefore) {
                // Input went from down to up — release frame
                downActions.remove(b.action);
                justReleased.add(b.action);
            }
        }
    }

    // Checks the raw LibGDX state for a given binding — abstracts over key vs mouse type
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

    // True while the action's physical input is held down
    public boolean isDown(String action) { return downActions.contains(action); }

    // True only on the single frame when the action transitioned from up to down
    public boolean wasPressed(String action) { return justPressed.contains(action); }

    // True only on the single frame when the action transitioned from down to up
    public boolean wasReleased(String action) { return justReleased.contains(action); }

    public int getMouseX() { return mouse.getX(); }
    public int getMouseY() { return mouse.getY(); }
}
