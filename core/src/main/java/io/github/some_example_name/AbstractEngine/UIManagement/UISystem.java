package io.github.some_example_name.AbstractEngine.UIManagement;

import java.util.List;

import io.github.some_example_name.AbstractEngine.EntityManagement.AbstractEntity;

// System responsible for connecting UIComponents to UIManager
// Ensures UIManager never directly communicates with game logic
public class UISystem {

    private final UIManager uiManager;
    private final UILayer defaultLayer;

    public UISystem(UIManager uiManager, UILayer defaultLayer) {
        this.uiManager = uiManager;
        this.defaultLayer = defaultLayer;
    }

    // Registers UI elements from entities to the UIManager
    public void register(List<AbstractEntity> entities) {

        for (AbstractEntity entity : entities) {

            UIComponent component = entity.getComponent(UIComponent.class);

            if (component == null) continue;

            for (UIElement element : component.getElements()) {
                defaultLayer.add(element);
            }
        }
    }
}
