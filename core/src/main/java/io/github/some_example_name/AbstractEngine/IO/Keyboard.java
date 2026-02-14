package io.github.some_example_name.AbstractEngine.IO;

import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.Gdx;

public class Keyboard {
    private final Set<Integer> pressedKeys = new HashSet<>();

    public void update() {
    }

    public boolean isKeyPressed(int keyCode) {
        return Gdx.input.isKeyPressed(keyCode);
    }

    public void clearPressedKeys() {
        pressedKeys.clear();
    }
}
