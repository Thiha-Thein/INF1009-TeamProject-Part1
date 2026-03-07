package io.github.some_example_name.AbstractEngine;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.List;

import io.github.some_example_name.AbstractEngine.AIManagement.*;
import io.github.some_example_name.AbstractEngine.AudioManagement.*;
import io.github.some_example_name.AbstractEngine.CollisionManagement.*;
import io.github.some_example_name.AbstractEngine.EntityManagement.*;
import io.github.some_example_name.AbstractEngine.IOManagement.*;
import io.github.some_example_name.AbstractEngine.MovementManagement.*;
import io.github.some_example_name.AbstractEngine.ScreenManagement.*;
import io.github.some_example_name.SolarSystemSimulation.*;
import io.github.some_example_name.AbstractEngine.UIManagement.*;

public class GameMaster extends ApplicationAdapter {

    private EntityManager entityManager;
    private CollisionManager collisionManager;
    private MovementManager movementManager;
    private IOManager ioManager;
    private InputSystem inputSystem;
    private SoundManager soundManager;
    private AudioSystem audioSystem; //to be used in the future
    private ScreenManager screenManager;
    private AIManager aiManager;

    private UISystem uiSystem;

    private SpriteBatch batch;



    // Dependencies constructed here — no graphics context needed
    public GameMaster() {
        entityManager = new EntityManager();
        collisionManager = new CollisionManager();
        movementManager = new MovementManager();
        ioManager = new IOManager();
        inputSystem = new InputSystem(ioManager);
        soundManager = new SoundManager();
        audioSystem = new AudioSystem(soundManager);
        screenManager = new ScreenManager();
        aiManager = new AIManager();
        uiSystem = new UISystem(new UIManager(), new UILayer());
    }

    @Override
    public void create() {

        System.out.println("GameMaster: Starting game...");
        batch = new SpriteBatch();
        initializeInput();
        initializeAudio();
        initializeScreens();

        System.out.println("GameMaster: Game started!");
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float deltaTime = Gdx.graphics.getDeltaTime();
        update(deltaTime);
    }

    private void initializeScreens() {

        SimulationScreen simulationScreen = new SimulationScreen(
            screenManager, batch, ioManager, soundManager,
            entityManager, aiManager, movementManager, collisionManager
        );

        StartScreen startScreen =
            new StartScreen(screenManager, ioManager, batch, soundManager, simulationScreen);

        screenManager.addScreen("start", startScreen);
        screenManager.addScreen("simulation", simulationScreen);

        screenManager.setScreen("start");
    }


    private void initializeInput() {
        ioManager.bindKey("w", Input.Keys.W);
        ioManager.bindKey("s", Input.Keys.S);
        ioManager.bindKey("a", Input.Keys.A);
        ioManager.bindKey("d", Input.Keys.D);
        ioManager.bindKey("escape", Input.Keys.ESCAPE);  // add this

        ioManager.bindMouse("leftClick", Input.Buttons.LEFT);
        ioManager.bindMouse("rightClick", Input.Buttons.RIGHT);
    }
    private void initializeAudio() {
        soundManager.loadMusic("menu_bgm", "music/menu.mp3");
        soundManager.loadMusic("interstellarBGM", "music/solarSimulationMusic.mp3");

        soundManager.loadSound("ui_click", "sfx/click.mp3");

        //Game music volume control
        soundManager.getMusicTrack("menu_bgm").setVolume(0.05f);
        soundManager.getMusicTrack("interstellarBGM").setVolume(0.2f);

        soundManager.playMusic("menu_bgm", true);
    }

    public void update(float deltaTime) {

        List<AbstractEntity> entities = entityManager.getEntities();

        ioManager.update();
        aiManager.update(entities, deltaTime);
        inputSystem.update(entities);
        movementManager.update(entities, deltaTime);
        collisionManager.checkCollisions(entities);
        entityManager.updateAll(deltaTime);

        // Register UI elements from entities
        uiSystem.register(entities);
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
