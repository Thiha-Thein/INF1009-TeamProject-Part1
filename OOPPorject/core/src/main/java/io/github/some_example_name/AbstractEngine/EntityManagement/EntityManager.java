package io.github.some_example_name.AbstractEngine.EntityManagement;

import java.util.ArrayList;
import java.util.List;

public class EntityManager {
    // From your UML diagram
    private List<entityInterface> entities;
    private int nextId;  // To generate unique IDs for entities

    // Constructor
    public EntityManager() {
        entities = new ArrayList<>();
        nextId = 0;
        System.out.println("EntityManager: Initialized");
    }

    // Add an entity to the manager
    public void addEntity(entityInterface entity) {
        entities.add(entity);
        int id = ((Entity) entity).getId();  // cast to Entity to access getId()
        System.out.println("Entity ID: " + id);
    }

    // Remove an entity from the manager
    public void removeEntity(entityInterface entity) {
        ((Entity) entity).dispose();  // Clean up the entity first
        entities.remove(entity);
    }

    // Update all active entities
    public void updateAll(float deltaTime) {
        for (entityInterface e : entities) {
            Entity entity = (Entity) e;
            if (entity.isActive()) {
                e.update(deltaTime);
            }
        }
    }

    // Render all active entities
    public void renderAll() {
        for (entityInterface e : entities) {
            Entity entity = (Entity) e;
            if (entity.isActive()) {
                e.render();
            }
        }
    }

    // Get the list of entities
    public List<entityInterface> getEntities() {
        return entities;
    }

    // Create a new entity with auto-generated ID
    public <T extends Entity & entityInterface> T createEntity(T entity) {
        addEntity(entity);
        nextId++;
        return entity;
    }

    public int generateId() {
        return nextId++;
    }

    // Clear all entities
    public void clear() {
        System.out.println("EntityManager: Clearing all entities...");
        for (entityInterface e : entities) {
            ((Entity) e).dispose();
        }
        entities.clear();
        nextId = 0;
        System.out.println("EntityManager: All entities cleared");
    }
}
