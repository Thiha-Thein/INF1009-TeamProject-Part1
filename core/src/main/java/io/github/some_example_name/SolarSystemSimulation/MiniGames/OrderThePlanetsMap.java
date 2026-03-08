package io.github.some_example_name.SolarSystemSimulation.MiniGames;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.github.some_example_name.AbstractEngine.AudioManagement.AudioSystem;
import io.github.some_example_name.AbstractEngine.AudioManagement.SoundEventComponent;
import io.github.some_example_name.AbstractEngine.AudioManagement.SoundManager;
import io.github.some_example_name.AbstractEngine.EntityManagement.AbstractEntity;
import io.github.some_example_name.AbstractEngine.EntityManagement.AnimationRenderer;
import io.github.some_example_name.AbstractEngine.EntityManagement.EntityManager;
import io.github.some_example_name.AbstractEngine.EntityManagement.Transform;
import io.github.some_example_name.AbstractEngine.IOManagement.IOManager;
import io.github.some_example_name.AbstractEngine.ScreenManagement.ISimulation;
import io.github.some_example_name.AbstractEngine.UIManagement.*;

// Order the Planets minigame — assigned to Jupiter
// Player arranges all 9 bodies (including the Sun) smallest to largest by diameter
// Planet sprites animate and lerp into shadow slots when placed
// ESC returns to the solar system at any time
public class OrderThePlanetsMap implements ISimulation {

    // the four states the game can be in
    private enum GameState { INSTRUCTIONS, PLAYING, CONFIRMING, RESULT }

    // correct order from smallest to largest by real diameter in km
    // Mercury 4879, Mars 6779, Venus 12104, Earth 12742,
    // Neptune 49528, Uranus 50724, Saturn 116460, Jupiter 139820, Sun 1391000
    private static final String[] CORRECT_ORDER = {
        "Mercury", "Mars", "Venus", "Earth",
        "Neptune", "Uranus", "Saturn", "Jupiter", "Sun"
    };

    // radii used to draw the shadow slot circles — scaled to hint at the correct answer
    private static final float[] DISPLAY_RADII = {
        14f, 17f, 22f, 23f, 32f, 34f, 44f, 52f, 70f
    };

    // total number of planets including the Sun
    private static final int PLANET_COUNT = CORRECT_ORDER.length;
    // how long the confirm result is shown before moving to the result screen
    private static final float CONFIRM_DURATION = 2.5f;
    // how fast planet sprites lerp toward their target positions each frame
    private static final float LERP_SPEED = 8f;

    // sprite sheet paths keyed by planet name — same as MatchThePlanetMap
    private static final java.util.Map<String, String> SPRITE_PATHS =
        new java.util.HashMap<String, String>() {{
            put("Sun", "planets/sun.png");
            put("Mercury", "planets/mercury.png");
            put("Venus", "planets/venus.png");
            put("Earth", "planets/earth.png");
            put("Mars", "planets/mars.png");
            put("Jupiter", "planets/jupiter.png");
            put("Saturn", "planets/saturn.png");
            put("Uranus", "planets/uranus.png");
            put("Neptune", "planets/neptune.png");
        }};

    private final SpriteBatch batch;
    private final IOManager ioManager;
    private final SoundManager soundManager;
    private final EntityManager entityManager;
    private final AudioSystem audioSystem;
    // called when the player presses ESC to return to the solar system
    private final Runnable onReturn;

    private Texture background;
    private OrthographicCamera camera;
    private ScreenViewport viewport;
    private ShapeRenderer shapeRenderer;

    // four font sizes matching the facts panel style
    private BitmapFont titleFont;
    private BitmapFont headerFont;
    private BitmapFont bodyFont;
    private BitmapFont statFont;

    // reused each frame to measure text without allocating
    private final GlyphLayout layout = new GlyphLayout();

