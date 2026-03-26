package io.github.some_example_name.SolarSystemSimulation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.some_example_name.AbstractEngine.EntityManagement.*;
import io.github.some_example_name.AbstractEngine.CollisionManagement.*;
import io.github.some_example_name.AbstractEngine.IOManagement.*;
import io.github.some_example_name.AbstractEngine.MovementManagement.*;
import io.github.some_example_name.AbstractEngine.AudioManagement.*;
import io.github.some_example_name.AbstractEngine.ScreenManagement.*;
import io.github.some_example_name.AbstractEngine.AIManagement.*;
import io.github.some_example_name.AbstractEngine.UIManagement.*;
import io.github.some_example_name.SolarSystemSimulation.PlanetData.*;
import io.github.some_example_name.SolarSystemSimulation.PlanetInteractive.*;
import io.github.some_example_name.SolarSystemSimulation.Shared.PlanetAssets;
import io.github.some_example_name.SolarSystemSimulation.Shared.SimulationFonts;

// the main solar system world — shows the sun and all planets orbiting it
// handles planet selection, presentation mode, size comparison and minigame launch
public class SolarSystemMap implements ISimulation {

    // engine managers injected from SimulationScreen
    private final EntityManager entityManager;
    private final MovementManager movementManager;
    private final CollisionManager collisionManager;
    private final SoundManager soundManager;
    private final IOManager ioManager;
    private final AIManager aiManager;
    private final SpriteBatch batch;

    // rendering objects for background, orbit lines and UI
    private Texture background;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private ScreenViewport viewport;

    // handles clicking a planet and entering presentation mode
    private PlanetPresentationHandler interactionHandler;
    // draws the planet name bar along the top of the screen — registered with uiManager
    private PlanetNameBar planetNameBar;
    // draws the facts panel when a planet is selected — registered with uiManager
    private PlanetFactsPanel planetFactsPanel;

    // lets the player cycle through planets to compare sizes
    private PlanetComparisonSelector comparisonSelector;
    // calculates how tall to draw each planet in the comparison view
    private PlanetSizeComparator sizeComparator;

    // all planets in solar system order including the sun
    private List<PlanetObj> orderedPlanets = new ArrayList<>();

    // maps planet names to the minigame that should open when the play button is clicked
    // filled in by SimulationScreen after all worlds are ready
    private Map<String, Runnable> gameCallbacks = new HashMap<>();

    // UI pipeline — PlanetNameBar and PlanetFactsPanel are UIElements registered here
    // instead of being rendered by direct render() calls
    private final UIManager uiManager = new UIManager();
    private final UILayer   uiLayer   = new UILayer();

    // shared panel fonts — four sizes used across all UI panels in this world
    private SimulationFonts fonts;
    // star_crush typeface used only for the planet name labels in the sidebar
    // kept separate because it is unique to this world and not part of SimulationFonts
    private BitmapFont font;

    // each row defines: name, mass, visual size, sprite path, orbit slot index, starting angle
    // sprite paths are pulled from PlanetAssets so adding a planet only requires one file change
    private static final Object[][] PLANET_DEFS = {
        { "Mercury", 18f, 50f, PlanetAssets.SPRITE_PATHS.get("Mercury"), 0, 0f },
        { "Venus",   27f, 65f, PlanetAssets.SPRITE_PATHS.get("Venus"),   1, 45f },
        { "Earth",   30f, 70f, PlanetAssets.SPRITE_PATHS.get("Earth"),   2, 90f },
        { "Mars",    24f, 55f, PlanetAssets.SPRITE_PATHS.get("Mars"),    3, 135f },
        { "Jupiter", 90f, 170f, PlanetAssets.SPRITE_PATHS.get("Jupiter"), 4, 180f },
        { "Saturn",  78f, 140f, PlanetAssets.SPRITE_PATHS.get("Saturn"), 5, 250f },
        { "Uranus",  36f, 105f, PlanetAssets.SPRITE_PATHS.get("Uranus"), 6, 270f },
        { "Neptune", 33f, 100f, PlanetAssets.SPRITE_PATHS.get("Neptune"), 7, 315f },
    };

