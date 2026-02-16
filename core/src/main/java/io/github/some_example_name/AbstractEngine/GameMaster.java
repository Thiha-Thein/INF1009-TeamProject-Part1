package io.github.some_example_name.AbstractEngine;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;

import io.github.some_example_name.AbstractEngine.EntityManagement.EntityManager;
import io.github.some_example_name.AbstractEngine.ScreenManager.ScreenManager;

public class GameMaster extends ApplicationAdapter {

    private EntityManager entityManager;
    private ScreenManager screenManager;
    private boolean isInitialized = false;

    @Override
    public void create() { start(); }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();
        update(dt);
    }

    public void start() {
        if (isInitialized) return;

        entityManager = new EntityManager();
        screenManager = new ScreenManager();

        entityManager.init();

        isInitialized = true;
    }

    public void update(float dt) {
        if (!isInitialized) return;

        screenManager.update(dt);
        screenManager.render();
    }

    @Override
    public void resize(int width, int height) {
        if (screenManager != null) screenManager.resize(width, height);
    }

    @Override
    public void dispose() {
        if (entityManager != null) entityManager.clear();
    }

    public EntityManager getEntityManager() { return entityManager; }
    public ScreenManager getScreenManager() { return screenManager; }
}
