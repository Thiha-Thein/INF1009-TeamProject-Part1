package io.github.some_example_name.AbstractEngine.ScreenManagement;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.HashMap;
import java.util.Map;

import io.github.some_example_name.AbstractEngine.IOManagement.*;
import io.github.some_example_name.AbstractEngine.AudioManagement.*;
import io.github.some_example_name.AbstractEngine.AIManagement.*;
import io.github.some_example_name.AbstractEngine.CollisionManagement.*;
import io.github.some_example_name.AbstractEngine.EntityManagement.*;
import io.github.some_example_name.AbstractEngine.MovementManagement.*;
import io.github.some_example_name.AbstractEngine.UIManagement.*;

import io.github.some_example_name.SolarSystemSimulation.*;
import io.github.some_example_name.SolarSystemSimulation.MiniGames.*;

// the main gameplay screen — holds the solar system and all minigame worlds
// switches between them when the player clicks a planet or finishes a minigame
public class SimulationScreen extends AbstractScreen {

    private final SpriteBatch batch;
    // maps screen coordinates to world coordinates
    private Viewport viewport;

    // stores every available world by name so we can switch between them
    private final Map<String, ISimulation> worlds = new HashMap<>();

    // the world that is currently being updated and drawn
    private ISimulation currentWorld;

    // engine systems needed to build and run the worlds
    private final IOManager ioManager;
    private final SoundManager soundManager;
    private final EntityManager entityManager;
    private final CollisionManager collisionManager;
    private final MovementManager movementManager;
    private final AIManager aiManager;

    // drives sound events on entities inside the minigame worlds
    private final AudioSystem audioSystem;

    // handles button layout and rendering
    private final UIManager uiManager = new UIManager();
    private final UILayer uiLayer = new UILayer();
    // detects mouse clicks on UI buttons
    private UIInputSystem uiInputSystem;

    // the back button shown in the corner during gameplay
    private UIButton quitButton;

    private BitmapFont font;

    // constructor — stores all engine systems and sets up the worlds
    public SimulationScreen(ScreenManager manager,
                            SpriteBatch batch,
                            IOManager ioManager,
                            SoundManager soundManager,
                            EntityManager entityManager,
                            AIManager aiManager,
                            MovementManager movementManager,
                            CollisionManager collisionManager,
                            AudioSystem audioSystem) {

        super(manager);

        this.batch = batch;
        this.ioManager = ioManager;
        this.soundManager = soundManager;
        this.entityManager = entityManager;
        this.aiManager = aiManager;
        this.movementManager = movementManager;
        this.collisionManager = collisionManager;
        this.audioSystem = audioSystem;

        // create all the worlds and store them in the map
        initializeWorlds();
        // give the solar system world the callbacks it needs to launch minigames
        wireGameCallbacks();
    }

    // creates each world and adds it to the worlds map under a string key
    private void initializeWorlds() {

        worlds.put("solarSystem",
            new SolarSystemMap(
                entityManager,
                movementManager,
                collisionManager,
                soundManager,
                ioManager,
                aiManager,
                batch
            )
        );

        // each minigame gets a lambda callback so it can navigate back to the solar system
        // without needing a direct reference to this screen
        worlds.put("factOrFiction",
            new FactOrFictionMap(
                batch,
                ioManager,
                soundManager,
                entityManager,
                audioSystem,
                () -> loadWorld("solarSystem")
            )
        );

        worlds.put("orderPlanets",
            new OrderThePlanetsMap(
                batch,
                ioManager,
                soundManager,
                entityManager,
                audioSystem,
                () -> loadWorld("solarSystem")
            )
        );

        worlds.put("matchPlanet",
            new MatchThePlanetMap(
                batch,
                ioManager,
                soundManager,
                entityManager,
                audioSystem,
                () -> loadWorld("solarSystem")
            )
        );
    }

