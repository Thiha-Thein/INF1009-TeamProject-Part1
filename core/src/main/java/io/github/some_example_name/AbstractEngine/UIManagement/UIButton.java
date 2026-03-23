package io.github.some_example_name.AbstractEngine.UIManagement;

import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

// Simple clickable UI element.
// The hitbox automatically fits the rendered text plus padding on all sides,
// so clicking anywhere on or near the visible label always registers — regardless
// of what size was passed to setSize(). setSize() is still accepted for layout
// purposes (e.g. background rects drawn externally) but does NOT constrain the
// click area. The click area is always text-width + 2*padX by text-height + 2*padY.
public class UIButton extends UIElement {

    private String text;
    private final BitmapFont font;
    private Runnable onClick;

    // Extra click area added around the text on each side (in world-space pixels)
    private static final float PAD_X = 24f;
    private static final float PAD_Y = 18f;

    // Cached text measurements — rebuilt whenever text changes
    private final GlyphLayout glyphLayout = new GlyphLayout();
    private boolean layoutDirty = true;

    public UIButton(String text, BitmapFont font) {
        this.text = text;
        this.font = font;
        rebuildLayout();
    }

    // Updates the displayed label
    public void setText(String newText) {
        if (!newText.equals(this.text)) {
            this.text = newText;
            layoutDirty = true;
        }
    }

    public void setOnClick(Runnable action) {
        this.onClick = action;
    }

    public void click() {
        if (onClick != null)
            onClick.run();
    }

    // Hit-test against the text bounding box expanded by PAD_X / PAD_Y on every side.
    // This means the click area is always the right size for the actual rendered text,
    // regardless of any external setSize() call.
    @Override
    public boolean contains(float mx, float my) {
        if (layoutDirty) rebuildLayout();

        // Center of button rect (set externally via setPosition + setSize)
        // Fall back to text-derived center if width/height were never set
        float cx = (width  > 0) ? x + width  / 2f : x + glyphLayout.width  / 2f;
        float cy = (height > 0) ? y + height / 2f : y + glyphLayout.height / 2f;

        float halfW = glyphLayout.width  / 2f + PAD_X;
        float halfH = glyphLayout.height / 2f + PAD_Y;

        return mx >= cx - halfW && mx <= cx + halfW &&
               my >= cy - halfH && my <= cy + halfH;
    }

    // Draws the button label centered inside the button bounds
    @Override
    public void render(SpriteBatch batch) {
        if (!visible) return;
        if (layoutDirty) rebuildLayout();

        float textX = x + (width  - glyphLayout.width)  / 2f;
        float textY = y + (height + glyphLayout.height) / 2f;

        font.draw(batch, glyphLayout, textX, textY);
    }

    private void rebuildLayout() {
        glyphLayout.setText(font, text);
        layoutDirty = false;
    }
}
