package io.github.some_example_name.AbstractEngine.EntityManagement;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractEntity implements iEntity {
    // From your UML diagram
    protected int id;
    protected boolean active;
    protected String tag;

    protected Transform transform;
    protected SpriteRenderer spriteRenderer;

    private Map<Class<?>, Object> components = new HashMap<>();

    public <T> void addComponent(Class<T> type, T component) {
        components.put(type, component);
    }

    public <T> T getComponent(Class<T> type) {
        return type.cast(components.get(type));
    }

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

    public int getId() {
        return id;
    }

    public void setId(int i){this.id = i;}


    // Cleanup
    public void dispose() {
        active = false;
        if (spriteRenderer != null) {
            spriteRenderer.dispose();
        }
    }
}
