package io.github.some_example_name.AbstractEngine.UIManagement;

import java.util.List;

import io.github.some_example_name.AbstractEngine.IOManagement.IOManager;

// Handles UI click detection — kept separate from UIManager so UIManager stays a pure renderer
// and this system can be replaced or extended (e.g. with hover highlighting) without touching rendering code
public class UIInputSystem {

    private final IOManager ioManager;
    private final UIManager uiManager;

    public UIInputSystem(IOManager ioManager, UIManager uiManager) {
        this.ioManager = ioManager;
        this.uiManager = uiManager;
    }

    // Called each frame with unprojected world-space mouse coordinates
    // Only processes a click on the single frame it is first pressed — held clicks do not retrigger
    public void update(float mx, float my) {

        if (!ioManager.wasPressed("leftClick"))
            return;

        // Iterate layers and elements in order — first button hit consumes the click so overlapping buttons do not both fire
        for (UILayer layer : uiManager.getLayers()) {
            for (UIElement element : layer.getElements()) {

                if (element instanceof UIButton) {

                    UIButton button = (UIButton) element;

                    if (button.isVisible() && button.contains(mx, my)) {
                        button.click();
                        return; // stop after the first hit to prevent click-through to elements underneath
                    }
                }
            }
        }
    }
}
