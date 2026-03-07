package io.github.some_example_name.AbstractEngine.UIManagement;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.List;

// A single rendering layer that holds a set of UI elements
// Using layers allows ordered rendering (e.g. HUD behind popups) by adding multiple UILayers to UIManager
public class UILayer {

    private final List<UIElement> elements = new ArrayList<>();

    // Adds an element to this layer — duplicate guard prevents the same element being registered twice
    // (e.g. when UISystem re-registers entity UI every frame)
    public void add(UIElement element) {
        if (!elements.contains(element))
            elements.add(element);
    }

    public void remove(UIElement element) {
        elements.remove(element);
    }

    // Advances all elements — called by UIManager each frame
    public void update(float deltaTime) {
        for (UIElement e : elements) {
            e.update(deltaTime);
        }
    }

    // Renders only visible elements — invisible elements stay registered so they can be re-shown without re-adding
    public void render(SpriteBatch batch) {
        for (UIElement e : elements) {
            if (e.isVisible())
                e.render(batch);
        }
    }

    // Exposes elements so UIInputSystem can perform hit-testing without needing its own element list
    public List<UIElement> getElements() {
        return elements;
    }
}
