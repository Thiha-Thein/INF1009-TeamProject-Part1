package io.github.some_example_name.SolarSystemSimulation.MiniGames;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.some_example_name.AbstractEngine.AudioManagement.AudioSystem;
import io.github.some_example_name.AbstractEngine.AudioManagement.SoundManager;
import io.github.some_example_name.AbstractEngine.EntityManagement.EntityManager;
import io.github.some_example_name.AbstractEngine.IOManagement.IOManager;
import io.github.some_example_name.AbstractEngine.ScreenManagement.ISimulation;
import io.github.some_example_name.SolarSystemSimulation.Shared.SimulationFonts;

// Base class for all three minigames.
// Owns every resource and lifecycle step that was copy-pasted across
// FactOrFictionMap, OrderThePlanetsMap and MatchThePlanetMap:
//   - engine references (batch, ioManager, entityManager, audioSystem, onReturn)
//   - viewport injection + fallback
//   - ShapeRenderer creation and disposal
//   - SimulationFonts loading and disposal
//   - background Texture loading and disposal
//   - GameResultPanel construction
//   - resize() and getBackground() implementations
//   - the shared update preamble (ESC check + audioSystem tick)
//
// Subclasses call initBase() at the top of their initialize(), then implement
// only the game-specific logic: setupUI(), setupEntities(), startRound(), etc.
public abstract class AbstractMinigame implements ISimulation {

    // ── engine references ────────────────────────────────────────────────────
    protected final SpriteBatch  batch;
    protected final IOManager    ioManager;
    protected final SoundManager soundManager;
    protected final EntityManager entityManager;
    protected final AudioSystem   audioSystem;
    // called when the player presses ESC to return to the solar system
    protected final Runnable      onReturn;

    // ── shared rendering resources (initialised by initBase) ─────────────────
    protected Viewport       viewport;
    protected ShapeRenderer  shapeRenderer;
    protected SimulationFonts fonts;
    protected Texture         background;
    protected GameResultPanel resultPanel;

    // ── constructor ──────────────────────────────────────────────────────────
    protected AbstractMinigame(SpriteBatch  batch,
                                IOManager    ioManager,
                                SoundManager soundManager,
                                EntityManager entityManager,
                                AudioSystem   audioSystem,
                                Runnable      onReturn) {
        this.batch         = batch;
        this.ioManager     = ioManager;
        this.soundManager  = soundManager;
        this.entityManager = entityManager;
        this.audioSystem   = audioSystem;
        this.onReturn      = onReturn;
    }

    // ── ISimulation: viewport injection

    // SimulationScreen calls this before initialize() so minigames share the
    // engine's FitViewport instead of creating their own camera.
    @Override
    public void setViewport(Viewport v) {
        this.viewport = v;
    }

    // ── shared initialisation helper

    // Call this at the very start of initialize() before any game-specific setup.
    // backgroundPath is the asset path for the background texture, e.g.
    //   "planets/spaceBackground.png"
    protected void initBase(String backgroundPath) {

        // fall back to a local ScreenViewport if SimulationScreen did not inject one
        if (viewport == null) {
            OrthographicCamera camera = new OrthographicCamera();
            viewport = new ScreenViewport(camera);
            viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        }

        shapeRenderer = new ShapeRenderer();
        background    = new Texture(backgroundPath);
        fonts         = new SimulationFonts();

        // resultPanel is shared — every minigame ends with the same result screen
        resultPanel = new GameResultPanel(
            batch, shapeRenderer, viewport,
            fonts.title, fonts.header, fonts.body
        );
    }

    // ── shared update preamble ───────────────────────────────────────────────

    // Call this at the very top of update(). Returns true if the minigame
    // should exit immediately (player pressed ESC). Also ticks the audio system.
    //
    // Usage:
    //   @Override public void update(float dt) {
    //       if (handleCommonUpdate(dt)) return;
    //       // game-specific state machine here
    //   }
    protected boolean handleCommonUpdate(float deltaTime) {

        if (ioManager.wasPressed("escape")) {
            onReturn.run();
            return true;
        }

        // audioSystem processes sound events queued by entities this frame.
        // entityManager.updateAll() is already called by GameMaster — do not call it again here.
        audioSystem.update(entityManager.getEntities());

        return false;
    }

    // ── ISimulation: shared implementations ─────────────────────────────────

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public Texture getBackground() {
        return background;
    }

    // Releases all resources owned by this class.
    // Subclasses that own additional resources should override and call super.dispose().
    @Override
    public void dispose() {
        entityManager.clear();
        if (background    != null) background.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (fonts         != null) fonts.dispose();
    }
}
