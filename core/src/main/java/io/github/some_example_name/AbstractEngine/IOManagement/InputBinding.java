package io.github.some_example_name.AbstractEngine.IOManagement;

// Immutable record that pairs a logical action name with the physical input it maps to
// Using static factory methods instead of a public constructor keeps the type discrimination clear
public class InputBinding {
    public final String action;
    public final InputType type;
    public final int keyCode;       // valid only when type == KEY; -1 otherwise
    public final int mouseButton;   // valid only when type == MOUSE_BUTTON; -1 otherwise

    private InputBinding(String action, InputType type, int keyCode, int mouseButton) {
        this.action = action;
        this.type = type;
        this.keyCode = keyCode;
        this.mouseButton = mouseButton;
    }

    // Creates a keyboard binding — mouseButton is set to -1 as a sentinel since it is unused
    public static InputBinding key(String action, int keyCode) {
        return new InputBinding(action, InputType.KEY, keyCode, -1);
    }

    // Creates a mouse button binding — keyCode is set to -1 as a sentinel since it is unused
    public static InputBinding mouse(String action, int mouseButton) {
        return new InputBinding(action, InputType.MOUSE_BUTTON, -1, mouseButton);
    }
}
