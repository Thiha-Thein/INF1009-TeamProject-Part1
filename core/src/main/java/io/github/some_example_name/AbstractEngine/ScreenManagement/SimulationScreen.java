package io.github.some_example_name.AbstractEngine.ScreenManagement;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class SimulationScreen extends AbstractScreen {

    private final SpriteBatch batch;
    private Viewport viewport;
    private Object resultData;

    public SimulationScreen(ScreenManager manager, SpriteBatch batch) {
        super(manager);
        this.batch = batch;
    }

    @Override
    public void show() {
        viewport = new ScreenViewport();
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void update(float deltaTime) {}

    @Override
    public void render() {
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
    }

    @Override
    public void hide() {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {}

    public Object getResultData() { return resultData; }
    public void setResultData(Object resultData) { this.resultData = resultData; }
}

