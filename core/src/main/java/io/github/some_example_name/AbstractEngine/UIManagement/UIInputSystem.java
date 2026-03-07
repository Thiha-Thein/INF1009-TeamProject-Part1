package io.github.some_example_name.AbstractEngine.UIManagement;

import java.util.List;

import io.github.some_example_name.AbstractEngine.IOManagement.IOManager;

// Handles UI click detection
// Separate system so UIManager only handles rendering
public class UIInputSystem {

    private final IOManager ioManager;
    private final UIManager uiManager;

    public UIInputSystem(IOManager ioManager, UIManager uiManager) {
        this.ioManager = ioManager;
        this.uiManager = uiManager;
    }

    // Processes UI input
    public void update(float mx, float my) {

        // Only trigger when the click happens THIS frame
        if (!ioManager.wasPressed("leftClick"))
            return;

        for (UILayer layer : uiManager.getLayers()) {
            for (UIElement element : layer.getElements()) {

                if (element instanceof UIButton) {

                    UIButton button = (UIButton) element;

                    if (button.contains(mx, my)) {
                        button.click();
                        return; // stop after first hit
                    }
                }
            }
        }
    }
}