    // position of Saturn in PLANET_DEFS — needed to apply its ring scale adjustment
    private static final int SATURN_INDEX = 5;

    // called by SimulationScreen to pass in the minigame launch callbacks
    // also forwards them to the facts panel if it is already built
    @Override
    public void setGameCallbacks(Map<String, Runnable> callbacks) {
        this.gameCallbacks = callbacks;
        if (planetFactsPanel != null)
            planetFactsPanel.setGameCallbacks(callbacks);
    }

    // constructor — stores all engine managers and creates the shape renderer
    public SolarSystemMap(EntityManager entityManager,
                          MovementManager movementManager,
                          CollisionManager collisionManager,
                          SoundManager soundManager,
                          IOManager ioManager,
                          AIManager aiManager,
                          SpriteBatch batch) {

        this.entityManager = entityManager;
        this.movementManager = movementManager;
        this.collisionManager = collisionManager;
        this.soundManager = soundManager;
        this.ioManager = ioManager;
        this.aiManager = aiManager;
        this.batch = batch;

        // shape renderer is used to draw orbit ellipses
        shapeRenderer = new ShapeRenderer();
    }

    // sets up the camera, creates all planet entities, and initialises all UI systems
    @Override
    public void initialize() {

        // remove any entities left over from a previous world
        entityManager.clear();

        // set up the camera and match it to the current screen size
        camera = new OrthographicCamera();
        viewport = new ScreenViewport(camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        shapeRenderer = new ShapeRenderer();

        background = new Texture("planets/spaceBackground.png");

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        // center point used as the spawn position before orbits take over
        float cx = (screenWidth - 600f) / 2f + 300f;
        float cy = (screenHeight - 600f) / 2f + 300f;

        // create the sun — it has no parent because it does not orbit anything
        float sunSize = ScaleUtil.px(400f);
        PlanetObj sun = PlanetFactory.create("Sun", 1000f, sunSize, PlanetAssets.SPRITE_PATHS.get("Sun"), null, -1, 0f);

        // position the sun in the center of the screen
        sun.setInitialPosition(
            (screenWidth - sunSize) / 2f,
            (screenHeight - sunSize) / 2f
        );

        orderedPlanets.clear();
        orderedPlanets.add(sun);

        // create each planet using the data defined in PLANET_DEFS
        for (Object[] def : PLANET_DEFS) {

            PlanetObj planet = PlanetFactory.create(
                (String) def[0],
                (float) def[1],
                ScaleUtil.px((float) def[2]),
                (String) def[3],
                sun,
                (int) def[4],
                (float) def[5]
            );

            // start all planets at the center before their orbit takes over
            planet.setInitialPosition(cx, cy);

            orderedPlanets.add(planet);
        }

        // add sun first so it renders underneath everything else
        entityManager.addEntity(sun);

        // add planets in reverse order so closer ones draw on top of farther ones
        for (int i = orderedPlanets.size() - 1; i >= 1; i--)
            entityManager.addEntity(orderedPlanets.get(i));

        // the mouse cursor entity handles hover detection on planets
        MainObject mouseCursor = new MainObject(ioManager, viewport);
        entityManager.addEntity(mouseCursor);

        // call start() on all registered entities
        entityManager.start();

        // make the sun visually larger than its default size
        sun.getAnimationRenderer().setScale(1.6f);

        // saturn's rings make the sprite look smaller — increase its scale to compensate
        PlanetObj saturn = orderedPlanets.get(SATURN_INDEX + 1);
        saturn.getAnimationRenderer().setScale(2.7f);

        // set up the system that handles clicking a planet and entering presentation mode
        interactionHandler = new PlanetPresentationHandler(viewport);

        // used to calculate the display heights of two planets side by side
        sizeComparator = new PlanetSizeComparator();

        // load all four shared font sizes
        fonts = new SimulationFonts();

        // separate font for planet name labels using the star_crush typeface
        FreeTypeFontGenerator labelGen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/star_crush.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter labelParam = new FreeTypeFontGenerator.FreeTypeFontParameter();
        labelParam.size = ScaleUtil.fontSize(28);
        font = labelGen.generateFont(labelParam);
        labelGen.dispose();

        // --- UI pipeline setup ---
        // Both PlanetNameBar and PlanetFactsPanel are UIElements owned by uiLayer.
        // uiManager.render(batch) at the end of render() drives them both.

        uiManager.addLayer(uiLayer);

        // PlanetNameBar no longer takes a batch — it receives one from UILayer at render time
        planetNameBar = new PlanetNameBar(viewport);
        planetNameBar.initialize(orderedPlanets);
        uiLayer.add(planetNameBar);

        // the facts panel shows planet data and the play game button
        // starts hidden; setVisible(true) is called when a planet is selected
        planetFactsPanel = new PlanetFactsPanel(
            shapeRenderer,
            viewport,
            fonts.title,
            fonts.header,
            fonts.body,
            fonts.stat
        );
        planetFactsPanel.setVisible(false);
        uiLayer.add(planetFactsPanel);

        // lets the player switch which planet is shown in the comparison column
        comparisonSelector = new PlanetComparisonSelector(orderedPlanets);

        // pass callbacks in case they were set before initialize() was called
        planetFactsPanel.setGameCallbacks(gameCallbacks);
    }

    // runs every frame — handles ESC, comparison cycling, planet clicks and play button clicks
    @Override
    public void update(float deltaTime) {

        interactionHandler.update(deltaTime);

        // ESC closes the facts panel and returns all planets to normal orbit
        if (ioManager.wasPressed("escape") && interactionHandler.isSelected()) {
            soundManager.playSound("ui_click");
            planetFactsPanel.setVisible(false);
            interactionHandler.triggerDeselect(entityManager.getEntities());
        }

        // A/D cycle through comparison planets in the side column
        if (ioManager.wasPressed("a"))
            comparisonSelector.previous();

        if (ioManager.wasPressed("d"))
            comparisonSelector.next();

        // feed the panel fresh state every frame so it always reflects the selected planet
        if (interactionHandler.isPresenting()) {
            PlanetObj selected = interactionHandler.getSelectedPlanet();
            if (selected != null) {
                planetFactsPanel.setState(
                    selected.getPlanetName(),
                    selected.getComponent(PlanetDataComponent.class)
                );

                // check if the play game button was clicked
                Vector2 mouse = viewport.unproject(
                    new Vector2(ioManager.getMouseX(), ioManager.getMouseY())
                );
                planetFactsPanel.checkPlayGameClick(
                    selected.getPlanetName(),
                    mouse.x, mouse.y,
                    ioManager.wasPressed("leftClick")
                );
            }
        }

        // check if the player clicked directly on a planet sprite
        if (ioManager.wasPressed("leftClick")) {
            for (AbstractEntity entity : entityManager.getEntities()) {
                if (entity instanceof PlanetObj) {
                    PlanetObj planet = (PlanetObj) entity;
                    // isMouseOver is set by the mouse cursor entity each frame
                    if (planet.isMouseOver()) {
                        // reset hover scale before entering presentation so the planet looks normal
                        planet.getAnimationRenderer().setScale(planet.getBaseScale());
                        soundManager.playSound("ui_click");
                        planetFactsPanel.setVisible(true);
                        interactionHandler.triggerPresentation(planet, entityManager.getEntities());
                        break;
                    }
                }
            }
        }

        // check if the player clicked a name in the name bar while no planet is selected
        if (ioManager.wasPressed("leftClick") && !interactionHandler.isSelected()) {

            Vector2 mouse = viewport.unproject(
                new Vector2(ioManager.getMouseX(), ioManager.getMouseY())
            );

            PlanetObj clicked = planetNameBar.getClickedPlanet(mouse);

            if (clicked != null) {
                soundManager.playSound("ui_click");
                planetFactsPanel.setVisible(true);
                interactionHandler.triggerPresentation(clicked, entityManager.getEntities());
            }
        }
    }

    // draws the solar system — chooses between system view and presentation view each frame
    @Override
    public void render(SpriteBatch batch) {

        viewport.apply();

        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);
        drawOrbits();

        // show presentation mode when a planet is selected, otherwise show the full system
        if (interactionHandler.isPresenting())
            renderPresentation();
        else
            renderSystem();

        // render all registered UIElements (PlanetNameBar + PlanetFactsPanel) on top
        batch.begin();
        uiManager.render(batch);
        batch.end();
    }

