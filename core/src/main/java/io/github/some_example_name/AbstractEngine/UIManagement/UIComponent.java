package io.github.some_example_name.AbstractEngine.UIManagement;

import java.util.ArrayList;
import java.util.List;

// Entity component that stores UI elements
// Allows game entities to attach UI without referencing UIManager
public class UIComponent {

    private final List<UIElement> elements = new ArrayList<>();

    // Adds UI element to the component
    public void addElement(UIElement element) {
        elements.add(element);
    }

    // Returns all UI elements attached to this component
    public List<UIElement> getElements() {
        return elements;
    }
}
