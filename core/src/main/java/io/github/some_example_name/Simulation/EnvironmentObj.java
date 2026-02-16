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
        this.soundManager = soundManager;  // Inject dependency
    }

    @Override
    public void start() {
        soundManager.playMusic("bgm", true);
        setTag("background");
        texture = new Texture("environment/Blue.png");
        updateSize();
    }

    @Override
    public void update(float deltaTime) {
        updateSize();
    }

    private void updateSize() {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        transform = new Transform(0, 0, width, height);
    }

    @Override
    public void render(SpriteBatch batch) {
        int tileWidth = texture.getWidth();
        int tileHeight = texture.getHeight();

        for (int x = 0; x < transform.getWidth(); x += tileWidth) {
            for (int y = 0; y < transform.getHeight(); y += tileHeight) {
                batch.draw(texture, x, y);
            }
        }
    }

    @Override
    public void dispose() {
        if (texture != null) texture.dispose();
    }
}
