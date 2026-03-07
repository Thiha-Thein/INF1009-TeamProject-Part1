package io.github.some_example_name.AbstractEngine.UIManagement;

import java.util.List;

import io.github.some_example_name.AbstractEngine.EntityManagement.AbstractEntity;

// Connects entity-owned UI (UIComponent) to the central UIManager each frame
// This prevents entities from needing a reference to UIManager directly, keeping game logic and UI loosely coupled
public class UISystem {

    private final UIManager uiManager;
    private final UILayer defaultLayer; // all entity UI lands in this layer unless a more specific one is needed

    public UISystem(UIManager uiManager, UILayer defaultLayer) {
        this.uiManager = uiManager;
        this.defaultLayer = defaultLayer;
    }

    // Scans all entities for UIComponents and registers their elements into the default layer
    // UILayer.add() has a duplicate guard so re-registering the same element each frame is safe and cheap
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
