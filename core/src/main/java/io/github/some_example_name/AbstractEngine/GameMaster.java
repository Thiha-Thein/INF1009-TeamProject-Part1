package io.github.some_example_name.AbstractEngine;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;

<<<<<<< Updated upstream
import io.github.some_example_name.AbstractEngine.EntityManagement.EntityManager;
=======
import java.util.List;

import io.github.some_example_name.AbstractEngine.EntityManagement.*;
import io.github.some_example_name.AbstractEngine.CollisionManagement.*;
import io.github.some_example_name.AbstractEngine.MovementManagement.*;
import io.github.some_example_name.AbstractEngine.IOManagement.*;
import io.github.some_example_name.AbstractEngine.AudioManagement.*;
import io.github.some_example_name.AbstractEngine.ScreenManagement.*;
import io.github.some_example_name.AbstractEngine.ScreenManagement.ISimulation;
import io.github.some_example_name.Simulation.*;
>>>>>>> Stashed changes

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

<<<<<<< Updated upstream
    // Unity-style Start method - called once at the beginning
    public void start() {
        if (isInitialized) {
            return;
        }

        System.out.println("GameMaster: Starting game...");

        // Initialize EntityManager
        entityManager = new EntityManager();
        MainObject player = new MainObject();
        entityManager.addEntity(player);

        isInitialized = true;
        entityManager.init();
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
=======
    // ===============================
    // Initialization
    // ===============================

    public void start() {
        if (isInitialized) return;

        System.out.println("GameMaster: Starting game...");

        batch = new SpriteBatch();

        initializeManagers();
        initializeInput();
        initializeAudio();
        initializeScreens();

        isInitialized = true;

        System.out.println("GameMaster: Game started!");
    }

    private void initializeManagers() {
        entityManager = new EntityManager();
        collisionManager = new CollisionManager();
        movementManager = new MovementManager();
        ioManager = new IOManager();
        soundManager = new SoundManager();
        screenManager = new ScreenManager();
    }

    private void initializeScreens() {

        StartScreen startScreen =
            new StartScreen(screenManager, ioManager, batch);

        ISimulation level1World = new SimWorld(
            entityManager,
            movementManager,
            collisionManager,
            soundManager,
            ioManager
        );

        SimulationScreen simulationScreen =
            new SimulationScreen(screenManager, batch, level1World, ioManager);

        screenManager.addScreen("start", startScreen);
        screenManager.addScreen("simulation", simulationScreen);

        screenManager.setScreen("start");
    }

    private void initializeInput() {
        ioManager.bindKey("up",Input.Keys.W);
        ioManager.bindKey("down",Input.Keys.S);
        ioManager.bindKey("left",Input.Keys.A);
        ioManager.bindKey("right",Input.Keys.D);
        ioManager.bindMouse("leftClick", Input.Buttons.LEFT);
        ioManager.bindMouse("rightClick", Input.Buttons.RIGHT);
    }

    private void initializeAudio() {
        soundManager.loadMusic("elbm", "music/elbm.mp3");
        soundManager.playMusic("elbm", true);
    }

    // ===============================
    // Main Update Loop
    // ===============================

    public void update(float deltaTime) {

        List<AbstractEntity> entities = entityManager.getEntities();
        if (!isInitialized) return;
        // Update input system
        ioManager.update();
        movementManager.update(entities, deltaTime);
        collisionManager.checkCollisions(entities);
        entityManager.updateAll(deltaTime);
        // Let ScreenManager handle active screen
        screenManager.update(deltaTime);
        screenManager.render();
    }

    // ===============================
    // Resize
    // ===============================

    @Override
    public void resize(int width, int height) {
        screenManager.resize(width, height);
>>>>>>> Stashed changes
    }

    // ===============================
    // Cleanup
    // ===============================

    @Override
    public void dispose() {

        System.out.println("GameMaster: Cleaning up...");

<<<<<<< Updated upstream
        if (entityManager != null) {
=======
        if (screenManager != null)
            screenManager.dispose();

        if (soundManager != null)
            soundManager.dispose();

        if (entityManager != null)
>>>>>>> Stashed changes
            entityManager.clear();

<<<<<<< Updated upstream
        System.out.println("GameMaster: Cleanup complete!");
    }

    // Getter for EntityManager (so other classes can access it)
    public EntityManager getEntityManager() {
        return entityManager;
    }
=======
        if (batch != null)
            batch.dispose();

        System.out.println("GameMaster: Cleanup complete!");
    }
>>>>>>> Stashed changes
}

