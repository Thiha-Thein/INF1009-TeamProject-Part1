package io.github.some_example_name.AbstractEngine.UIManagement;

import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

// Clickable text button — extends UIElement so it participates in layer-based rendering and hit-testing
// Click behaviour is injected as a Runnable so this class has no knowledge of game-specific logic
public class UIButton extends UIElement {

    private float x, y;
    private float width, height;

    private String text;
    private BitmapFont font;

    // The action to run when this button is clicked — set via setOnClick() after construction
    private Runnable onClick;

    private boolean visible = true;

    public UIButton(String text, BitmapFont font) {
        this.text = text;
        this.font = font;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
    }

    // Registers the action to invoke when this button is clicked
    public void setOnClick(Runnable action) {
        this.onClick = action;
    }

    // Fires the registered click action — called by UIInputSystem when a left-click lands inside this button's bounds
    public void click() {
        if (onClick != null)
            onClick.run();
    }

    // Returns true if the given world-space point falls within the button's rectangular bounds — used for hit-testing
    public boolean contains(float mx, float my) {
        return mx >= x &&
            mx <= x + width &&
            my >= y &&
            my <= y + height;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    // Draws the button label centered both horizontally and vertically within the button bounds
    @Override
    public void render(SpriteBatch batch) {

        if (!visible) return;

        // GlyphLayout measures the text so we can calculate the centered offset
        GlyphLayout layout = new GlyphLayout(font, text);

        float textX = x + (width - layout.width) / 2f;
        float textY = y + (height + layout.height) / 2f; // LibGDX draws text from the baseline upward

        font.draw(batch, layout, textX, textY);
    }
}
