package io.github.some_example_name.AbstractEngine;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.List;

import io.github.some_example_name.AbstractEngine.AudioManagement.*;
import io.github.some_example_name.AbstractEngine.CollisionManagement.*;
import io.github.some_example_name.AbstractEngine.EntityManagement.*;
import io.github.some_example_name.AbstractEngine.IOManagement.*;
import io.github.some_example_name.AbstractEngine.MovementManagement.*;
import io.github.some_example_name.AbstractEngine.ScreenManagement.*;
import io.github.some_example_name.AbstractEngine.ScreenManagement.ISimulation;
import io.github.some_example_name.Simulation.*;

public class GameMaster extends ApplicationAdapter {

    private EntityManager entityManager;
    private CollisionManager collisionManager;
    private MovementManager movementManager;

    private IOManager ioManager;
    private InputSystem inputSystem;

    private SoundManager soundManager;
    private AudioSystem audioSystem; //to be used in the future

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
        inputSystem = new InputSystem(ioManager);

        soundManager = new SoundManager();
        audioSystem = new AudioSystem(soundManager);

        screenManager = new ScreenManager();
    }

    private void initializeScreens() {

        StartScreen startScreen =
            new StartScreen(screenManager, ioManager, batch, soundManager);

        ISimulation level1World = new SimWorld(
            entityManager,
            movementManager,
            collisionManager,
            soundManager,
            ioManager
        );

        SimulationScreen simulationScreen =
            new SimulationScreen(screenManager, batch, level1World, ioManager, soundManager);

        screenManager.addScreen("start", startScreen);
        screenManager.addScreen("simulation", simulationScreen);

        screenManager.setScreen("start");
    }


    private void initializeInput() {
        ioManager.bindKey("w", Input.Keys.W);
        ioManager.bindKey("s", Input.Keys.S);
        ioManager.bindKey("a", Input.Keys.A);
        ioManager.bindKey("d", Input.Keys.D);

        ioManager.bindMouse("leftClick", Input.Buttons.LEFT);
        ioManager.bindMouse("rightClick", Input.Buttons.RIGHT);
    }

    private void initializeAudio() {
        soundManager.loadMusic("menu_bgm", "music/menu.mp3");
        soundManager.loadMusic("game_bgm", "music/game.mp3");

        soundManager.loadSound("ui_click", "sfx/click.mp3");

        //Game music volume control
        soundManager.getMusicTrack("menu_bgm").setVolume(0.6f);  
        soundManager.getMusicTrack("game_bgm").setVolume(0.2f); 

        soundManager.playMusic("menu_bgm", true);
    }

    public void update(float deltaTime) {
        if (!isInitialized) return;

        List<AbstractEntity> entities = entityManager.getEntities();

        ioManager.update();
        inputSystem.update(entities);

        movementManager.update(entities, deltaTime);
        collisionManager.checkCollisions(entities);

        entityManager.updateAll(deltaTime);
        audioSystem.update(entities);

        screenManager.update(deltaTime);
        screenManager.render();
    }

    @Override
    public void resize(int width, int height) {
        screenManager.resize(width, height);
    }

    @Override
    public void dispose() {
        System.out.println("GameMaster: Cleaning up...");

        if (screenManager != null) screenManager.dispose();
        if (soundManager != null) soundManager.dispose();
        if (entityManager != null) entityManager.clear();
        if (batch != null) batch.dispose();

        System.out.println("GameMaster: Cleanup complete!");
    }
}
