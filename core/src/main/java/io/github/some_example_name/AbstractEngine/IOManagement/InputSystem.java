package io.github.some_example_name.AbstractEngine.IOManagement;

import java.util.List;
import java.util.Map;

import io.github.some_example_name.AbstractEngine.EntityManagement.AbstractEntity;

public class InputSystem {

    private final IOManager ioManager;

    public InputSystem(IOManager ioManager) {
        this.ioManager = ioManager;
    }

    public void update(List<AbstractEntity> entities) {

        for (AbstractEntity entity : entities) {
            InputComponent input = entity.getComponent(InputComponent.class);
            if (input == null) continue;

            for (Map.Entry<String, String> e : input.getBindings().entrySet()) {
                String entityAction = e.getKey(); 
                String ioAction = e.getValue(); 

                boolean down = ioManager.isDown(ioAction);
                boolean pressed = ioManager.wasPressed(ioAction);
                boolean released = ioManager.wasReleased(ioAction);

                input.setState(entityAction, down, pressed, released);
            }
        }
    }
}