    // confirm button sits in its own layer so it renders above the planet cards
    private final UIManager uiManager = new UIManager();
    private final UILayer slotLayer = new UILayer();
    private final UILayer confirmLayer = new UILayer();
    private UIInputSystem uiInputSystem;
    private UIButton confirmButton;

    // invisible buttons placed over each planet sprite and each shadow slot for click detection
    private UIButton[] cardButtons;
    private UIButton[] slotUIButtons;

    // one AnimationRenderer entity per planet — lifecycle managed by entityManager
    private AbstractEntity[] planetEntities;

    // the default top-row position each planet returns to when cleared from a slot
    private float[] homeX;
    private float[] homeY;

    // current rendered position — updated each frame by lerping toward the target
    private float[] currentX;
    private float[] currentY;

    // target position — set to a slot center when placed, or home when cleared
    private float[] targetX;
    private float[] targetY;

    // center X and Y of each shadow slot in the bottom row
    private float[] slotCX;
    private float[] slotCY;

    // minimal entity that only carries SoundEventComponent — consumed by AudioSystem each frame
    private AbstractEntity soundEntity;

    private GameState gameState;
    // planet names in the shuffled order shown in the top row
    private List<String> shuffledPlanets;
    // tracks which planet name has been placed in each slot — null means the slot is empty
    private String[] playerSlots;
    // the name of the planet card the player has currently selected
    private String selectedCard;

    // whether each slot was correct after the player confirmed — used to color the slots
    private boolean[] slotCorrect;
    // counts down from CONFIRM_DURATION before switching to the result screen
    private float confirmTimer;
    // how many slots the player got right — passed to the result panel
    private int correctCountForResult;

    // shared result screen used by all minigames
    private GameResultPanel resultPanel;

    // constructor — stores all engine references needed to run the minigame
    public OrderThePlanetsMap(SpriteBatch batch,
                              IOManager ioManager,
                              SoundManager soundManager,
                              EntityManager entityManager,
                              AudioSystem audioSystem,
                              Runnable onReturn) {

        this.batch = batch;
        this.ioManager = ioManager;
        this.soundManager = soundManager;
        this.entityManager = entityManager;
        this.audioSystem = audioSystem;
        this.onReturn = onReturn;
    }

