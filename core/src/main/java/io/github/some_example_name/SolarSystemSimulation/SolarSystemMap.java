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
import java.util.List;

import io.github.some_example_name.AbstractEngine.EntityManagement.*;
import io.github.some_example_name.AbstractEngine.CollisionManagement.*;
import io.github.some_example_name.AbstractEngine.IOManagement.*;
import io.github.some_example_name.AbstractEngine.MovementManagement.*;
import io.github.some_example_name.AbstractEngine.AudioManagement.*;
import io.github.some_example_name.AbstractEngine.ScreenManagement.*;
import io.github.some_example_name.AbstractEngine.AIManagement.*;
import io.github.some_example_name.SolarSystemSimulation.PlanetData.*;
import io.github.some_example_name.SolarSystemSimulation.PlanetInteractive.*;

// Concrete ISimulation implementation that builds and runs the solar system scene
// Owns the planets, camera, orbit rendering, interaction handler and all UI elements specific to this simulation
public class SolarSystemMap implements ISimulation {

    // Engine managers provided by SimulationScreen — the simulation does not own these
    private final EntityManager entityManager;
    private final MovementManager movementManager;
    private final CollisionManager collisionManager;
    private final SoundManager soundManager;
    private final IOManager ioManager;
    private final AIManager aiManager;
    private final SpriteBatch batch;

    private Texture background;
    private ShapeRenderer shapeRenderer; // used exclusively for drawing orbit ellipses
    private OrthographicCamera camera;
    private ScreenViewport viewport;

    // Handles selecting a planet, animating it to a presentation position and dismissing it
    private PlanetPresentationHandler interactionHandler;

    // UI elements that display information about the selected planet
    private PlanetNameBar planetNameBar;
    private PlanetFactsPanel planetFactsPanel;

    // Used in presentation mode to cycle through comparison planets and calculate their relative sizes
    private PlanetComparisonSelector comparisonSelector;
    private PlanetSizeComparator sizeComparator;

    // Ordered Sun-first list used for UI layout and size comparisons — matches PLANET_DEFS ordering
    private List<PlanetObj> orderedPlanets = new ArrayList<>();

    // Multiple fonts used across different UI sections — all generated from the same typeface at different sizes
    private BitmapFont titleFont;
    private BitmapFont headerFont;
    private BitmapFont bodyFont;
    private BitmapFont statFont;
    private BitmapFont font; // used for planet name labels in comparison mode

    // Static data table describing each planet — avoids hard-coding spawn logic scattered across the class
    // Format: name, mass, size (world units), sprite path, orbit index, starting angle (degrees)
    private static final Object[][] PLANET_DEFS = {
        { "Mercury", 18f,  50f,  "planets/mercury.png", 0, 0f   },
        { "Venus",   27f,  65f,  "planets/venus.png",   1, 45f  },
        { "Earth",   30f,  70f,  "planets/earth.png",   2, 90f  },
        { "Mars",    24f,  55f,  "planets/mars.png",    3, 135f },
        { "Jupiter", 90f, 170f,  "planets/jupiter.png", 4, 180f },
        { "Saturn",  78f, 140f,  "planets/saturn.png",  5, 250f },
        { "Uranus",  36f, 105f,  "planets/uranus.png",  6, 270f },
        { "Neptune", 33f, 100f,  "planets/neptune.png", 7, 315f },
    };

    // Saturn's sprite is wider than its planet body — a larger renderer scale compensates for the ring texture
    private static final int SATURN_INDEX = 5; // index in orderedPlanets, offset by 1 because Sun is at index 0

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

