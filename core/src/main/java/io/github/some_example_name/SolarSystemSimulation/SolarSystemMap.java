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

public class SolarSystemMap implements ISimulation {

    // Core ECS managers provided by the engine
    private final EntityManager entityManager;
    private final MovementManager movementManager;
    private final CollisionManager collisionManager;
    private final SoundManager soundManager;
    private final IOManager ioManager;
    private final AIManager aiManager;
    private final SpriteBatch batch;

    // Rendering objects used for drawing background, shapes and UI
    private Texture background;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private ScreenViewport viewport;

    // Interaction and UI handlers
    private PlanetPresentationHandler interactionHandler;
    private PlanetNameBar planetNameBar;
    private PlanetFactsPanel planetFactsPanel;

    // Comparison system used in presentation mode
    private PlanetComparisonSelector comparisonSelector;
    private PlanetSizeComparator sizeComparator;

    // Ordered list of planets used for UI and comparison
    private List<PlanetObj> orderedPlanets = new ArrayList<>();

    // Fonts used in UI
    private BitmapFont titleFont;
    private BitmapFont headerFont;
    private BitmapFont bodyFont;
    private BitmapFont statFont;
    private BitmapFont font;

    // Static definitions describing the planets to create
    // Format: name, mass, size, sprite path, orbit index, starting angle
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

    // Index used to locate Saturn for sprite scaling adjustment
    private static final int SATURN_INDEX = 5;

    // Constructor receives all engine managers
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

        // Create renderer for orbit lines
        shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void initialize() {

        // Clear previous entities before loading map
        entityManager.clear();

        // Setup camera and viewport
        camera = new OrthographicCamera();
        viewport = new ScreenViewport(camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        // Renderer used to draw orbit lines
        shapeRenderer = new ShapeRenderer();

        // Load space background texture
        background = new Texture("planets/spaceBackground.png");

        // Get screen dimensions
        float screenWidth  = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        // Center point used for planet spawning
        float cx = (screenWidth - 600f) / 2f + 300f;
        float cy = (screenHeight - 600f) / 2f + 300f;

        // Create the Sun (no parent because it does not orbit)
        PlanetObj sun = PlanetFactory.create("Sun", 1000f, 400f, "planets/sun.png", null, -1, 0f);

        // Place the Sun at the center of the screen
        sun.setInitialPosition(
            (screenWidth - 400f) / 2f,
            (screenHeight - 400f) / 2f
        );

        orderedPlanets.clear();
        orderedPlanets.add(sun);

        // Create all other planets from the definitions list
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

            // Give planets an initial center position before orbit starts
            planet.setInitialPosition(cx, cy);

            orderedPlanets.add(planet);
        }

        // Add Sun first
        entityManager.addEntity(sun);

        // Add other planets in reverse order for correct render layering
        for (int i = orderedPlanets.size() - 1; i >= 1; i--)
            entityManager.addEntity(orderedPlanets.get(i));

        MainObject mouseCursor = new MainObject(ioManager, viewport);
        entityManager.addEntity(mouseCursor);
        // Start all entities
        entityManager.start();

        // Increase Sun size visually
        sun.getAnimationRenderer().setScale(1.6f);

        // Adjust Saturn sprite scale
        PlanetObj saturn = orderedPlanets.get(SATURN_INDEX + 1);
        saturn.getAnimationRenderer().setScale(2.7f);

        // Create interaction handler responsible for presentation mode
        interactionHandler = new PlanetPresentationHandler(viewport);

        // Comparator used to calculate relative planet sizes
        sizeComparator = new PlanetSizeComparator();

        // Create planet name bar UI
        planetNameBar = new PlanetNameBar(batch, viewport);

        // Create comparison selector used during presentation mode
        comparisonSelector = new PlanetComparisonSelector(orderedPlanets);

        // Generate fonts used in UI
        generateFonts();

        // Create planet information panel
        planetFactsPanel = new PlanetFactsPanel(
            batch,
            shapeRenderer,
            viewport,
            titleFont,
            headerFont,
            bodyFont,
            statFont
        );

        // Initialize name bar with planet list
        planetNameBar.initialize(orderedPlanets);
    }

