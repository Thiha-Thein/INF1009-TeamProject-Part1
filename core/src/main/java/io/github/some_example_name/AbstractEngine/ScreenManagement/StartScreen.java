package io.github.some_example_name.AbstractEngine.ScreenManagement;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.some_example_name.AbstractEngine.IOManagement.*;
import io.github.some_example_name.AbstractEngine.AudioManagement.*;

public class StartScreen extends AbstractScreen {

    private final IOManager ioManager;
    private final SoundManager soundManager;

    private SpriteBatch batch;
    private Texture backgroundTexture;
    private BitmapFont font;
    private GlyphLayout solarLayout, wordSlayerLayout,quitLayout;
    private Viewport viewport;

    private float solarX, solarY, quitX, quitY, wordX, wordY;
    private final SimulationScreen simulationScreen;

    public StartScreen(ScreenManager manager, IOManager ioManager, SpriteBatch batch, SoundManager soundManager, SimulationScreen simulationScreen) {
        super(manager);
        this.ioManager = ioManager;
        this.batch = batch;
        this.soundManager = soundManager;
        this.simulationScreen = simulationScreen;
    }

    @Override
    public void show() {
        backgroundTexture = new Texture("environment/Blue.png");
        viewport = new ScreenViewport();
        soundManager.playMusic("menu_bgm", true);

        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);

        if (font != null) font.dispose();

        FreeTypeFontGenerator generator =
            new FreeTypeFontGenerator(Gdx.files.internal("fonts/Molen_Friend_Demo.otf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter =
            new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = (int) (height / 10f);
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;
        font = generator.generateFont(parameter);
        generator.dispose();

        solarLayout = new GlyphLayout(font, "SIMULATION");
        wordSlayerLayout = new GlyphLayout(font, "GAME");
        quitLayout = new GlyphLayout(font, "QUIT");

        float centerX = width / 2f;
        float centerY = height / 2f;

        float spacing = 200f; // vertical spacing between buttons

        solarX = centerX - solarLayout.width / 2f;
        solarY = centerY + spacing;

        wordX = centerX - wordSlayerLayout.width / 2f;
        wordY = centerY;

        quitX = centerX - quitLayout.width / 2f;
        quitY = centerY - spacing;
    }

    @Override
    public void update(float deltaTime) {
        if (ioManager.wasPressed("leftClick")) {
            Vector2 mouse = viewport.unproject(
                new Vector2(ioManager.getMouseX(), ioManager.getMouseY())
            );

            if (isInside(mouse.x, mouse.y, solarX, solarY, solarLayout)) {
                soundManager.playSound("ui_click");
                simulationScreen.loadWorld("solarSystem");
                manager.setScreen("simulation");
            }

            if (isInside(mouse.x, mouse.y, wordX, wordY, wordSlayerLayout)) {
                soundManager.playSound("ui_click");
                simulationScreen.loadWorld("wordSlayer");
                manager.setScreen("simulation");
            }

            if (isInside(mouse.x, mouse.y, quitX, quitY, quitLayout)) {
                soundManager.playSound("ui_click");
                Gdx.app.exit();
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
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();

        int tileWidth = backgroundTexture.getWidth();
        int tileHeight = backgroundTexture.getHeight();
        for (int x = 0; x < viewport.getScreenWidth(); x += tileWidth) {
            for (int y = 0; y < viewport.getScreenHeight(); y += tileHeight) {
                batch.draw(backgroundTexture, x, y);
            }
        }

        font.draw(batch, solarLayout, solarX, solarY);
        font.draw(batch, wordSlayerLayout, wordX, wordY);
        font.draw(batch, quitLayout, quitX, quitY);

        batch.end();
    }

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        if (font != null) font.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}
}
