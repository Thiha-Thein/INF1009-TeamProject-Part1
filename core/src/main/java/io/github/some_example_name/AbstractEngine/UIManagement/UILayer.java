package io.github.some_example_name.AbstractEngine.UIManagement;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.List;

// Represents one UI rendering layer
// Allows ordered UI drawing (HUD, panels, popups etc.)
public class UILayer {

    private final List<UIElement> elements = new ArrayList<>();

    // Adds UI element to this layer
    public void add(UIElement element) {
        if (!elements.contains(element))
            elements.add(element);
    }

    // Removes UI element
    public void remove(UIElement element) {
        elements.remove(element);
    }

    // Updates all UI elements in this layer
    public void update(float deltaTime) {
        for (UIElement e : elements) {
            e.update(deltaTime);
        }
    }

    // Renders UI elements in this layer
    public void render(SpriteBatch batch) {
        for (UIElement e : elements) {
            if (e.isVisible())
                e.render(batch);
        }
    }

    // Returns elements for input detection
    public List<UIElement> getElements() {
        return elements;
    }
}
