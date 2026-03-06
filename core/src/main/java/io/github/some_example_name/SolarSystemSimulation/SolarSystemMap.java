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

// inner rocky planets
        PlanetObj mercury = planetFactory.create("Mercury", 18f, 50f,  "planets/mercury.png",  sun, 0, 0f);
        PlanetObj venus   = planetFactory.create("Venus",   27f, 65f,  "planets/venus.png",    sun, 1, 45f);
        PlanetObj earth   = planetFactory.create("Earth",   30f, 70f,  "planets/earth.png",    sun, 2, 90f);
        PlanetObj mars    = planetFactory.create("Mars",    24f, 55f,  "planets/mars.png",     sun, 3, 135f);

// gas giants
        PlanetObj jupiter = planetFactory.create("Jupiter", 90f, 170f, "planets/jupiter.png",  sun, 4, 180f);
        PlanetObj saturn  = planetFactory.create("Saturn",  78f, 140f, "planets/saturn.png",   sun, 5, 225f);

// ice giants
        PlanetObj uranus  = planetFactory.create("Uranus",  36f, 105f, "planets/uranus.png",   sun, 6, 270f);
        PlanetObj neptune = planetFactory.create("Neptune", 33f, 100f, "planets/neptune.png",  sun, 7, 315f);


        // set initial positions to sun center
        mercury.setInitialPosition(cx, cy);
        venus.setInitialPosition(cx, cy);
        earth.setInitialPosition(cx, cy);
        mars.setInitialPosition(cx, cy);
        jupiter.setInitialPosition(cx, cy);
        saturn.setInitialPosition(cx, cy);
        uranus.setInitialPosition(cx, cy);
        neptune.setInitialPosition(cx, cy);

        entityManager.addEntity(sun);

        entityManager.addEntity(neptune);   // furthest first
        entityManager.addEntity(uranus);
        entityManager.addEntity(saturn);
        entityManager.addEntity(jupiter);
        entityManager.addEntity(mars);
        entityManager.addEntity(earth);
        entityManager.addEntity(venus);
        entityManager.addEntity(mercury);   // closest last

        entityManager.start();
        saturn.getAnimationRenderer().setScale(2.5f);
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
