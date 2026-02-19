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

import io.github.some_example_name.AbstractEngine.IOManagement.IOManager;
import io.github.some_example_name.AbstractEngine.AudioManagement.SoundManager;

public class SimulationScreen extends AbstractScreen {

    private final SpriteBatch batch;
    private Viewport viewport;
    private ISimulation world;
    private final IOManager ioManager;
    private final SoundManager soundManager;

    // Quit button
    private BitmapFont font;
    private GlyphLayout quitLayout;
    private float quitX, quitY;

    public SimulationScreen(ScreenManager manager,
            				SpriteBatch batch,
            				ISimulation world,
            				IOManager ioManager,
            				SoundManager soundManager) {
    	super(manager);
    	this.batch = batch;
    	this.world = world;
    	this.ioManager = ioManager;
    	this.soundManager = soundManager;
    }

    @Override
    public void show() {
        viewport = new ScreenViewport();
        world.initialize();

        // Setup font for quit button
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
        if (viewport != null) {
            viewport.update(width, height, true);
        }
        world.resize(width, height);

        // Position quit button at bottom right with margins
        quitX = width - quitLayout.width - 20;
        quitY = 20 + quitLayout.height; // Bottom + margin + text height
    }

    @Override
    public void update(float deltaTime) {
        world.update(deltaTime);

        // Check quit button click
        if (ioManager.wasPressed("leftClick")) {
            Vector2 mouse = viewport.unproject(
                new Vector2(ioManager.getMouseX(), ioManager.getMouseY())
            );

            if (isInside(mouse.x, mouse.y, quitX, quitY, quitLayout)) {
                soundManager.playSound("ui_click");
                soundManager.playMusic("menu_bgm", true);
                manager.setScreen("start");
            }
        }
    }

    private boolean isInside(float mx, float my, float x, float y, GlyphLayout layout) {
        return mx >= x &&
            mx <= x + layout.width &&
            my >= y - layout.height &&
            my <= y;
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        world.render(batch);

        // Render quit button on top of everything
        batch.begin();
        font.draw(batch, quitLayout, quitX, quitY);
        batch.end();
    }

    @Override
    public void dispose() {
        world.dispose();
        if (font != null) font.dispose();
    }

    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
}