    // sets up the camera, fonts, UI, sound entity and shows the instruction screen
    @Override
    public void initialize() {

        camera = new OrthographicCamera();
        viewport = new ScreenViewport(camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        shapeRenderer = new ShapeRenderer();
        background = new Texture("planets/spaceBackground.png");

        generateFonts();
        setupUI();
        setupSoundEntity();

        resultPanel = new GameResultPanel(batch, shapeRenderer, viewport, titleFont, headerFont, bodyFont);

        gameState = GameState.INSTRUCTIONS;
    }

    // generates four font sizes from the rajdhani font file
    private void generateFonts() {

        FreeTypeFontGenerator generator =
            new FreeTypeFontGenerator(Gdx.files.internal("fonts/rajdhani.regular.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter param =
            new FreeTypeFontGenerator.FreeTypeFontParameter();

        param.size = 46; titleFont = generator.generateFont(param);
        param.size = 32; headerFont = generator.generateFont(param);
        param.size = 26; bodyFont = generator.generateFont(param);
        param.size = 28; statFont = generator.generateFont(param);

        generator.dispose();
    }

    // registers both UI layers and creates the input system for button click detection
    private void setupUI() {

        uiManager.addLayer(slotLayer);
        uiManager.addLayer(confirmLayer);
        uiInputSystem = new UIInputSystem(ioManager, uiManager);
    }

    // creates a minimal entity whose only purpose is carrying SoundEventComponent
    private void setupSoundEntity() {

        entityManager.clear();

        soundEntity = new AbstractEntity() {

            @Override
            public void start() {
                // 1x1 transform is required by AbstractEntity but invisible on screen
                transform = new Transform(0, 0, 1, 1);
                addComponent(SoundEventComponent.class, new SoundEventComponent());
                setTag("order_sound");
            }

            @Override public void update(float delta) {}
            @Override public void resize(int w, int h) {}
        };

        entityManager.addEntity(soundEntity);
        entityManager.start();
    }

    // resets state, shuffles planet order and builds all entities and buttons for the round
    private void startRound() {

        selectedCard = null;
        playerSlots = new String[PLANET_COUNT];

        shuffledPlanets = new ArrayList<>(Arrays.asList(CORRECT_ORDER));
        Collections.shuffle(shuffledPlanets);

        buildPlanetEntities();
        buildSlotButtons();
        buildConfirmButton();

        gameState = GameState.PLAYING;
    }

    // spawns one AnimationRenderer entity per planet positioned in the shuffled top row
    // also creates invisible UIButtons for click detection and records home/target positions
    private void buildPlanetEntities() {

        // remove any planet entities left over from a previous round
        if (planetEntities != null)
            for (AbstractEntity e : planetEntities) entityManager.removeEntity(e);

        // clear card buttons from the slot layer
        for (UIElement e : new ArrayList<>(slotLayer.getElements())) slotLayer.remove(e);

        float sw = viewport.getWorldWidth();
        float sh = viewport.getWorldHeight();
        // sprite size is 10% of screen height so they fit in the top row regardless of resolution
        float sprSize = sh * 0.10f;
        float gap = (sw - sprSize * PLANET_COUNT) / (PLANET_COUNT + 1);
        float rowY = sh * 0.68f;

        planetEntities = new AbstractEntity[PLANET_COUNT];
        cardButtons = new UIButton[PLANET_COUNT];
        homeX = new float[PLANET_COUNT];
        homeY = new float[PLANET_COUNT];
        currentX = new float[PLANET_COUNT];
        currentY = new float[PLANET_COUNT];
        targetX = new float[PLANET_COUNT];
        targetY = new float[PLANET_COUNT];

        for (int i = 0; i < PLANET_COUNT; i++) {

            final int idx = i;
            final String name = shuffledPlanets.get(i);
            final float px = gap + i * (sprSize + gap);
            final float py = rowY;

            // home, current and target all start at the same position
            homeX[i] = currentX[i] = targetX[i] = px;
            homeY[i] = currentY[i] = targetY[i] = py;

            planetEntities[i] = new AbstractEntity() {

                @Override
                public void start() {
                    transform = new Transform(px, py, sprSize, sprSize);
                    AnimationRenderer ar = new AnimationRenderer();
                    ar.addAnimation("spin", SPRITE_PATHS.getOrDefault(name, "planets/earth.png"), 30, 8, 0.08f, true);
                    // sun and saturn sprites appear smaller due to corona/rings — bump up scale to match SolarSystemMap
                    if (name.equals("Sun")) ar.setScale(2.1f);
                    if (name.equals("Saturn")) ar.setScale(2.7f);
                    setAnimationRenderer(ar);
                    setTag("order_" + name);
                }

                @Override public void update(float delta) {}
                @Override public void resize(int w, int h) {}
            };

            entityManager.addEntity(planetEntities[i]);
            // start manually so we do not restart the sound entity
            planetEntities[i].start();

            // invisible button sits exactly over the sprite and fires handleCardClick when pressed
            cardButtons[i] = new UIButton("", bodyFont);
            cardButtons[i].setSize(sprSize, sprSize);
            cardButtons[i].setPosition(px, py);
            cardButtons[i].setOnClick(() -> handleCardClick(idx));
            slotLayer.add(cardButtons[i]);
        }

        // compute the center position of each shadow slot in the bottom row
        slotCX = new float[PLANET_COUNT];
        slotCY = new float[PLANET_COUNT];

        float slotRowCY = sh * 0.28f;
        float slotGap = (sw - sprSize * PLANET_COUNT) / (PLANET_COUNT + 1);

        for (int i = 0; i < PLANET_COUNT; i++) {
            slotCX[i] = slotGap + i * (sprSize + slotGap) + sprSize / 2f;
            slotCY[i] = slotRowCY;
        }
    }

    // creates invisible UIButtons over each shadow slot so the player can click them
    private void buildSlotButtons() {

        float sh = viewport.getWorldHeight();
        float sw = viewport.getWorldWidth();
        float sprSize = sh * 0.10f;
        float slotGap = (sw - sprSize * PLANET_COUNT) / (PLANET_COUNT + 1);

        slotUIButtons = new UIButton[PLANET_COUNT];

        for (int i = 0; i < PLANET_COUNT; i++) {

            final int idx = i;
            float bx = slotGap + i * (sprSize + slotGap);
            // position the button so its center matches the slot circle center
            float by = slotCY[i] - sprSize / 2f;

            slotUIButtons[i] = new UIButton("", bodyFont);
            slotUIButtons[i].setSize(sprSize, sprSize);
            slotUIButtons[i].setPosition(bx, by);
            slotUIButtons[i].setOnClick(() -> handleSlotClick(idx));
            slotLayer.add(slotUIButtons[i]);
        }
    }

    // creates the CONFIRM button shown in the center-bottom when all slots are filled
    private void buildConfirmButton() {

        // clear any previous confirm button first
        for (UIElement e : new ArrayList<>(confirmLayer.getElements())) confirmLayer.remove(e);

        float sw = viewport.getWorldWidth();
        float sh = viewport.getWorldHeight();

        confirmButton = new UIButton("CONFIRM", headerFont);
        confirmButton.setSize(sw * 0.18f, 52f);
        confirmButton.setPosition((sw - sw * 0.18f) / 2f, sh * 0.06f);
        confirmButton.setOnClick(this::confirmOrder);
        // hidden until every slot is filled
        confirmButton.setVisible(false);

        confirmLayer.add(confirmButton);
    }

    // toggles selection of a card — already-placed cards are blocked until cleared from their slot
    private void handleCardClick(int idx) {

        String name = shuffledPlanets.get(idx);

        // block selection if this planet is already placed in a slot
        for (String p : playerSlots)
            if (name.equals(p)) return;

        // tap the same card again to deselect it
        selectedCard = name.equals(selectedCard) ? null : name;

        SoundEventComponent sfx = soundEntity.getComponent(SoundEventComponent.class);
        if (sfx != null) sfx.request("ui_click");
    }

    // places the selected card into a slot, or clears an existing placement if no card is held
    private void handleSlotClick(int slotIdx) {

        if (selectedCard != null) {

            // cannot place into an already-occupied slot
            if (playerSlots[slotIdx] != null) return;

            playerSlots[slotIdx] = selectedCard;

            // move the planet sprite's lerp target to the slot center position
            int cardIdx = shuffledPlanets.indexOf(selectedCard);
            float sh = viewport.getWorldHeight();
            float sprSize = sh * 0.10f;
            targetX[cardIdx] = slotCX[slotIdx] - sprSize / 2f;
            targetY[cardIdx] = slotCY[slotIdx] - sprSize / 2f;

            selectedCard = null;

        } else if (playerSlots[slotIdx] != null) {

            // no card held — snap the planet in this slot back to its home row position
            int cardIdx = shuffledPlanets.indexOf(playerSlots[slotIdx]);
            targetX[cardIdx] = homeX[cardIdx];
            targetY[cardIdx] = homeY[cardIdx];
            playerSlots[slotIdx] = null;
        }

        SoundEventComponent sfx = soundEntity.getComponent(SoundEventComponent.class);
        if (sfx != null) sfx.request("ui_click");

        // show the confirm button only when every slot has a planet in it
        boolean allFilled = true;
        for (String s : playerSlots) if (s == null) { allFilled = false; break; }
        confirmButton.setVisible(allFilled);
    }

    // checks each slot against the correct order and starts the confirm countdown
    private void confirmOrder() {

        slotCorrect = new boolean[PLANET_COUNT];
        correctCountForResult = 0;

        for (int i = 0; i < PLANET_COUNT; i++) {
            slotCorrect[i] = CORRECT_ORDER[i].equals(playerSlots[i]);
            if (slotCorrect[i]) correctCountForResult++;
        }

        SoundEventComponent sfx = soundEntity.getComponent(SoundEventComponent.class);
        if (sfx != null) sfx.request("ui_click");

        // show the colored slot result for CONFIRM_DURATION seconds before showing the result screen
        confirmTimer = CONFIRM_DURATION;
        gameState = GameState.CONFIRMING;
    }

    // called every frame — handles ESC, updates entities and drives the state machine
    @Override
    public void update(float deltaTime) {

        if (ioManager.wasPressed("escape")) { onReturn.run(); return; }

        // advance animation frames and process pending sound events
        entityManager.updateAll(deltaTime);
        audioSystem.update(entityManager.getEntities());

        switch (gameState) {

            case INSTRUCTIONS:
                if (ioManager.wasPressed("space") || ioManager.wasPressed("leftClick"))
                    startRound();
                break;

            case PLAYING:
                // lerp each planet sprite toward its current target position
                for (int i = 0; i < PLANET_COUNT; i++) {
                    currentX[i] += (targetX[i] - currentX[i]) * LERP_SPEED * deltaTime;
                    currentY[i] += (targetY[i] - currentY[i]) * LERP_SPEED * deltaTime;
                    // keep the entity transform and its invisible click button in sync
                    planetEntities[i].getTransform().setPosition(new Vector2(currentX[i], currentY[i]));
                    cardButtons[i].setPosition(currentX[i], currentY[i]);
                }

                Vector2 mouse = viewport.unproject(new Vector2(ioManager.getMouseX(), ioManager.getMouseY()));
                uiInputSystem.update(mouse.x, mouse.y);
                uiManager.update(deltaTime);
                break;

            case CONFIRMING:
                // keep lerping sprites during the confirm phase
                for (int i = 0; i < PLANET_COUNT; i++) {
                    currentX[i] += (targetX[i] - currentX[i]) * LERP_SPEED * deltaTime;
                    currentY[i] += (targetY[i] - currentY[i]) * LERP_SPEED * deltaTime;
                    planetEntities[i].getTransform().setPosition(new Vector2(currentX[i], currentY[i]));
                }

                // count down then switch to the result screen
                confirmTimer -= deltaTime;
                if (confirmTimer <= 0f) gameState = GameState.RESULT;
                break;

            case RESULT:
                break;
        }
    }

    // called every frame — applies viewport and delegates drawing to the current state
    @Override
    public void render(SpriteBatch batch) {

        viewport.apply();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        switch (gameState) {
            case INSTRUCTIONS: renderInstructions(); break;
            case PLAYING: case CONFIRMING: renderGame(); break;
            case RESULT:
                resultPanel.render(correctCountForResult, PLANET_COUNT);
                break;
        }
    }

    // draws the how-to-play panel shown before the round starts
    private void renderInstructions() {

        float sw = viewport.getWorldWidth();
        float sh = viewport.getWorldHeight();
        float pw = sw * 0.62f, ph = sh * 0.70f;
        float px = (sw - pw) / 2f, py = (sh - ph) / 2f;

        // dark filled panel background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0.15f, 0.97f);
        shapeRenderer.rect(px, py, pw, ph);
        shapeRenderer.end();

        // blue-white border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.5f, 0.7f, 1f, 1f);
        shapeRenderer.rect(px, py, pw, ph);
        shapeRenderer.end();

        batch.begin();

        float tx = px + 40f, tw = pw - 80f;
        float ty = py + ph - 40f;

        // game title
        titleFont.setColor(Color.CYAN);
        layout.setText(titleFont, "ORDER THE PLANETS", Color.CYAN, tw, Align.center, false);
        titleFont.draw(batch, layout, tx, ty);
        titleFont.setColor(Color.WHITE);
        ty -= layout.height + 20f;

        // subtitle
        layout.setText(headerFont, "Smallest to Largest by Diameter", Color.WHITE, tw, Align.center, false);
        headerFont.draw(batch, layout, tx, ty);
        ty -= layout.height + 35f;

        // instruction lines — empty strings add a gap between sections
        String[] lines = {
            "Nine planets (including the Sun) are shown on the top row.",
            "Nine shadow slots wait in the bottom row.",
            "Slot 1 = smallest,   Slot 9 = largest.",
            "",
            "Click a planet to select it.",
            "Then click a shadow slot to place it.",
            "Click a filled slot to return the planet.",
            "",
            "Fill all 9 slots then click  CONFIRM.",
            "",
            "Press  SPACE  or  CLICK  to begin.",
            "Press  ESC  at any time to return."
        };

        for (String line : lines) {
            if (line.isEmpty()) { ty -= 14f; continue; }
            layout.setText(bodyFont, line, Color.LIGHT_GRAY, tw, Align.center, false);
            bodyFont.draw(batch, layout, tx, ty);
            ty -= layout.height + 10f;
        }

        batch.end();
    }

    // draws the active game — planet row, slot row, labels, selection ring and confirm button
    private void renderGame() {

        float sw = viewport.getWorldWidth();
        float sh = viewport.getWorldHeight();
        float sprSize = sh * 0.10f;

        // game title at the top of the screen
        batch.begin();
        titleFont.setColor(Color.CYAN);
        layout.setText(titleFont, "ORDER THE PLANETS", Color.CYAN, sw, Align.center, false);
        titleFont.draw(batch, layout, 0, sh - 20f);
        titleFont.setColor(Color.WHITE);
        batch.end();

        // faint horizontal divider between the top planet row and the bottom slot row
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.3f, 0.5f, 0.8f, 0.4f);
        shapeRenderer.line(sw * 0.05f, sh * 0.55f, sw * 0.95f, sh * 0.55f);
        shapeRenderer.end();

        // shadow slot circles — each one is sized to hint at which planet belongs there
        for (int i = 0; i < PLANET_COUNT; i++) {

            float r = DISPLAY_RADII[i];

            // during confirm phase, fill the slot green if correct, red if wrong
            if (gameState == GameState.CONFIRMING) {
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(slotCorrect[i] ? new Color(0f, 0.5f, 0.1f, 0.5f)
                                                      : new Color(0.5f, 0f, 0.05f, 0.5f));
                shapeRenderer.circle(slotCX[i], slotCY[i], r + 6f);
                shapeRenderer.end();
            }

            // outline — brighter when the slot is occupied
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(playerSlots[i] != null
                ? new Color(0.4f, 0.7f, 1f, 0.8f)
                : new Color(0.2f, 0.3f, 0.6f, 0.35f));
            shapeRenderer.circle(slotCX[i], slotCY[i], r);
            shapeRenderer.end();
        }

        // slot number labels below each circle
        batch.begin();
        for (int i = 0; i < PLANET_COUNT; i++) {
            bodyFont.setColor(new Color(0.5f, 0.7f, 1f, 0.6f));
            layout.setText(bodyFont, String.valueOf(i + 1));
            bodyFont.draw(batch, layout, slotCX[i] - layout.width / 2f, slotCY[i] - DISPLAY_RADII[i] - 6f);
        }
        bodyFont.setColor(Color.WHITE);
        batch.end();

        // SMALLEST and LARGEST labels at the ends of the slot row
        batch.begin();
        statFont.setColor(new Color(0.5f, 0.7f, 1f, 0.5f));
        layout.setText(statFont, "SMALLEST");
        statFont.draw(batch, layout, slotCX[0] - layout.width / 2f, sh * 0.14f);
        layout.setText(statFont, "LARGEST");
        statFont.draw(batch, layout, slotCX[PLANET_COUNT - 1] - layout.width / 2f, sh * 0.14f);
        statFont.setColor(Color.WHITE);
        batch.end();

        // yellow selection ring drawn around whichever planet card is currently selected
        if (selectedCard != null) {
            int selIdx = shuffledPlanets.indexOf(selectedCard);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.YELLOW);
            shapeRenderer.rect(currentX[selIdx] - 4, currentY[selIdx] - 4, sprSize + 8, sprSize + 8);
            shapeRenderer.end();
        }

        // draw all planet sprites through the entity manager
        batch.begin();
        entityManager.renderAll(batch);
        batch.end();

        // planet name labels below each card — faded out when the planet is placed in a slot
        batch.begin();
        for (int i = 0; i < PLANET_COUNT; i++) {
            String name = shuffledPlanets.get(i);
            boolean placed = false;
            for (String p : playerSlots) if (name.equals(p)) { placed = true; break; }
            // dim the label to show the card is no longer available
            bodyFont.setColor(placed ? new Color(0.3f, 0.3f, 0.5f, 0.4f) : new Color(0.8f, 0.9f, 1f, 0.9f));
            layout.setText(bodyFont, name);
            bodyFont.draw(batch, layout, homeX[i] + sprSize / 2f - layout.width / 2f, homeY[i] - 8f);
        }
        bodyFont.setColor(Color.WHITE);
        batch.end();

        // helper text showing the selected card name when something is held
        if (selectedCard != null) {
            batch.begin();
            statFont.setColor(Color.YELLOW);
            layout.setText(statFont, "Selected:  " + selectedCard + "   — click a slot");
            statFont.draw(batch, layout, (sw - layout.width) / 2f, sh * 0.50f);
            statFont.setColor(Color.WHITE);
            batch.end();
        }

        // score summary shown briefly during the confirm phase before the result screen
        if (gameState == GameState.CONFIRMING) {
            batch.begin();
            String msg = correctCountForResult + "  out of  " + PLANET_COUNT + "  correct!";
            Color c = correctCountForResult == PLANET_COUNT ? Color.GREEN : Color.YELLOW;
            headerFont.setColor(c);
            layout.setText(headerFont, msg, c, sw, Align.center, false);
            headerFont.draw(batch, layout, 0, sh * 0.52f);
            headerFont.setColor(Color.WHITE);
            batch.end();
        }

        // draw the confirm button background only when the button is visible
        if (confirmButton != null && confirmButton.isVisible()) {
            float bw = sw * 0.18f;
            float bx = (sw - bw) / 2f;
            float by = sh * 0.06f;

            // green filled background to make the button stand out
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.1f, 0.4f, 0.15f, 1f);
            shapeRenderer.rect(bx, by, bw, 52f);
            shapeRenderer.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(0.3f, 1f, 0.4f, 1f);
            shapeRenderer.rect(bx, by, bw, 52f);
            shapeRenderer.end();
        }

        // UIManager draws the CONFIRM button text label on top of the shape renderer background
        batch.begin();
        uiManager.render(batch);

        // ESC hint at the bottom of the screen
        layout.setText(bodyFont, "ESC  —  Return to Solar System", Color.DARK_GRAY, sw, Align.center, false);
        bodyFont.setColor(Color.DARK_GRAY);
        bodyFont.draw(batch, layout, 0, 30f);
        bodyFont.setColor(Color.WHITE);

        batch.end();
    }

    // called when the window is resized — updates the viewport to match the new size
    @Override
    public void resize(int width, int height) { viewport.update(width, height, true); }

    // returns the background texture used by SimulationScreen
    @Override
    public Texture getBackground() { return background; }

    // frees all resources when switching away from this world
    @Override
    public void dispose() {

        entityManager.clear();
        if (background != null) background.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (titleFont != null) titleFont.dispose();
        if (headerFont != null) headerFont.dispose();
        if (bodyFont != null) bodyFont.dispose();
        if (statFont != null) statFont.dispose();
    }
}
