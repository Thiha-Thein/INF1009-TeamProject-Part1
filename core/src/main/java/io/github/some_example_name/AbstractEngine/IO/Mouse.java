package io.github.some_example_name.AbstractEngine.IO;

import com.badlogic.gdx.Gdx;

public class Mouse {
    private int x;
    private int y;
    private boolean justClicked; 

    public void update() {
        x = Gdx.input.getX();
        y = Gdx.input.getY();
        justClicked = Gdx.input.justTouched();
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public boolean wasClicked() { return justClicked; }

    public void clearClick() { justClicked = false; }
}
