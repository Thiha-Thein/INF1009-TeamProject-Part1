package io.github.some_example_name.AbstractEngine.UIManagement;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

// Abstract base for all renderable UI elements — defines the common position, size, visibility contract
// Concrete types like UIButton extend this and implement their own rendering and optional update logic
public abstract class UIElement {

    protected float x;
    protected float y;
    protected float width;
    protected float height;

    protected boolean visible = true;

    // Renders this element to the screen — called by UILayer during its render pass
    public abstract void render(SpriteBatch batch);

    // Optional per-frame update — override for animated elements like pulsing buttons or progress bars
    public void update(float deltaTime) {}

    // Returns true if the given world-space point falls inside this element's rectangle
    // Default implementation handles rectangles — override for non-rectangular hit areas
    public boolean contains(float mx, float my) {
        return mx >= x && mx <= x + width &&
            my >= y && my <= y + height;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setSize(float width, float height) {
        this.width  = width;
        this.height = height;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
