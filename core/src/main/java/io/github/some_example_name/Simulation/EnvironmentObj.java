package io.github.some_example_name.Simulation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import io.github.some_example_name.AbstractEngine.EntityManagement.*;
import io.github.some_example_name.AbstractEngine.AudioManagement.*;

public class EnvironmentObj extends AbstractEntity {

    private SoundManager soundManager;
    private Texture texture;

    public EnvironmentObj(SoundManager soundManager) {
        this.soundManager = soundManager;
    }

    @Override
    public void start() {

        setTag("background");
        texture = new Texture("environment/Blue.png");
    }

    @Override
    public void update(float deltaTime) {}

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void render(SpriteBatch batch) {
        int tileWidth = texture.getWidth();
        int tileHeight = texture.getHeight();
        for (int x = 0; x < Gdx.graphics.getWidth(); x += tileWidth) {
            for (int y = 0; y < Gdx.graphics.getHeight(); y += tileHeight) {
                batch.draw(texture, x, y);
            }
        }
    }

    @Override
    public void dispose() {
        if (texture != null) texture.dispose();
    }
}
