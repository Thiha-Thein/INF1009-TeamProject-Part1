package io.github.some_example_name.AbstractEngine.EntityManagement;

public class Entity {
    // From your UML diagram
    private int id;
    private boolean active;
    private String tag;
    protected Transform transform;     // every entity has this
    // Constructor
    public Entity(int id) {
        this.id = id;
        this.active = true;  // Entities start active
        this.tag = "";
    }

    // Getters and Setters
    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public Transform getTransform() {
        return transform;
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

    // Cleanup
    public void dispose() {
        // Clean up resources when entity is destroyed
    }
}
