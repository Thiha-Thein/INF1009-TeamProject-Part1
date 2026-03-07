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

// Central game coordinator — owns all subsystems and drives the main loop via LibGDX callbacks
public class GameMaster extends ApplicationAdapter {

    // Core subsystems — each responsible for one domain of game logic
    private EntityManager entityManager;       // tracks and updates all active game entities
    private CollisionManager collisionManager; // detects and resolves collisions between entities
    private MovementManager movementManager;   // applies velocity and physics-based movement
    private IOManager ioManager;               // stores raw input bindings and current input state
    private InputSystem inputSystem;           // translates input state into entity actions
    private SoundManager soundManager;         // loads, plays, and manages audio assets
    private AudioSystem audioSystem;           // higher-level audio logic driven by entity state — reserved for future use
    private ScreenManager screenManager;       // manages screen transitions and delegates render/update calls
    private AIManager aiManager;              // runs AI behaviour logic for non-player entities
    private UISystem uiSystem;                // collects UI components from entities and coordinates rendering

    private SpriteBatch batch; // shared sprite batch passed into screens for 2D rendering

    // Subsystems are constructed here because they have no dependency on the LibGDX graphics context,
    // which is not available until create() is called
    public GameMaster() {
        entityManager = new EntityManager();
        collisionManager = new CollisionManager();
        movementManager = new MovementManager();
        ioManager = new IOManager();
        inputSystem = new InputSystem(ioManager);    // InputSystem reads from IOManager to drive entity behaviour
        soundManager = new SoundManager();
        audioSystem = new AudioSystem(soundManager); // AudioSystem wraps SoundManager with entity-aware logic
        screenManager = new ScreenManager();
        aiManager = new AIManager();
        uiSystem = new UISystem(new UIManager(), new UILayer()); // UIManager and UILayer have no external dependencies so they are created inline
    }

    @Override
    // Called once by LibGDX after the graphics context is ready — safe to load assets and build screens here
    public void create() {

        System.out.println("GameMaster: Starting game...");
        batch = new SpriteBatch(); // SpriteBatch requires an active GL context, so it must be created here rather than in the constructor
        initializeInput();
        initializeAudio();
        initializeScreens();

        System.out.println("GameMaster: Game started!");
    }

    @Override
    // Called every frame by LibGDX — clears the screen then hands off to update()
    public void render() {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1); // dark blue-grey background
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float deltaTime = Gdx.graphics.getDeltaTime(); // time in seconds since last frame, used to keep logic frame-rate independent
        update(deltaTime);
    }

    // Builds the two application screens and registers them with the screen manager before setting the initial screen
    private void initializeScreens() {

        // SimulationScreen needs most subsystems since it runs the live solar simulation
        SimulationScreen simulationScreen = new SimulationScreen(
            screenManager, batch, ioManager, soundManager,
            entityManager, aiManager, movementManager, collisionManager
        );

        // StartScreen only needs basic subsystems; it receives a reference to SimulationScreen so it can trigger the transition
        StartScreen startScreen =
            new StartScreen(screenManager, ioManager, batch, soundManager, simulationScreen);

        screenManager.addScreen("start", startScreen);
        screenManager.addScreen("simulation", simulationScreen);

        screenManager.setScreen("start"); // begin on the start screen
    }

    // Maps logical action names to physical key and mouse button codes
    private void initializeInput() {
        ioManager.bindKey("w", Input.Keys.W);
        ioManager.bindKey("s", Input.Keys.S);
        ioManager.bindKey("a", Input.Keys.A);
        ioManager.bindKey("d", Input.Keys.D);
        ioManager.bindKey("escape", Input.Keys.ESCAPE);

        ioManager.bindMouse("leftClick", Input.Buttons.LEFT);
        ioManager.bindMouse("rightClick", Input.Buttons.RIGHT);
    }

    // Loads all audio assets and sets initial playback volumes before starting menu music
    private void initializeAudio() {
        soundManager.loadMusic("menu_bgm", "music/menu.mp3");
        soundManager.loadMusic("interstellarBGM", "music/solarSimulationMusic.mp3");

        soundManager.loadSound("ui_click", "sfx/click.mp3");

        // Keep background music subtle so it does not overpower gameplay sounds
        soundManager.getMusicTrack("menu_bgm").setVolume(0.05f);
        soundManager.getMusicTrack("interstellarBGM").setVolume(0.2f);

        soundManager.playMusic("menu_bgm", true); // loop menu music until the simulation screen takes over
    }

    // Main update loop — runs all subsystems in dependency order every frame
    public void update(float deltaTime) {

        List<AbstractEntity> entities = entityManager.getEntities();

        ioManager.update();                          // snapshot current input state before any system reads it
        aiManager.update(entities, deltaTime);       // AI decisions are made first so they feed into movement
        inputSystem.update(entities);                // player input applied after AI so both influence the same movement step
        movementManager.update(entities, deltaTime); // moves entities based on velocity accumulated this frame
        collisionManager.checkCollisions(entities);  // resolves positions after movement to prevent overlap
        entityManager.updateAll(deltaTime);          // runs per-entity update logic (animations, timers, etc.)

        uiSystem.register(entities);    // rebuild the UI element list from the current entity set each frame
        audioSystem.update(entities);   // trigger or stop sounds based on entity state changes this frame
        screenManager.update(deltaTime);
        screenManager.render();
    }

    @Override
    // Forwards resize events to the screen manager so the active screen can adjust its viewport
    public void resize(int width, int height) {
        screenManager.resize(width, height);
    }

    @Override
    // Releases all resources in reverse dependency order to avoid dangling references
    public void dispose() {
        System.out.println("GameMaster: Cleaning up...");

        if (screenManager != null) screenManager.dispose(); // screens may hold textures and other GL resources
        if (soundManager != null) soundManager.dispose();   // releases all loaded audio assets
        if (entityManager != null) entityManager.clear();   // drops entity references to allow GC
        if (batch != null) batch.dispose();                 // SpriteBatch holds a native GL resource

        System.out.println("GameMaster: Cleanup complete!");
    }
}
