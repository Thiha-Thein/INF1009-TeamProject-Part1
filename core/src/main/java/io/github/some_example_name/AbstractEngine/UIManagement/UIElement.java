package io.github.some_example_name.AbstractEngine.UIManagement;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

// Base class for all UI elements
// Defines position, size and visibility behaviour
// Concrete UI elements such as UIButton or UILabel extend this
public abstract class UIElement {

    protected float x;
    protected float y;
    protected float width;
    protected float height;

    protected boolean visible = true;

    // Render UI element to the screen
    public abstract void render(SpriteBatch batch);

    // Optional update behaviour for animated UI
    public void update(float deltaTime) {}

    // Returns true if a point is inside this UI element
    // Used for click detection
    public boolean contains(float mx, float my) {
        return mx >= x && mx <= x + width &&
            my >= y && my <= y + height;
    }

    // Sets UI element position
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    // Sets UI element size
    public void setSize(float width, float height) {
        this.width  = width;
        this.height = height;
    }

    // Returns whether UI element should be rendered
    public boolean isVisible() {
        return visible;
    }

    // Enables or disables rendering of the UI element
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
