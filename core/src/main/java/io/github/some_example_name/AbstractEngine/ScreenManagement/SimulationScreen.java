package io.github.some_example_name.AbstractEngine.ScreenManagement;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
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

import io.github.some_example_name.SolarSystemSimulation.*;
import io.github.some_example_name.WordSlayer.*;


public class SimulationScreen extends AbstractScreen {

    private final SpriteBatch batch;
    private Viewport viewport;
    private final Map<String, ISimulation> worlds = new HashMap<>();  // ADD
    private ISimulation currentWorld;                                  // ADD
    private final IOManager ioManager;
    private final SoundManager soundManager;
    private final EntityManager entityManager;
    private final CollisionManager collisionManager;
    private final MovementManager movementManager;
    private final AIManager aiManager;

    private BitmapFont font;
    private GlyphLayout quitLayout;
    private float quitX, quitY;

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

    private void initializeWorlds() {
        worlds.put("solarSystem", new SolarSystemMap(
            entityManager, movementManager, collisionManager, soundManager, ioManager, aiManager
        ));
        worlds.put("wordSlayer", new WordSlayerMap(
            entityManager, movementManager, collisionManager, soundManager, ioManager, aiManager
        ));
    }

    public void loadWorld(String name) {
        if (currentWorld != null) currentWorld.dispose();
        currentWorld = worlds.get(name);
        if (currentWorld != null) {
            currentWorld.initialize();
            System.out.println("Loading world: " + name);
            currentWorld.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
    }

    @Override
    public void show() {
        viewport = new ScreenViewport();

        FreeTypeFontGenerator generator =
            new FreeTypeFontGenerator(Gdx.files.internal("fonts/Molen_Friend_Demo.otf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter =
            new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 100;
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;
        font = generator.generateFont(parameter);
        generator.dispose();

        quitLayout = new GlyphLayout(font, "QUIT");
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void resize(int width, int height) {
        if (viewport != null) viewport.update(width, height, true);
        if (currentWorld != null) currentWorld.resize(width, height);

        if (quitLayout != null) {
            quitX = width - quitLayout.width - 20;
            quitY = 20 + quitLayout.height;
        }
    }

    @Override
    public void update(float deltaTime) {
        if (currentWorld != null) currentWorld.update(deltaTime);

        if (ioManager.wasPressed("leftClick")) {
            Vector2 mouse = viewport.unproject(
                new Vector2(ioManager.getMouseX(), ioManager.getMouseY())
            );
            if (isInside(mouse.x, mouse.y, quitX, quitY, quitLayout)) {
                soundManager.playSound("ui_click");
                soundManager.playMusic("menu_bgm", true);
                if (currentWorld != null) currentWorld.dispose();
                currentWorld = null;
                manager.setScreen("start");
            }
        }
    }

    private boolean isInside(float mx, float my, float x, float y, GlyphLayout layout) {
        return mx >= x && mx <= x + layout.width &&
            my >= y - layout.height && my <= y;
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        if (currentWorld != null) {
            batch.begin();
            batch.draw(currentWorld.getBackground(), 0, 0,
                viewport.getWorldWidth(),
                viewport.getWorldHeight());
            batch.end();

            currentWorld.render(batch);
        }

        batch.begin();
        font.draw(batch, quitLayout, quitX, quitY);
        batch.end();
    }

    @Override
    public void dispose() {
        for (ISimulation world : worlds.values()) world.dispose();
        worlds.clear();
        if (font != null) font.dispose();
    }

    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
}
