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

public class SimulationScreen extends AbstractScreen {

    private final SpriteBatch batch;
    private Viewport viewport;

    // all available simulation worlds
    private final Map<String, ISimulation> worlds = new HashMap<>();

    // current active world
    private ISimulation currentWorld;

    // engine systems used by simulation
    private final IOManager ioManager;
    private final SoundManager soundManager;
    private final EntityManager entityManager;
    private final CollisionManager collisionManager;
    private final MovementManager movementManager;
    private final AIManager aiManager;

    // UI system for buttons
    private final UIManager uiManager = new UIManager();
    private final UILayer uiLayer = new UILayer();
    private UIInputSystem uiInputSystem;

    // back button
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

        initializeWorlds();
    }

    // register available simulation worlds
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

    // load a world by name
    public void loadWorld(String name) {

        if (currentWorld != null)
            currentWorld.dispose();

        currentWorld = worlds.get(name);

        if (currentWorld != null) {

            currentWorld.initialize();

            currentWorld.resize(
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight()
            );
        }
    }

    @Override
    public void show() {

        viewport = new ScreenViewport();

        // create font for UI buttons
        FreeTypeFontGenerator generator =
            new FreeTypeFontGenerator(Gdx.files.internal("fonts/star_crush.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter parameter =
            new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = 90;

        font = generator.generateFont(parameter);
        generator.dispose();

        // add UI layer
        uiManager.addLayer(uiLayer);

        // create back button
        quitButton = new UIButton("BACK", font);

        float buttonWidth = 140;
        float buttonHeight = 45;

        quitButton.setSize(buttonWidth, buttonHeight);

        quitButton.setOnClick(() -> {

            soundManager.playSound("ui_click");
            soundManager.playMusic("menu_bgm", true);

            if (currentWorld != null)
                currentWorld.dispose();

            currentWorld = null;

            manager.setScreen("start");
        });

        uiLayer.add(quitButton);

        // UI click detection
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

        // anchor button bottom-left
        quitButton.setPosition(marginX, marginY);
    }

    @Override
    public void update(float deltaTime) {

        // update simulation world
        if (currentWorld != null)
            currentWorld.update(deltaTime);

        // convert mouse to world coordinates
        Vector2 mouse = viewport.unproject(
            new Vector2(ioManager.getMouseX(), ioManager.getMouseY())
        );

        // update UI click detection
        uiInputSystem.update(mouse.x, mouse.y);

        // update UI animations
        uiManager.update(deltaTime);
    }

    @Override
    public void render() {

        // clear screen
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        // render simulation world
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

            // render planets and orbit visuals
            currentWorld.render(batch);
        }

        // render engine UI (buttons etc)
        batch.begin();
        uiManager.render(batch);
        batch.end();
    }

    @Override public void dispose() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
}
