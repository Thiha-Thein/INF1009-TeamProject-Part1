package io.github.some_example_name.AbstractEngine.EntityManagement;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

// Simple static sprite renderer for entities that only need a single image with no animation
// For animated entities, use AnimationRenderer instead — AbstractEntity prefers AnimationRenderer when both are set
public class SpriteRenderer {

    private Texture texture;
    private boolean visible = true;

    public SpriteRenderer(String texturePath) {
        this.texture = new Texture(texturePath);
    }

    // Draws the texture stretched to exactly fill the entity's transform bounds
    public void render(SpriteBatch batch, Transform transform) {
        if (!visible) return;

        batch.draw(
            texture,
            transform.getX(),
            transform.getY(),
            transform.getWidth(),
            transform.getHeight()
        );
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }

    // Releases the GL texture — must be called before discarding this renderer to avoid memory leaks
    public void dispose() {
        texture.dispose();
        texture = null; // null-out to catch accidental post-dispose usage early
    }
}
