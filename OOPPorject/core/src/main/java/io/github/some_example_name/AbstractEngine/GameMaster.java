package io.github.some_example_name.AbstractEngine;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;

import io.github.some_example_name.AbstractEngine.EntityManagement.EntityManager;

public class GameMaster extends ApplicationAdapter {

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
        EntityManager entityManager = EntityManager.getInstance(); // 👈 singleton
        MainObject player = new MainObject();
        simulatedObject enemy = new simulatedObject();
        entityManager.addEntity(player);
        entityManager.addEntity(enemy);
        entityManager.start();

        isInitialized = true;
        System.out.println("GameMaster: Game started!");
    }

    // Unity-style Update method - called every frame
    public void update(float deltaTime) {
        if (!isInitialized) {
            return;
        }

        // Update all entities
        EntityManager.getInstance().updateAll(deltaTime);

        // Render all entities
        EntityManager.getInstance().renderAll();
    }

    @Override
    public void dispose() {
        System.out.println("GameMaster: Cleaning up...");

        if (EntityManager.getInstance() != null) {
            EntityManager.getInstance().clear();
        }

        System.out.println("GameMaster: Cleanup complete!");
    }
}

