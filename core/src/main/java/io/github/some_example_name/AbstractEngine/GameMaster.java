package io.github.some_example_name.AbstractEngine;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.List;

import io.github.some_example_name.AbstractEngine.EntityManagement.*;
import io.github.some_example_name.AbstractEngine.CollisionManagement.*;
import io.github.some_example_name.AbstractEngine.MovementManagement.*;
import io.github.some_example_name.AbstractEngine.IOManagement.*;
import io.github.some_example_name.AbstractEngine.AudioManagement.*;
import io.github.some_example_name.AbstractEngine.ScreenManagement.*;
import io.github.some_example_name.Simulation.*;

public class GameMaster extends ApplicationAdapter {

    private EntityManager entityManager;
    private CollisionManager collisionManager;
    private MovementManager movementManager;
    private IOManager ioManager;
    private SoundManager soundManager;
    private ScreenManager screenManager;
    private SpriteBatch batch;

    private boolean isInitialized = false;

    @Override
    public void create() {
        start();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float deltaTime = Gdx.graphics.getDeltaTime();

        update(deltaTime);
    }

    // ---- Initialization ----

    public void start() {
        if (isInitialized) return;
        batch = new SpriteBatch();
        System.out.println("GameMaster: Starting game...");

        initializeManagers();
        initializeScreens();
        initializeWorld();
        initializeInput();
        initializeAudio();

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

    private void initializeAudio() {
        soundManager.loadMusic("bgm", "music/bgm.mp3");
        soundManager.playMusic("bgm", true);
    }

    private void initializeScreens() {

        StartScreen startScreen = new StartScreen(screenManager, ioManager, batch);
        SimulationScreen simulationScreen = new SimulationScreen(screenManager, batch);

        screenManager.addScreen("start", startScreen);
        screenManager.addScreen("simulation", simulationScreen);

        screenManager.setScreen("start");
    }


    private void initializeWorld() {
        MainObject player = new MainObject();
        simulatedObject enemy = new simulatedObject();
        EnvironmentObj background = new EnvironmentObj(soundManager);

        entityManager.addEntity(player);
        entityManager.addEntity(background);
        entityManager.addEntity(enemy);
        entityManager.start();
    }

    private void initializeInput() {
        /*ioManager.bindKey("move_left", Input.Keys.A);
        ioManager.bindKey("move_right", Input.Keys.D);
        ioManager.bindKey("move_up", Input.Keys.W);
        ioManager.bindKey("move_down", Input.Keys.S);*/
        ioManager.bindMouse("leftClick", Input.Buttons.LEFT);
        ioManager.bindMouse("rightClick", Input.Buttons.RIGHT);
    }

    // ---- Update Loop ----

    public void update(float deltaTime) {
        if (!isInitialized) return;

        ioManager.update();

        //Screen logic
        screenManager.update(deltaTime);
        screenManager.render();

        AbstractScreen current = screenManager.getCurrentScreen();

        //Only run gameplay systems if SimulationScreen is active
        if (current instanceof SimulationScreen) {

            List<AbstractEntity> entities = entityManager.getEntities();

            movementManager.update(entities, deltaTime);
            collisionManager.checkCollisions(entities);
            entityManager.updateAll(deltaTime);

            batch.begin();
            entityManager.renderAll(batch);
            batch.end();
        }
    }

    @Override
    public void resize(int width, int height) {
        screenManager.resize(width, height);
    }

    @Override
    public void dispose() {
        System.out.println("GameMaster: Cleaning up...");

        if (screenManager != null) {
            screenManager.dispose();
        }

        if (soundManager != null) {
            soundManager.dispose();
        }

        if (entityManager != null) {
            entityManager.clear();
        }

        if (batch != null) {
            batch.dispose();
        }

        System.out.println("GameMaster: Cleanup complete!");
    }

}


