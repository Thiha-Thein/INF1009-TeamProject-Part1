package io.github.some_example_name.AbstractEngine.IOManagement;

import com.badlogic.gdx.Gdx;

public class Mouse {
    private int x;
    private int y;

    public void update() {
        x = Gdx.input.getX();
        y = Gdx.input.getY();
    }

    public int getX() { return x; }
    public int getY() { return y; }
}

