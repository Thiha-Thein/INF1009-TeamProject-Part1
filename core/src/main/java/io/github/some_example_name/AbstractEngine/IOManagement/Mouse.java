package io.github.some_example_name.AbstractEngine.IOManagement;

import com.badlogic.gdx.Gdx;

// Snapshots mouse position and click state once per frame so the rest of the engine
// reads from a stable cache rather than calling Gdx.input repeatedly mid-frame
public class Mouse {
    private int x;
    private int y;
    private boolean justClicked; // true only on the frame a touch/click was first detected

    // Called once per frame by IOManager — captures current hardware state into fields
    public void update() {
        x = Gdx.input.getX();
        y = Gdx.input.getY();
        justClicked = Gdx.input.justTouched();
    }

    public int getX() { return x; }
    public int getY() { return y; }

    // Returns true only on the frame a click or touch began — subsequent held frames return false
    public boolean wasClicked() { return justClicked; }

    // Manually clears the click flag — useful if a system wants to consume the click so nothing else reacts to it
    public void clearClick() { justClicked = false; }
}