    // Generates fonts used for UI and labels
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

        generator.dispose();

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

        // Update interaction system
        interactionHandler.update(deltaTime);

        // Exit presentation mode
        if (ioManager.wasPressed("escape") && interactionHandler.isSelected())
            interactionHandler.triggerDeselect(entityManager.getEntities());

        // Cycle comparison planets
        if (ioManager.wasPressed("a"))
            comparisonSelector.previous();

        if (ioManager.wasPressed("d"))
            comparisonSelector.next();

        if (ioManager.wasPressed("leftClick")) {
            for (AbstractEntity entity : entityManager.getEntities()) {
                if (entity instanceof PlanetObj) {
                    PlanetObj planet = (PlanetObj) entity;
                    // PlanetObj tells us if mouse collider is touching it
                    if (planet.isMouseOver()) {
                        // Reset hover scale before entering presentation
                        planet.getAnimationRenderer().setScale(planet.getBaseScale());
                        interactionHandler.triggerPresentation(planet, entityManager.getEntities());
                        break;
                    }
                }
            }
        }


                // Handle clicking on planet names
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

        // Apply viewport before rendering
        viewport.apply();

        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        drawBackground();
        drawOrbits();

        // Choose between system view and presentation view
        if (interactionHandler.isPresenting())
            renderPresentation();
        else
            renderSystem();
    }

    // Draws background texture
    private void drawBackground() {

        batch.begin();
        batch.draw(background, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        batch.end();
    }

    // Draw orbit ellipses around planets
    private void drawOrbits() {

        if (!interactionHandler.shouldShowOrbits())
            return;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        // Reset orbit color each frame
        shapeRenderer.setColor(1f,1f,1f,0.6f);

        for (AbstractEntity entity : entityManager.getEntities())
            if (entity instanceof PlanetObj)
                ((PlanetObj) entity).drawOrbit(shapeRenderer);

        shapeRenderer.end();
    }

    // Render normal solar system view
    private void renderSystem() {

        batch.begin();
        entityManager.renderAll(batch);
        batch.end();

        planetNameBar.render();
    }

    // Render presentation comparison mode
    private void renderPresentation() {

        PlanetObj selected = interactionHandler.getSelectedPlanet();
        if (selected == null) return;

        PlanetObj compare = comparisonSelector.getPlanet(selected);

        float screenWidth = viewport.getWorldWidth();
        float screenHeight = viewport.getWorldHeight();

        if (compare != null) {

            float[] heights = sizeComparator.getDisplayHeights(selected, compare, screenHeight);

            renderComparisonPlanet(selected, heights[0], screenWidth * 0.3f, screenHeight * 0.72f);
            renderComparisonPlanet(compare, heights[1], screenWidth * 0.3f, screenHeight * 0.28f);
        }

        planetFactsPanel.render(
            selected.getPlanetName(),
            selected.getComponent(PlanetDataComponent.class)
        );
    }

    // Draw a single comparison planet and its label
    private void renderComparisonPlanet(PlanetObj planet, float height, float cx, float cy) {

        float aspect = planet.getTransform().getWidth() /
            planet.getTransform().getHeight();

        // Ensure renderer is visible during presentation mode
        planet.getAnimationRenderer().setVisible(true);

        float h = height;
        float w = h * aspect;

        float x = cx - w / 2f;
        float y = cy - h / 2f;

        Transform transform = new Transform(x,y,w,h);

        batch.begin();
        planet.getAnimationRenderer().update(Gdx.graphics.getDeltaTime());
        planet.getAnimationRenderer().render(batch, transform);
        batch.end();

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

        planetNameBar.resize();
        collisionManager.setWorldBounds(width, height);
    }

    @Override
    public Texture getBackground() {
        return background;
    }

    @Override
    public void dispose() {

        entityManager.clear();

        shapeRenderer.dispose();
        background.dispose();

        titleFont.dispose();
        headerFont.dispose();
        bodyFont.dispose();
        statFont.dispose();
        font.dispose();
    }
}
