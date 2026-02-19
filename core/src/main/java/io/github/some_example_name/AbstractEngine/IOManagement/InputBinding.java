package io.github.some_example_name.AbstractEngine.IOManagement;

public class InputBinding {
    public final String action;
    public final InputType type;
    public final int keyCode;
    public final int mouseButton;

    private InputBinding(String action, InputType type, int keyCode, int mouseButton) {
        this.action = action;
        this.type = type;
        this.keyCode = keyCode;
        this.mouseButton = mouseButton;
    }

    public static InputBinding key(String action, int keyCode) {
        return new InputBinding(action, InputType.KEY, keyCode, -1);
    }

    public static InputBinding mouse(String action, int mouseButton) {
        return new InputBinding(action, InputType.MOUSE_BUTTON, -1, mouseButton);
    }
}
