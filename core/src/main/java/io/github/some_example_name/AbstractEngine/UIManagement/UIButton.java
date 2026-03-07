package io.github.some_example_name.AbstractEngine.UIManagement;

import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

/*
 * UIButton
 *
 * Simple clickable UI element.
 * Stores position, size and click behaviour.
 */
public class UIButton extends UIElement {

    private float x, y;
    private float width, height;

    private String text;
    private BitmapFont font;

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

    public void setOnClick(Runnable action) {
        this.onClick = action;
    }

    public void click() {
        if (onClick != null)
            onClick.run();
    }

    /*
     * Returns true if the mouse is inside the button bounds
     */
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

    /*
     * Draws the button text centered inside the button bounds
     */
    @Override
    public void render(SpriteBatch batch) {

        if (!visible) return;

        GlyphLayout layout = new GlyphLayout(font, text);

        float textX = x + (width - layout.width) / 2f;
        float textY = y + (height + layout.height) / 2f;

        font.draw(batch, layout, textX, textY);
    }
}
