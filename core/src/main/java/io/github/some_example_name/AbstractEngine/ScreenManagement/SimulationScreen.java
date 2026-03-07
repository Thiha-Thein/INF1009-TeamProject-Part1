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

// Screen that hosts ISimulation worlds — acts as a container that owns the viewport and UI chrome
// while delegating all simulation-specific logic to whichever ISimulation world is currently loaded
public class SimulationScreen extends AbstractScreen {

    private final SpriteBatch batch;
    private Viewport viewport; // created in show() since it needs a GL context

    // Registry of named worlds — add new simulations here to make them loadable by name
    private final Map<String, ISimulation> worlds = new HashMap<>();

    // The currently running simulation — null when no world has been loaded yet
    private ISimulation currentWorld;

    // Engine systems forwarded to simulation worlds that need them
    private final IOManager ioManager;
    private final SoundManager soundManager;
    private final EntityManager entityManager;
    private final CollisionManager collisionManager;
    private final MovementManager movementManager;
    private final AIManager aiManager;

    // UI system for screen-level buttons (e.g. the Back button) that exist outside the simulation world
    private final UIManager uiManager = new UIManager();
    private final UILayer uiLayer = new UILayer();
    private UIInputSystem uiInputSystem;

    private UIButton quitButton;
    private BitmapFont font;

    public SimulationScreen(ScreenManager manager,
                            SpriteBatch batch,
                            IOManager ioManager,
                            SoundManager soundManager,
                            EntityManager entityManager,
                            AIManager aiManager,
                            MovementManager movementManager,
                            CollisionManager collisionManager) {

        super(manager);

        this.batch = batch;
        this.ioManager = ioManager;
        this.soundManager = soundManager;
        this.entityManager = entityManager;
        this.aiManager = aiManager;
        this.movementManager = movementManager;
        this.collisionManager = collisionManager;

        initializeWorlds(); // worlds are registered here so they can be loaded by name later
    }

    // Registers all available simulation worlds — add new ISimulation implementations here
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
    }

    // Switches to a named world, disposing the previous one first to release its resources
    public void loadWorld(String name) {

        if (currentWorld != null)
            currentWorld.dispose();

        currentWorld = worlds.get(name);

        if (currentWorld != null) {

            currentWorld.initialize();

            // Pass the current screen size so the world can position entities correctly from the start
            currentWorld.resize(
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight()
            );
        }
    }

    @Override
    // Called when this screen becomes active — creates the viewport, fonts, and Back button
    public void show() {

        viewport = new ScreenViewport();

        FreeTypeFontGenerator generator =
            new FreeTypeFontGenerator(Gdx.files.internal("fonts/star_crush.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter parameter =
            new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = 90;
        font = generator.generateFont(parameter);
        generator.dispose(); // generator can be disposed immediately after font is created

        uiManager.addLayer(uiLayer);

        quitButton = new UIButton("BACK", font);

        float buttonWidth = 140;
        float buttonHeight = 45;

        quitButton.setSize(buttonWidth, buttonHeight);

        // Back button disposes the current world, restores menu music and returns to the start screen
        quitButton.setOnClick(() -> {

            soundManager.playSound("ui_click");
            soundManager.playMusic("menu_bgm", true);

            if (currentWorld != null)
                currentWorld.dispose();

            currentWorld = null;

            manager.setScreen("start");
        });

        uiLayer.add(quitButton);

        // UIInputSystem converts unprojected mouse coordinates into button click events
        uiInputSystem = new UIInputSystem(ioManager, uiManager);

        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void resize(int width, int height) {

        viewport.update(width, height, true);

        if (currentWorld != null)
            currentWorld.resize(width, height);

        float marginX = 80f;
        float marginY = 80f;

        // Anchor the Back button to the bottom-left regardless of window size
        quitButton.setPosition(marginX, marginY);
    }

    @Override
    public void update(float deltaTime) {

        if (currentWorld != null)
            currentWorld.update(deltaTime);

        // Unproject mouse from screen-space pixels to world-space units before passing to UI hit-testing
        Vector2 mouse = viewport.unproject(
            new Vector2(ioManager.getMouseX(), ioManager.getMouseY())
        );

        uiInputSystem.update(mouse.x, mouse.y);
        uiManager.update(deltaTime);
    }

    @Override
    public void render() {

        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        // Draw background first so it appears behind all simulation visuals
        if (currentWorld != null) {

            batch.begin();
            batch.draw(
                currentWorld.getBackground(),
                0,
                0,
                viewport.getWorldWidth(),
                viewport.getWorldHeight()
            );
            batch.end();

            // Delegate planet and orbit rendering to the active world
            currentWorld.render(batch);
        }

        // Draw screen-level UI (Back button) in a separate pass on top of everything else
        batch.begin();
        uiManager.render(batch);
        batch.end();
    }

    // These lifecycle methods are intentionally empty — this screen has no resources
    // that need special handling on hide/pause/resume beyond what dispose covers
    @Override public void dispose() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
}
