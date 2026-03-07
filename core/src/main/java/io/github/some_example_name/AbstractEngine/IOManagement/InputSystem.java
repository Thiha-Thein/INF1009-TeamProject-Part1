package io.github.some_example_name.AbstractEngine.IOManagement;

import java.util.List;
import java.util.Map;

import io.github.some_example_name.AbstractEngine.EntityManagement.AbstractEntity;

// Bridges IOManager and the individual entities that care about input
// Each frame it pushes the current frame's input state into every entity's InputComponent
// so entities can query input without knowing about IOManager at all
public class InputSystem {

    private final IOManager ioManager;

    public InputSystem(IOManager ioManager) {
        this.ioManager = ioManager;
    }

    // For each entity that has an InputComponent, resolves its action bindings against IOManager's state
    // and writes the result back — entities read the result in their own update() call this same frame
    public void update(List<AbstractEntity> entities) {

        for (AbstractEntity entity : entities) {
            InputComponent input = entity.getComponent(InputComponent.class);
            if (input == null) continue;

            for (Map.Entry<String, String> e : input.getBindings().entrySet()) {
                String entityAction = e.getKey(); // the name the entity uses internally (e.g. "moveLeft")
                String ioAction = e.getValue();   // the name registered in IOManager (e.g. "a")

                boolean down = ioManager.isDown(ioAction);
                boolean pressed = ioManager.wasPressed(ioAction);
                boolean released = ioManager.wasReleased(ioAction);

                input.setState(entityAction, down, pressed, released);
            }
        }
    }
}
