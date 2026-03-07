package io.github.some_example_name.AbstractEngine.EntityManagement;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.HashMap;
import java.util.Map;

// Base class for everything that exists in the game world — subclass this to create players, planets, cursors, etc.
// Uses a component map so systems can attach and retrieve arbitrary data without modifying the entity class hierarchy
public abstract class AbstractEntity implements iEntity {

    protected int id;
    protected boolean active, pendingRemoval = false;
    protected String tag;          // string identifier used by systems to find specific entities (e.g. "mouse")
    protected Transform transform; // world position and size — most systems read this directly
    protected SpriteRenderer spriteRenderer;
    protected AnimationRenderer animationRenderer;

    // Generic component store — keyed by class so components are retrieved by type rather than by name
    private Map<Class<?>, Object> components = new HashMap<>();

    // Registers a component against its type — replaces any previously stored component of the same type
    public <T> void addComponent(Class<T> type, T component) {
        components.put(type, component);
    }

    // Returns the component of the requested type, or null if none has been added
    public <T> T getComponent(Class<T> type) {
        return type.cast(components.get(type));
    }

    public void setActive(boolean active) { this.active = active; }
    public boolean isActive()             { return active; }

    public void setTransform(Transform transform) { this.transform = transform; }
    public Transform getTransform()               { return transform; }

    // Renders the entity — AnimationRenderer takes priority over SpriteRenderer when both are present
    public void render(SpriteBatch batch) {
        if (animationRenderer != null) {
            animationRenderer.render(batch, transform);
        } else if (spriteRenderer != null) {
            spriteRenderer.render(batch, transform);
        }
    }

    public void setSpriteRenderer(SpriteRenderer sr)   { this.spriteRenderer = sr; }
    public SpriteRenderer getSpriteRenderer()           { return spriteRenderer; }

    public void setAnimationRenderer(AnimationRenderer ar) { this.animationRenderer = ar; }
    public AnimationRenderer getAnimationRenderer()        { return animationRenderer; }

    // Tags are used by collision callbacks and search queries to identify the entity without instanceof checks
    public void setTag(String tag)  { this.tag = tag; }
    public String getTag()          { return tag; }

    public int getId()              { return id; }
    public void setId(int i)        { this.id = i; }

    // Marks the entity for deferred removal — EntityManager removes it after the current update loop finishes
    // to avoid ConcurrentModificationException when iterating the entity list
    public void markForRemoval()    { pendingRemoval = true; }
    public boolean isPendingRemoval() { return pendingRemoval; }

    // Releases GL resources held by renderers — called by EntityManager before the entity is discarded
    public void dispose() {
        active = false;
        if (animationRenderer != null) animationRenderer.dispose();
        if (spriteRenderer    != null) spriteRenderer.dispose();
    }
}
