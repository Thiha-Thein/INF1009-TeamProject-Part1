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

import io.github.some_example_name.AbstractEngine.IOManagement.IOManager;

public class StartScreen extends AbstractScreen {

    private final IOManager ioManager;

    private SpriteBatch batch;
    private Texture backgroundTexture;
    private BitmapFont font;
    private GlyphLayout startLayout;
    private GlyphLayout quitLayout;
    private Viewport viewport;

    private float startX, startY;
    private float quitX, quitY;

    public StartScreen(ScreenManager manager, IOManager ioManager, SpriteBatch batch) {
        super(manager);
        this.ioManager = ioManager;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        backgroundTexture = new Texture("environment/Blue.png");
        viewport = new ScreenViewport();

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

        startLayout = new GlyphLayout(font, "START");
        quitLayout = new GlyphLayout(font, "QUIT");

        startX = (width - startLayout.width) / 2f;
        startY = height / 2f + startLayout.height;

        quitX = (width - quitLayout.width) / 2f;
        quitY = height / 2f - quitLayout.height * 2f;
    }

    @Override
    public void update(float deltaTime) {
        if (ioManager.wasPressed("leftClick")) {
            Vector2 mouse = viewport.unproject(
                new Vector2(ioManager.getMouseX(), ioManager.getMouseY())
            );

            if (isInside(mouse.x, mouse.y, startX, startY, startLayout)) {
                manager.setScreen("simulation");
            }
            if (isInside(mouse.x, mouse.y, quitX, quitY, quitLayout)) {
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

        font.draw(batch, startLayout, startX, startY);
        font.draw(batch, quitLayout, quitX, quitY);

        batch.end();
    }

    @Override
    public void hide() { dispose(); }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (font != null) font.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}
}

