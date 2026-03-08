package io.github.some_example_name.AbstractEngine.UIManagement;

import java.util.ArrayList;
import java.util.List;

// Entity component that stores UI elements belonging to this entity
// Lets game entities carry their own UI without holding a direct reference to UIManager
public class UIComponent {

    private final List<UIElement> elements = new ArrayList<>();

    // Attaches a UI element to this entity — UISystem will register it with the active UILayer each frame
    public void addElement(UIElement element) {
        elements.add(element);
    }

    // Returns all attached elements so UISystem can iterate them during registration
    public List<UIElement> getElements() {
        return elements;
    }
}