    // draws a circular orbit line for each planet using the shape renderer
    private void drawOrbits() {

        // orbits are hidden during presentation mode
        if (!interactionHandler.shouldShowOrbits())
            return;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        // semi-transparent white for orbit lines
        shapeRenderer.setColor(1f, 1f, 1f, 0.6f);

        for (AbstractEntity entity : entityManager.getEntities())
            if (entity instanceof PlanetObj)
                ((PlanetObj) entity).drawOrbit(shapeRenderer);

        shapeRenderer.end();
    }

    // draws all planet sprites in the normal solar system view
    // PlanetNameBar is now drawn by uiManager.render() at the end of render()
    private void renderSystem() {
        batch.begin();
        entityManager.renderAll(batch);
        batch.end();
    }

    // draws the selected planet and a comparison planet side by side with the facts panel
    // PlanetFactsPanel is now drawn by uiManager.render() at the end of render()
    private void renderPresentation() {

        PlanetObj selected = interactionHandler.getSelectedPlanet();
        if (selected == null) return;

        // get the planet to compare against from the selector
        PlanetObj compare = comparisonSelector.getPlanet(selected);

        float screenWidth = viewport.getWorldWidth();
        float screenHeight = viewport.getWorldHeight();

        if (compare != null) {
            // get scaled display heights so both planets fit on screen proportionally
            float[] heights = sizeComparator.getDisplayHeights(selected, compare, screenHeight);
            // draw the selected planet in the upper column and the comparison in the lower
            renderComparisonPlanet(selected, heights[0], screenWidth * 0.3f, screenHeight * 0.72f);
            renderComparisonPlanet(compare, heights[1], screenWidth * 0.3f, screenHeight * 0.28f);
        }
    }

