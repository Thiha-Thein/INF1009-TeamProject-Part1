package io.github.some_example_name.AbstractEngine.ScreenManagement;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.some_example_name.AbstractEngine.IOManagement.*;
import io.github.some_example_name.AbstractEngine.AudioManagement.*;
import io.github.some_example_name.AbstractEngine.UIManagement.*;

// Main menu screen — displays Simulation and Quit buttons against a space background
// Receives a reference to SimulationScreen so it can call loadWorld() before switching to it
public class StartScreen extends AbstractScreen {

    private final IOManager ioManager;
    private final SoundManager soundManager;
    private final SimulationScreen simulationScreen; // held so the button callback can trigger world loading

    private SpriteBatch batch;
    private Texture backgroundTexture;
    private Viewport viewport;

    private final UIManager uiManager = new UIManager();
    private final UILayer uiLayer = new UILayer();
    private UIInputSystem uiInputSystem;

    private BitmapFont font;

    public StartScreen(ScreenManager manager,
                       IOManager ioManager,
                       SpriteBatch batch,
                       SoundManager soundManager,
                       SimulationScreen simulationScreen) {

        super(manager);

        this.ioManager = ioManager;
        this.batch = batch;
        this.soundManager = soundManager;
        this.simulationScreen = simulationScreen;
    }

    @Override
    // Called when this screen becomes active — safe to load assets here because GL context is guaranteed
    public void show() {

        viewport = new ScreenViewport();
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        backgroundTexture = new Texture("planets/spaceBackground.png");

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/star_crush.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = 140;
        font = generator.generateFont(parameter);
        generator.dispose();

        uiManager.addLayer(uiLayer);

        float centerX = Gdx.graphics.getWidth() / 2f;
        float centerY = Gdx.graphics.getHeight() / 2f;
        float buttonWidth = 450;
        float buttonHeight = 100;

        // Simulation button — loads the solar system world then switches to SimulationScreen
        UIButton simulationButton = new UIButton("SIMULATION", font);
        simulationButton.setSize(buttonWidth, buttonHeight);
        simulationButton.setPosition(centerX - buttonWidth / 2f, centerY + 50);

        simulationButton.setOnClick(() -> {
            soundManager.playSound("ui_click");
            simulationScreen.loadWorld("solarSystem");
            manager.setScreen("simulation");
        });

        // Quit button — exits the application cleanly via LibGDX's platform-safe exit
        UIButton quitButton = new UIButton("QUIT", font);
        quitButton.setSize(buttonWidth, buttonHeight);
        quitButton.setPosition(centerX - buttonWidth / 2f, centerY - 120);

        quitButton.setOnClick(() -> {
            soundManager.playSound("ui_click");
            Gdx.app.exit();
        });

        uiLayer.add(simulationButton);
        uiLayer.add(quitButton);

        // UIInputSystem handles converting unprojected mouse coordinates into button click events
        uiInputSystem = new UIInputSystem(ioManager, uiManager);
    }

    @Override
    public void update(float deltaTime) {

        // Unproject mouse from screen pixels to world units before passing to UI hit-testing
        Vector2 mouse = viewport.unproject(
            new Vector2(ioManager.getMouseX(), ioManager.getMouseY())
        );

        uiInputSystem.update(mouse.x, mouse.y);
        uiManager.update(deltaTime);
    }

    @Override
    public void render() {
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        // Background fills the entire world area regardless of window size
        batch.draw(backgroundTexture,
            0, 0,
            viewport.getWorldWidth(),
            viewport.getWorldHeight()
        );
        uiManager.render(batch);
        batch.end();
    }

    // These lifecycle methods are empty — assets are recreated in show() each time the screen becomes active
    @Override public void dispose() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
}