        shapeRenderer = new ShapeRenderer();
    }

    @Override
    // Builds the entire solar system scene — clears previous state first so this can safely be called on re-entry
    public void initialize() {

        entityManager.clear(); // remove any entities from a previous world load

        camera = new OrthographicCamera();
        viewport = new ScreenViewport(camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        shapeRenderer = new ShapeRenderer();

        background = new Texture("planets/spaceBackground.png");

        float screenWidth  = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        // Default center point used to give orbiting planets a starting position before their first orbit update
        float cx = (screenWidth - 600f) / 2f + 300f;
        float cy = (screenHeight - 600f) / 2f + 300f;

        // The Sun has no parent entity and sits at the screen center
        PlanetObj sun = PlanetFactory.create("Sun", 1000f, 400f, "planets/sun.png", null, -1, 0f);

        sun.setInitialPosition(
            (screenWidth - 400f) / 2f,
            (screenHeight - 400f) / 2f
        );

        orderedPlanets.clear();
        orderedPlanets.add(sun); // Sun is always index 0 in orderedPlanets

        // Create planets from the definition table — each gets sun as its orbit parent
        for (Object[] def : PLANET_DEFS) {

            PlanetObj planet = PlanetFactory.create(
                (String) def[0],
                (float)  def[1],
                (float)  def[2],
                (String) def[3],
                sun,
                (int)    def[4],
                (float)  def[5]
            );

            planet.setInitialPosition(cx, cy);

            orderedPlanets.add(planet);
        }

        // Add Sun first so it is at the bottom of the draw stack (rendered behind planets)
        entityManager.addEntity(sun);

        // Add planets in reverse order so closer planets (lower index) draw on top of farther ones
        for (int i = orderedPlanets.size() - 1; i >= 1; i--)
            entityManager.addEntity(orderedPlanets.get(i));

        // Mouse cursor entity — needs to be added after planets so its collider participates in collision detection
        MainObject mouseCursor = new MainObject(ioManager, viewport);
        entityManager.addEntity(mouseCursor);

        entityManager.start(); // calls start() on all entities now that all of them exist

        // Scale up the Sun sprite beyond its world transform size for a more dramatic visual
        sun.getAnimationRenderer().setScale(1.6f);

        // Saturn's ring sprite is larger than its planet body so it needs extra scale to look proportional
        PlanetObj saturn = orderedPlanets.get(SATURN_INDEX + 1); // +1 because index 0 is the Sun
        saturn.getAnimationRenderer().setScale(2.7f);

        interactionHandler = new PlanetPresentationHandler(viewport);

        sizeComparator = new PlanetSizeComparator();

        planetNameBar = new PlanetNameBar(batch, viewport);

        comparisonSelector = new PlanetComparisonSelector(orderedPlanets);

        generateFonts();

        planetFactsPanel = new PlanetFactsPanel(
            batch,
            shapeRenderer,
            viewport,
            titleFont,
            headerFont,
            bodyFont,
            statFont
        );

        planetNameBar.initialize(orderedPlanets); // builds text layout after fonts are ready
    }

    // Generates all fonts from disk — called once during initialize() after the GL context is confirmed available
    private void generateFonts() {

        FreeTypeFontGenerator generator =
            new FreeTypeFontGenerator(Gdx.files.internal("fonts/rajdhani.regular.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter param =
            new FreeTypeFontGenerator.FreeTypeFontParameter();

        param.size = 46;
        titleFont = generator.generateFont(param);

        param.size = 32;
        headerFont = generator.generateFont(param);

        param.size = 26;
        bodyFont = generator.generateFont(param);

        param.size = 28;
        statFont = generator.generateFont(param);

        generator.dispose(); // generator object can be disposed once all fonts are created

        // Second font for the planet name bar uses a different typeface
        FreeTypeFontGenerator labelGen =
            new FreeTypeFontGenerator(Gdx.files.internal("fonts/star_crush.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter labelParam =
            new FreeTypeFontGenerator.FreeTypeFontParameter();

        labelParam.size = 28;
        font = labelGen.generateFont(labelParam);

        labelGen.dispose();
    }

    @Override
    public void update(float deltaTime) {

        interactionHandler.update(deltaTime); // advances any in-progress planet position transition

        // ESC exits presentation mode and returns the planet to its orbit
        if (ioManager.wasPressed("escape") && interactionHandler.isSelected())
            interactionHandler.triggerDeselect(entityManager.getEntities());

        // A and D cycle the comparison planet shown alongside the selected planet
        if (ioManager.wasPressed("a"))
            comparisonSelector.previous();

        if (ioManager.wasPressed("d"))
            comparisonSelector.next();

        // Left-click on a planet triggers presentation mode — hover state is set by collision detection
        if (ioManager.wasPressed("leftClick")) {
            for (AbstractEntity entity : entityManager.getEntities()) {
                if (entity instanceof PlanetObj) {
                    PlanetObj planet = (PlanetObj) entity;
                    if (planet.isMouseOver()) {
                        // Reset hover scale before the presentation handler moves the planet
                        // so it doesn't animate from the enlarged hover size
                        planet.getAnimationRenderer().setScale(planet.getBaseScale());
                        interactionHandler.triggerPresentation(planet, entityManager.getEntities());
                        break;
                    }
                }
            }
        }

        // Separate click check for the name bar — only active when no planet is already selected
        if (ioManager.wasPressed("leftClick") && !interactionHandler.isSelected()) {

            Vector2 mouse = viewport.unproject(
                new Vector2(ioManager.getMouseX(), ioManager.getMouseY())
            );

            PlanetObj clicked = planetNameBar.getClickedPlanet(mouse);

            if (clicked != null)
                interactionHandler.triggerPresentation(clicked, entityManager.getEntities());
        }
    }

    @Override
    public void render(SpriteBatch batch) {

        viewport.apply();

        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        drawBackground();
        drawOrbits();

        // Switch between normal system view and the zoomed planet presentation view
        if (interactionHandler.isPresenting())
            renderPresentation();
        else
            renderSystem();
    }

    // Draws the background texture stretched to fill the entire viewport
    private void drawBackground() {

        batch.begin();
        batch.draw(background, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        batch.end();
    }

    // Draws orbit ellipses — hidden during presentation to focus attention on the selected planet
    private void drawOrbits() {

        if (!interactionHandler.shouldShowOrbits())
            return;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        shapeRenderer.setColor(1f,1f,1f,0.6f); // semi-transparent white

        for (AbstractEntity entity : entityManager.getEntities())
            if (entity instanceof PlanetObj)
                ((PlanetObj) entity).drawOrbit(shapeRenderer);

        shapeRenderer.end();
    }

    // Normal system view — renders all entities and the name bar
    private void renderSystem() {

        batch.begin();
        entityManager.renderAll(batch);
        batch.end();

        planetNameBar.render();
    }

    // Presentation view — draws two planets side by side for size comparison alongside the facts panel
    private void renderPresentation() {

        PlanetObj selected = interactionHandler.getSelectedPlanet();
        if (selected == null) return;

        PlanetObj compare = comparisonSelector.getPlanet(selected);

        float screenWidth = viewport.getWorldWidth();
        float screenHeight = viewport.getWorldHeight();

        if (compare != null) {

            // Get display heights scaled relative to true planetary diameters
            float[] heights = sizeComparator.getDisplayHeights(selected, compare, screenHeight);

            // Selected planet on the upper left; comparison planet on the lower left
            renderComparisonPlanet(selected, heights[0], screenWidth * 0.3f, screenHeight * 0.72f);
            renderComparisonPlanet(compare, heights[1], screenWidth * 0.3f, screenHeight * 0.28f);
        }

        // Facts panel occupies the right side of the screen
        planetFactsPanel.render(
            selected.getPlanetName(),
            selected.getComponent(PlanetDataComponent.class)
        );
    }

    // Renders a single planet at a given center position and display height, with its name label below
    private void renderComparisonPlanet(PlanetObj planet, float height, float cx, float cy) {

        float aspect = planet.getTransform().getWidth() /
            planet.getTransform().getHeight();

        planet.getAnimationRenderer().setVisible(true);

        float h = height;
        float w = h * aspect; // preserve sprite aspect ratio

        float x = cx - w / 2f;
        float y = cy - h / 2f;

        // Create a temporary transform so the renderer draws at the comparison position
        // without permanently moving the planet's actual transform
        Transform transform = new Transform(x,y,w,h);

        batch.begin();
        planet.getAnimationRenderer().update(Gdx.graphics.getDeltaTime());
        planet.getAnimationRenderer().render(batch, transform);
        batch.end();

        // Draw planet name label centered below the planet sprite
        batch.begin();
        font.draw(batch, planet.getPlanetName(),
            x + w/2f - (planet.getPlanetName().length()*4.5f), y-10f);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {

        viewport.update(width, height, true);

        for (AbstractEntity entity : entityManager.getEntities())
            entity.resize(width, height);

        planetNameBar.resize(); // rebuilds text layout positions for the new viewport size
        collisionManager.setWorldBounds(width, height);
    }

    @Override
    public Texture getBackground() {
        return background;
    }

    @Override
    // Releases all resources owned by this simulation — called before switching worlds or screens
    public void dispose() {

        entityManager.clear();

        shapeRenderer.dispose();
        background.dispose();

        // Dispose all fonts individually — they each hold a separate GL texture
        titleFont.dispose();
        headerFont.dispose();
        bodyFont.dispose();
        statFont.dispose();
        font.dispose();
    }
}
