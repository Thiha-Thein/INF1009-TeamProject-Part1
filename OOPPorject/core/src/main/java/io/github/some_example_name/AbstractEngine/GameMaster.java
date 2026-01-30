package io.github.some_example_name.AbstractEngine;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;

import io.github.some_example_name.AbstractEngine.EntityManagement.EntityManager;

public class GameMaster extends ApplicationAdapter {

    // Add EntityManager
    private EntityManager entityManager;
    private boolean isInitialized = false;

    @Override
    public void create() {
        start();
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();
        update(deltaTime);
    }

    // Unity-style Start method - called once at the beginning
    public void start() {
        if (isInitialized) {
            return;
        }

        System.out.println("GameMaster: Starting game...");

        // Initialize EntityManager
        entityManager = new EntityManager();

        isInitialized = true;
        System.out.println("GameMaster: Game started!");
    }

    // Unity-style Update method - called every frame
    public void update(float deltaTime) {
        if (!isInitialized) {
            return;
        }

        // Update all entities
        entityManager.updateAll(deltaTime);

        // Render all entities
        entityManager.renderAll();
    }

    @Override
    public void dispose() {
        System.out.println("GameMaster: Cleaning up...");

        if (entityManager != null) {
            entityManager.clear();
        }

        System.out.println("GameMaster: Cleanup complete!");
    }

    // Getter for EntityManager (so other classes can access it)
    public EntityManager getEntityManager() {
        return entityManager;
    }
}

