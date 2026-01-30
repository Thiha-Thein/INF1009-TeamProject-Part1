package io.github.some_example_name.AbstractEngine.EntityManagement;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class SpriteRenderer {

    private Texture texture;
    private boolean visible = true;

    public SpriteRenderer(String texturePath) {
        this.texture = new Texture(texturePath);
    }

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

    public void dispose() {
        texture.dispose();
        texture = null;
    }
}
