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

// the main entry point for the game — LibGDX calls create(), render(), resize(), and dispose() on this class
public class GameMaster extends ApplicationAdapter {

    // manages all game entities (planets, players, etc.)
    private EntityManager entityManager;
    // checks when two entities overlap
    private CollisionManager collisionManager;
    // moves entities each frame based on their velocity
    private MovementManager movementManager;
    // stores key and mouse bindings
    private IOManager ioManager;
    // reads player input and maps it to entity actions
    private InputSystem inputSystem;
    // loads and plays sound effects and music
    private SoundManager soundManager;
    // drives audio components attached to entities — reserved for future use
    private AudioSystem audioSystem;
    // switches between different screens (start screen, simulation, etc.)
    private ScreenManager screenManager;
    // controls AI-driven entities
    private AIManager aiManager;

    // manages and renders UI elements like buttons
    private UISystem uiSystem;

    // used to draw textures and sprites to the screen
    private SpriteBatch batch;

    // constructor — creates all the engine systems before the graphics context is ready
    public GameMaster() {
        entityManager = new EntityManager();
        collisionManager = new CollisionManager();
        movementManager = new MovementManager();
        ioManager = new IOManager();
        // inputSystem needs ioManager so it knows which keys are pressed
        inputSystem = new InputSystem(ioManager);
        soundManager = new SoundManager();
        // audioSystem wraps soundManager for entity-driven audio events
        audioSystem = new AudioSystem(soundManager);
        screenManager = new ScreenManager();
        aiManager = new AIManager();
        uiSystem = new UISystem(new UIManager(), new UILayer());
    }

    // called once by LibGDX when the game window is ready
    @Override
    public void create() {

        System.out.println("GameMaster: Starting game...");
        batch = new SpriteBatch();
        initializeInput();
        initializeAudio();
        initializeScreens();

        System.out.println("GameMaster: Game started!");
    }

    // called every frame by LibGDX — clears the screen then runs the game update
    @Override
    public void render() {
        // fill the screen with a dark background colour before drawing anything
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float deltaTime = Gdx.graphics.getDeltaTime();
        update(deltaTime);
    }

    // creates the start screen and simulation screen and registers them with the screen manager
    private void initializeScreens() {

        // audioSystem is passed to SimulationScreen so minigame worlds can use it
        SimulationScreen simulationScreen = new SimulationScreen(
            screenManager, batch, ioManager, soundManager,
            entityManager, aiManager, movementManager, collisionManager, audioSystem
        );

        StartScreen startScreen =
            new StartScreen(screenManager, ioManager, batch, soundManager, simulationScreen);

        // register both screens so the manager can switch between them by name
        screenManager.addScreen("start", startScreen);
        screenManager.addScreen("simulation", simulationScreen);

        // start on the start screen
        screenManager.setScreen("start");
    }

    // binds keyboard keys and mouse buttons to named actions used throughout the game
    private void initializeInput() {
        ioManager.bindKey("w", Input.Keys.W);
        ioManager.bindKey("s", Input.Keys.S);
        ioManager.bindKey("a", Input.Keys.A);
        ioManager.bindKey("d", Input.Keys.D);
        // escape is used to close panels and exit minigames
        ioManager.bindKey("escape", Input.Keys.ESCAPE);

        ioManager.bindMouse("leftClick", Input.Buttons.LEFT);
        ioManager.bindMouse("rightClick", Input.Buttons.RIGHT);
        ioManager.bindKey("space", Input.Keys.SPACE);
    }

    // loads all music and sound effect files and sets their starting volumes
    private void initializeAudio() {
        soundManager.loadMusic("menu_bgm", "music/menu.mp3");
        soundManager.loadMusic("interstellarBGM", "music/solarSimulationMusic.mp3");

        soundManager.loadSound("ui_click", "sfx/click.mp3");

        // keep the menu music quiet so it does not overpower the UI
        soundManager.getMusicTrack("menu_bgm").setVolume(0.05f);
        soundManager.getMusicTrack("interstellarBGM").setVolume(0.2f);

        // start playing the menu music on loop straight away
        soundManager.playMusic("menu_bgm", true);
    }

    // runs all game systems in order every frame
    public void update(float deltaTime) {

        List<AbstractEntity> entities = entityManager.getEntities();

        // process keyboard and mouse state for this frame
        ioManager.update();
        // move AI-controlled entities
        aiManager.update(entities, deltaTime);
        // apply player input to entities
        inputSystem.update(entities);
        // move all entities based on their velocity
        movementManager.update(entities, deltaTime);
        // detect and resolve overlapping entities
        collisionManager.checkCollisions(entities);
        // run each entity's own update logic
        entityManager.updateAll(deltaTime);

        // collect UI elements from entities and register them for rendering
        uiSystem.register(entities);
        // trigger any sound events attached to entities
        audioSystem.update(entities);
        // update then draw the current screen
        screenManager.update(deltaTime);
        screenManager.render();
    }

    // called by LibGDX whenever the window is resized — passes new size to the screen manager
    @Override
    public void resize(int width, int height) {
        screenManager.resize(width, height);
    }

    // called when the game closes — frees memory used by screens, sounds, entities, and the batch
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
