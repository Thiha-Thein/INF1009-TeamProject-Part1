package io.github.some_example_name.AbstractEngine.EntityManagement;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class Entity implements iEntity {
    // From your UML diagram
    private int id;
    private boolean active;
    private String tag;

    protected Transform transform;
    protected SpriteRenderer spriteRenderer;

    // Getters and Setters
    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public void setTransform(Transform transform) {
        this.transform = transform;
    }

    public Transform getTransform() {
        return transform;
    }

    public void render(SpriteBatch batch) {
        if (spriteRenderer != null) {
            spriteRenderer.render(batch, transform);
        }
    }

    public void setSpriteRenderer(SpriteRenderer sr) {
        this.spriteRenderer = sr;
    }

    public SpriteRenderer getSpriteRenderer() {
        return spriteRenderer;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public void setId(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }




    // Cleanup
    public void dispose() {
        active = false;
        if (spriteRenderer != null) {
            spriteRenderer.dispose();
        }
    }
}