    // tells the solar system which minigame to open when the player clicks each planet
    private void wireGameCallbacks() {

        ISimulation solarWorld = worlds.get("solarSystem");
        if (solarWorld == null) return;

        // map each planet name to the world that should open when it is clicked
        Map<String, Runnable> callbacks = new HashMap<>();
        callbacks.put("Earth", () -> loadWorld("factOrFiction"));
        callbacks.put("Jupiter", () -> loadWorld("orderPlanets"));
        callbacks.put("Saturn", () -> loadWorld("matchPlanet"));

        // setGameCallbacks is defined on ISimulation — only SolarSystemMap does anything with it
        solarWorld.setGameCallbacks(callbacks);
    }

    // disposes the current world and switches to a new one by name
    public void loadWorld(String name) {

        // clean up the old world before switching
        if (currentWorld != null)
            currentWorld.dispose();

        currentWorld = worlds.get(name);

        if (currentWorld != null) {
            // set up the new world and give it the current screen size
            currentWorld.initialize();

            currentWorld.resize(
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight()
            );
        }
    }

    // called when this screen becomes the active screen — sets up the viewport, font, and back button
    @Override
    public void show() {

        viewport = new ScreenViewport();

        // generate the font used on the back button from a ttf file
        FreeTypeFontGenerator generator =
            new FreeTypeFontGenerator(Gdx.files.internal("fonts/star_crush.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter parameter =
            new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = 90;

        font = generator.generateFont(parameter);
        // dispose the generator after the font is built to free memory
        generator.dispose();

        // register the UI layer so buttons get drawn
        uiManager.addLayer(uiLayer);

        // create the back button with a label and the font we just generated
        quitButton = new UIButton("BACK", font);

        float buttonWidth = 140;
        float buttonHeight = 45;

        quitButton.setSize(buttonWidth, buttonHeight);

        // what happens when the player clicks the back button
        quitButton.setOnClick(() -> {
            // play a click sound and restart the menu music
            soundManager.playSound("ui_click");
            soundManager.playMusic("menu_bgm", true);

            // clean up whatever world is currently running
            if (currentWorld != null)
                currentWorld.dispose();

            currentWorld = null;

            // go back to the start screen
            manager.setScreen("start");
        });

        uiLayer.add(quitButton);

        // set up the system that detects mouse clicks on buttons
        uiInputSystem = new UIInputSystem(ioManager, uiManager);

        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    // called whenever the window is resized — updates the viewport and repositions the button
    @Override
    public void resize(int width, int height) {

        // true centers the camera after resizing
        viewport.update(width, height, true);

        if (currentWorld != null)
            currentWorld.resize(width, height);

        float marginX = 80f;
        float marginY = 80f;

        // pin the back button to the bottom-left corner
        quitButton.setPosition(marginX, marginY);
    }

    // updates the current world and checks for button clicks each frame
    @Override
    public void update(float deltaTime) {

        if (currentWorld != null)
            currentWorld.update(deltaTime);

        // convert the raw mouse pixel position into world coordinates
        Vector2 mouse = viewport.unproject(
            new Vector2(ioManager.getMouseX(), ioManager.getMouseY())
        );

        // check if the mouse is hovering over or clicking any buttons
        uiInputSystem.update(mouse.x, mouse.y);

        // run any button animations (hover effects, etc.)
        uiManager.update(deltaTime);
    }

    // draws the background, the current world, and the UI buttons each frame
    @Override
    public void render() {

        // clear to black before drawing
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        if (currentWorld != null) {
            batch.begin();
            // stretch the world's background image to fill the whole screen
            batch.draw(currentWorld.getBackground(), 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
            batch.end();

            // draw planets, orbits, and any world-specific visuals
            currentWorld.render(batch);
        }

        // draw UI elements like the back button on top of everything else
        batch.begin();
        uiManager.render(batch);
        batch.end();
    }

    @Override public void dispose() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
}
