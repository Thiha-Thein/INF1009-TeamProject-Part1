package io.github.some_example_name.AbstractEngine.IOManagement;

// Discriminates between the two physical input sources supported by InputBinding
// Used by IOManager to decide which LibGDX API to poll when checking binding state
public enum InputType {
    KEY,          // keyboard key, polled via Gdx.input.isKeyPressed
    MOUSE_BUTTON  // mouse button, polled via Gdx.input.isButtonPressed
}
