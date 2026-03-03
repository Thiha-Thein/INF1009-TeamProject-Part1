package io.github.some_example_name.SolarSystemSimulation;
import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import io.github.some_example_name.AbstractEngine.EntityManagement.*;
import io.github.some_example_name.AbstractEngine.CollisionManagement.*;
import io.github.some_example_name.AbstractEngine.IOManagement.*;
import io.github.some_example_name.AbstractEngine.MovementManagement.*;
import io.github.some_example_name.AbstractEngine.AudioManagement.*;
import io.github.some_example_name.AbstractEngine.ScreenManagement.*;
import io.github.some_example_name.AbstractEngine.AIManagement.*;

public class SolarSystemMap implements ISimulation {

    private final EntityManager entityManager;
    private final MovementManager movementManager;
    private final CollisionManager collisionManager;
    private final SoundManager soundManager;
    private final IOManager ioManager;
    private final AIManager aiManager;
    private Texture background;
    private ShapeRenderer shapeRenderer;
    private planetFactory planetFactory;
    private float cx, cy;

    public SolarSystemMap(EntityManager entityManager,
                    MovementManager movementManager,
                    CollisionManager collisionManager,
                    SoundManager soundManager,
                    IOManager ioManager,
                    AIManager aiManager) {
        this.entityManager = entityManager;
        this.movementManager = movementManager;
        this.collisionManager = collisionManager;
        this.soundManager = soundManager;
        this.ioManager = ioManager;
        this.aiManager = aiManager;
        shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void initialize() {
        entityManager.clear();  // ADD THIS
        // Recreate shapeRenderer if disposed
        if (shapeRenderer == null) {
            shapeRenderer = new ShapeRenderer();
        }

        background = new Texture("planets/spaceBackground.png");

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        cx = (screenWidth - 600f) / 2f + 300f;
        cy = (screenHeight - 600f) / 2f + 300f;

        PlanetObj sun = new PlanetObj("Sun", 1000f, 600f, "planets/sun.png", null);
        sun.setInitialPosition((screenWidth - 600f) / 2f, (screenHeight - 600f) / 2f);

        PlanetObj lava   = planetFactory.create("Lava",      8f,  60f,  "planets/lavaworld.png",      sun, 0, 0f);
        PlanetObj dry    = planetFactory.create("Dry",       12f, 70f,  "planets/drywasteland.png",   sun, 1, 45f);
        PlanetObj earth  = planetFactory.create("Earth",     15f, 80f,  "planets/earth.png",          sun, 2, 90f);
        PlanetObj elike  = planetFactory.create("EarthLike", 13f, 75f,  "planets/earthlookalike.png", sun, 3, 135f);
        PlanetObj crater = planetFactory.create("Crater",    20f, 85f,  "planets/craterplanet.png",   sun, 4, 180f);
        PlanetObj gas    = planetFactory.create("Gas",       50f, 200f, "planets/gasstar.png",        sun, 5, 225f);
        PlanetObj saturn = planetFactory.create("Saturn",    45f, 250f, "planets/saturn.png",         sun, 6, 270f);
        PlanetObj ice    = planetFactory.create("Ice",       10f, 70f,  "planets/iceworld.png",       sun, 7, 315f);

        // set initial positions to sun center
        lava.setInitialPosition(cx, cy);
        dry.setInitialPosition(cx, cy);
        earth.setInitialPosition(cx, cy);
        elike.setInitialPosition(cx, cy);
        crater.setInitialPosition(cx, cy);
        gas.setInitialPosition(cx, cy);
        saturn.setInitialPosition(cx, cy);
        ice.setInitialPosition(cx, cy);

        entityManager.addEntity(sun);
        entityManager.addEntity(ice);      // furthest first
        entityManager.addEntity(saturn);
        entityManager.addEntity(gas);
        entityManager.addEntity(crater);
        entityManager.addEntity(elike);
        entityManager.addEntity(earth);
        entityManager.addEntity(dry);
        entityManager.addEntity(lava);     // closest last
        entityManager.start();
    }

    @Override
    public void update(float deltaTime) {

    }

    @Override
    public void render(SpriteBatch batch) {

        //draw orbit lines
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.7f, 0.8f, 1f, 0.25f); // soft space blue

        for (AbstractEntity entity : entityManager.getEntities()) {
            if (entity instanceof PlanetObj) {
                ((PlanetObj) entity).drawOrbit(shapeRenderer);
            }
        }

        shapeRenderer.end();

        //draw
        batch.begin();
        entityManager.renderAll(batch);
        batch.end();
    }

    public void resize(int width, int height) {

        for (AbstractEntity entity : entityManager.getEntities()) {
            entity.resize(width, height);
        }
        collisionManager.setWorldBounds(width, height);
    }

    @Override
    public Texture getBackground() {
        return background;
    }

    @Override
    public void dispose() {
        entityManager.clear();
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
            shapeRenderer = null;  // prevent double dispose
        }
        if (background != null) {
            background.dispose();
            background = null;
        }
    }
}
