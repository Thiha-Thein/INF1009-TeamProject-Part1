package io.github.some_example_name.AbstractEngine.UIManagement;

import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

// Simple clickable UI element
// Stores position, size, label text and click behaviour
// Background drawing is handled externally by ShapeRenderer — this class only draws text
public class UIButton extends UIElement {

    private String text;
    private final BitmapFont font;
    private Runnable onClick;

    public UIButton(String text, BitmapFont font) {
        this.text = text;
        this.font = font;
    }

    // Updates the displayed label — used by OrderThePlanetsMap to refresh slot contents
    // without destroying and recreating the button
    public void setText(String text) {
        this.text = text;
    }

    public void setOnClick(Runnable action) {
        this.onClick = action;
    }

    public void click() {
        if (onClick != null)
            onClick.run();
    }

    // Returns true if the given point is inside this button's bounds
    public boolean contains(float mx, float my) {
        return mx >= x && mx <= x + width &&
               my >= y && my <= y + height;
    }

    // Draws the button label centered inside the button bounds
    // Background rect is drawn separately by the caller using ShapeRenderer
    @Override
    public void render(SpriteBatch batch) {

        if (!visible) return;

        GlyphLayout layout = new GlyphLayout(font, text);

        float textX = x + (width  - layout.width)  / 2f;
        float textY = y + (height + layout.height) / 2f;

        font.draw(batch, layout, textX, textY);
    }
}
