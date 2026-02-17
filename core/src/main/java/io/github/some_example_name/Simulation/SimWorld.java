package io.github.some_example_name.Simulation;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import io.github.some_example_name.AbstractEngine.EntityManagement.*;
import io.github.some_example_name.AbstractEngine.CollisionManagement.*;
import io.github.some_example_name.AbstractEngine.IOManagement.*;
import io.github.some_example_name.AbstractEngine.MovementManagement.*;
import io.github.some_example_name.AbstractEngine.AudioManagement.*;
import io.github.some_example_name.AbstractEngine.ScreenManagement.ISimulation;

public class SimWorld implements ISimulation {

    private final EntityManager entityManager;
    private final MovementManager movementManager;
    private final CollisionManager collisionManager;
    private final SoundManager soundManager;
    private final IOManager ioManager;

    public SimWorld(EntityManager entityManager,
                    MovementManager movementManager,
                    CollisionManager collisionManager,
                    SoundManager soundManager, IOManager ioManager) {
        this.entityManager = entityManager;
        this.movementManager = movementManager;
        this.collisionManager = collisionManager;
        this.soundManager = soundManager;
        this.ioManager = ioManager;
    }

    @Override
    public void initialize() {
        EnvironmentObj background = new EnvironmentObj(soundManager);
        MainObject player = new MainObject(ioManager);
        ballObj ball = new ballObj();
        simulatedObject ai = new simulatedObject(ball);
        soundManager.setVolume(10);
        soundManager.playMusic("elbm", true);
        entityManager.addEntity(background);
        entityManager.addEntity(player);
        entityManager.addEntity(ball);
        entityManager.addEntity(ai);
        entityManager.start();
    }

    @Override
    public void update(float deltaTime) {

    }

    @Override
    public void render(SpriteBatch batch) {
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
    public void dispose() {
        entityManager.clear();
    }
}