    // draws one planet sprite at a given height, centered on cx/cy, with its name below
    private void renderComparisonPlanet(PlanetObj planet, float height, float cx, float cy) {

        // preserve the original width-to-height ratio when scaling
        float aspect = planet.getTransform().getWidth() /
            planet.getTransform().getHeight();

        // make sure the planet is visible even if it was hidden during orbit
        planet.getAnimationRenderer().setVisible(true);

        float h = height;
        float w = h * aspect;

        // top-left corner of the sprite based on the given center position
        float x = cx - w / 2f;
        float y = cy - h / 2f;

        // temporary transform so the sprite draws at the comparison size, not its orbit size
        Transform transform = new Transform(x, y, w, h);

        batch.begin();
        planet.getAnimationRenderer().update(Gdx.graphics.getDeltaTime());
        planet.getAnimationRenderer().render(batch, transform);
        batch.end();

        // draw the planet name just below the sprite using the star_crush label font
        batch.begin();
        font.draw(batch, planet.getPlanetName(),
            x + w / 2f - (planet.getPlanetName().length() * 4.5f), y - 10f);
        batch.end();
    }

    // called when the window is resized — updates viewport, all entities, name bar and collision bounds
    @Override
    public void resize(int width, int height) {

        viewport.update(width, height, true);

        for (AbstractEntity entity : entityManager.getEntities())
            entity.resize(width, height);

        if (planetNameBar != null)
            planetNameBar.resize();

        collisionManager.setWorldBounds(width, height);
    }

    // returns the background texture so SimulationScreen can draw it before the world renders
    @Override
    public Texture getBackground() {
        return background;
    }

    // frees all resources — called when switching away from this world
    @Override
    public void dispose() {

        entityManager.clear();

        shapeRenderer.dispose();
        background.dispose();

        if (fonts != null) fonts.dispose();
        if (font  != null) font.dispose();
        uiLayer.remove(planetNameBar);
        uiLayer.remove(planetFactsPanel);
        
    }
}
