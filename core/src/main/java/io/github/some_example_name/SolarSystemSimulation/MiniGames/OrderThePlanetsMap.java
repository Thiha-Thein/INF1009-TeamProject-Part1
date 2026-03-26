package io.github.some_example_name.SolarSystemSimulation.MiniGames;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;

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
import io.github.some_example_name.AbstractEngine.MovementManagement.MovementComponent;
import io.github.some_example_name.AbstractEngine.MovementManagement.MovementManager;
import io.github.some_example_name.AbstractEngine.UIManagement.UIButton;
import io.github.some_example_name.AbstractEngine.UIManagement.UIElement;
import io.github.some_example_name.AbstractEngine.UIManagement.UIInputSystem;
import io.github.some_example_name.AbstractEngine.UIManagement.UILayer;
import io.github.some_example_name.AbstractEngine.UIManagement.UIManager;
import io.github.some_example_name.SolarSystemSimulation.Shared.PlanetAssets;

// Order the Planets minigame — assigned to Jupiter.
// Player arranges all 9 bodies (including the Sun) smallest to largest by diameter.
//
// Responsibilities (SRP — game logic only):
//   - planet card entities, slot buttons and confirm button lifecycle
//   - drag-and-drop selection, placement and snap-back logic
//   - answer checking and confirm phase countdown
//   - rendering the active game board and confirm feedback
//
// Delegated to AbstractMinigame:
//   - viewport injection/fallback, ShapeRenderer, SimulationFonts, Texture lifecycle
//   - GameResultPanel construction, resize(), getBackground(), dispose()
//   - ESC check and audioSystem tick in update()
//
// Delegated to MinigameInstructionPanel:
//   - drawing the how-to-play panel in the INSTRUCTIONS state
public class OrderThePlanetsMap extends AbstractMinigame {

    private enum GameState { INSTRUCTIONS, PLAYING, CONFIRMING, RESULT }

    // correct order smallest to largest by real diameter
    private static final String[] CORRECT_ORDER = {
        "Mercury", "Mars", "Venus", "Earth",
        "Neptune", "Uranus", "Saturn", "Jupiter", "Sun"
    };

    // radii used to draw the shadow slot circles — scaled to hint at the correct answer
    private static final float[] DISPLAY_RADII = {
        14f, 17f, 22f, 23f, 32f, 34f, 44f, 52f, 70f
    };

    private static final int   PLANET_COUNT     = CORRECT_ORDER.length;
    private static final float CONFIRM_DURATION = 2.5f;
    private static final float LERP_SPEED       = 8f;

    // extra engine dependency unique to this minigame
    private final MovementManager movementManager;

    // ── UI ───────────────────────────────────────────────────────────────────
    private final UILayer   slotLayer    = new UILayer();
    private final UILayer   confirmLayer = new UILayer();
    private UIInputSystem   uiInputSystem;
    private UIButton        confirmButton;
    private UIButton[]      slotUIButtons;

    // ── game entities ────────────────────────────────────────────────────────
    private List<PlanetCard>   cards        = new ArrayList<>();
    private AbstractEntity     soundEntity;
    private float[]            slotCX;
    private float[]            slotCY;

    // ── round state ──────────────────────────────────────────────────────────
    private GameState    gameState;
    private List<String> shuffledPlanets;
    private String[]     playerSlots;
    private String       selectedCard;
    private boolean[]    slotCorrect;
    private float        confirmTimer;
    private int          correctCountForResult;

    // ── extracted renderers ──────────────────────────────────────────────────
    private MinigameInstructionPanel instructionPanel;
    private final GlyphLayout layout = new GlyphLayout();


    // ── constructor ──────────────────────────────────────────────────────────

    public OrderThePlanetsMap(SpriteBatch     batch,
                               IOManager       ioManager,
                               SoundManager    soundManager,
                               EntityManager   entityManager,
                               AudioSystem     audioSystem,
                               MovementManager movementManager,
                               Runnable        onReturn) {
        super(batch, ioManager, soundManager, entityManager, audioSystem, onReturn);
        this.movementManager = movementManager;
    }


    // ── ISimulation: initialize ──────────────────────────────────────────────

    @Override
    public void initialize() {

        // add game layers first so resultLayer (added by initBase) renders on top
        uiManager.addLayer(slotLayer);
        uiManager.addLayer(confirmLayer);

        // sets up viewport, shapeRenderer, background, fonts, resultPanel + resultLayer
        initBase("planets/spaceBackground.png");

        uiInputSystem = new UIInputSystem(ioManager, uiManager);

        setupSoundEntity();

        // OrderThePlanets has more instructions so needs a taller panel (0.70)
        instructionPanel = new MinigameInstructionPanel(
            batch, shapeRenderer, viewport,
            fonts.title, fonts.header, fonts.body,
            "ORDER THE PLANETS",
            "Smallest to Largest by Diameter",
            new String[] {
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
            },
            0.70f
        );

        gameState = GameState.INSTRUCTIONS;
    }

    private void setupSoundEntity() {
        entityManager.clear();
        soundEntity = new AbstractEntity() {
            @Override public void start() {
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


    // ── round logic ──────────────────────────────────────────────────────────

    private void startRound() {
        selectedCard = null;
        playerSlots  = new String[PLANET_COUNT];

        shuffledPlanets = new ArrayList<>(Arrays.asList(CORRECT_ORDER));
        Collections.shuffle(shuffledPlanets);

        buildPlanetEntities();
        buildSlotButtons();
        buildConfirmButton();

        gameState = GameState.PLAYING;
    }

    private void buildPlanetEntities() {
        for (PlanetCard card : cards) entityManager.removeEntity(card.entity);
        for (UIElement  e    : new ArrayList<>(slotLayer.getElements())) slotLayer.remove(e);
        cards.clear();

        float sw      = viewport.getWorldWidth();
        float sh      = viewport.getWorldHeight();
        float sprSize = sh * 0.10f;
        float gap     = (sw - sprSize * PLANET_COUNT) / (PLANET_COUNT + 1);
        float rowY    = sh * 0.68f;

        for (int i = 0; i < PLANET_COUNT; i++) {
            final int    idx  = i;
            final String name = shuffledPlanets.get(i);
            final float  px   = gap + i * (sprSize + gap);
            final float  py   = rowY;

            PlanetCard       card    = new PlanetCard(name, px, py);
            final PlanetCard cardRef = card;

            card.entity = new AbstractEntity() {
                @Override public void start() {
                    transform = new Transform(px, py, sprSize, sprSize);
                    AnimationRenderer ar = new AnimationRenderer();
                    ar.addAnimation("spin",
                        PlanetAssets.SPRITE_PATHS.getOrDefault(name, "planets/earth.png"),
                        30, 8, 0.08f, true);
                    if (name.equals("Sun"))    ar.setScale(2.1f);
                    if (name.equals("Saturn")) ar.setScale(2.7f);
                    setAnimationRenderer(ar);
                    setTag("order_" + name);
                    addComponent(MovementComponent.class, new MovementComponent(transform, 0));
                }
                @Override public void update(float delta) {
                    MovementComponent mv = getComponent(MovementComponent.class);
                    if (mv == null) return;
                    cardRef.currentX += (cardRef.targetX - cardRef.currentX) * LERP_SPEED * delta;
                    cardRef.currentY += (cardRef.targetY - cardRef.currentY) * LERP_SPEED * delta;
                    mv.moveTo(cardRef.currentX, cardRef.currentY);
                    cardRef.button.setPosition(cardRef.currentX, cardRef.currentY);
                }
                @Override public void resize(int w, int h) {}
            };

            entityManager.addEntity(card.entity);
            card.entity.start();

            card.button = new UIButton("", fonts.body);
            card.button.setSize(sprSize, sprSize);
            card.button.setPosition(px, py);
            card.button.setOnClick(() -> handleCardClick(idx));
            slotLayer.add(card.button);

            cards.add(card);
        }

        slotCX = new float[PLANET_COUNT];
        slotCY = new float[PLANET_COUNT];
        float slotRowCY = sh * 0.28f;
        float slotGap   = (sw - sprSize * PLANET_COUNT) / (PLANET_COUNT + 1);
        for (int i = 0; i < PLANET_COUNT; i++) {
            slotCX[i] = slotGap + i * (sprSize + slotGap) + sprSize / 2f;
            slotCY[i] = slotRowCY;
        }
    }

    private void buildSlotButtons() {
        float sh      = viewport.getWorldHeight();
        float sw      = viewport.getWorldWidth();
        float sprSize = sh * 0.10f;
        float slotGap = (sw - sprSize * PLANET_COUNT) / (PLANET_COUNT + 1);

        slotUIButtons = new UIButton[PLANET_COUNT];
        for (int i = 0; i < PLANET_COUNT; i++) {
            final int idx = i;
            slotUIButtons[i] = new UIButton("", fonts.body);
            slotUIButtons[i].setSize(sprSize, sprSize);
            slotUIButtons[i].setPosition(slotGap + i * (sprSize + slotGap),
                                          slotCY[i] - sprSize / 2f);
            slotUIButtons[i].setOnClick(() -> handleSlotClick(idx));
            slotLayer.add(slotUIButtons[i]);
        }
    }

    private void buildConfirmButton() {
        for (UIElement e : new ArrayList<>(confirmLayer.getElements())) confirmLayer.remove(e);
        float sw = viewport.getWorldWidth();
        float sh = viewport.getWorldHeight();

        confirmButton = new UIButton("CONFIRM", fonts.header);
        confirmButton.setSize(sw * 0.18f, 52f);
        confirmButton.setPosition((sw - sw * 0.18f) / 2f, sh * 0.06f);
        confirmButton.setOnClick(this::confirmOrder);
        confirmButton.setVisible(false);
        confirmLayer.add(confirmButton);
    }

    private void handleCardClick(int idx) {
        String name = shuffledPlanets.get(idx);
        for (String p : playerSlots) if (name.equals(p)) return;
        selectedCard = name.equals(selectedCard) ? null : name;
        requestSound();
    }

    private void handleSlotClick(int slotIdx) {
        float sh      = viewport.getWorldHeight();
        float sprSize = sh * 0.10f;

        if (selectedCard != null) {
            if (playerSlots[slotIdx] != null) return;
            playerSlots[slotIdx] = selectedCard;
            int cardIdx = shuffledPlanets.indexOf(selectedCard);
            cards.get(cardIdx).targetX = slotCX[slotIdx] - sprSize / 2f;
            cards.get(cardIdx).targetY = slotCY[slotIdx] - sprSize / 2f;
            selectedCard = null;
        } else if (playerSlots[slotIdx] != null) {
            int cardIdx = shuffledPlanets.indexOf(playerSlots[slotIdx]);
            cards.get(cardIdx).targetX = cards.get(cardIdx).homeX;
            cards.get(cardIdx).targetY = cards.get(cardIdx).homeY;
            playerSlots[slotIdx] = null;
        }

        requestSound();

        boolean allFilled = true;
        for (String s : playerSlots) if (s == null) { allFilled = false; break; }
        confirmButton.setVisible(allFilled);
    }

    private void confirmOrder() {
        slotCorrect           = new boolean[PLANET_COUNT];
        correctCountForResult = 0;
        for (int i = 0; i < PLANET_COUNT; i++) {
            slotCorrect[i] = CORRECT_ORDER[i].equals(playerSlots[i]);
            if (slotCorrect[i]) correctCountForResult++;
        }
        requestSound();
        confirmTimer = CONFIRM_DURATION;
        gameState    = GameState.CONFIRMING;
    }

    private void requestSound() {
        SoundEventComponent sfx = soundEntity.getComponent(SoundEventComponent.class);
        if (sfx != null) sfx.request("ui_click");
    }


    // ── ISimulation: update ──────────────────────────────────────────────────

    @Override
    public void update(float deltaTime) {
        if (handleCommonUpdate(deltaTime)) return;

        switch (gameState) {
            case INSTRUCTIONS:
                if (ioManager.wasPressed("space") || ioManager.wasPressed("leftClick"))
                    startRound();
                break;
            case PLAYING:
                Vector2 mouse = viewport.unproject(
                    new Vector2(ioManager.getMouseX(), ioManager.getMouseY()));
                uiInputSystem.update(mouse.x, mouse.y);
                uiManager.update(deltaTime);
                break;
            case CONFIRMING:
                confirmTimer -= deltaTime;
                if (confirmTimer <= 0f) {
                    showResult(correctCountForResult, PLANET_COUNT);
                    gameState = GameState.RESULT;
                }
                break;
            case RESULT:
                break;
        }
    }


    // ── ISimulation: render ──────────────────────────────────────────────────

    @Override
    public void render(SpriteBatch batch) {
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);

        switch (gameState) {
            case INSTRUCTIONS:              instructionPanel.render(); break;
            case PLAYING: case CONFIRMING:  renderGame(); break;
            case RESULT:                    break;
        }

        // uiManager renders buttons (slotLayer, confirmLayer) AND the result panel
        // (resultLayer) every frame. resultPanel is hidden until showResult() makes
        // it visible, so this is always safe.
        batch.begin();
        uiManager.render(batch);
        batch.end();
    }

    private void renderGame() {
        float sw      = viewport.getWorldWidth();
        float sh      = viewport.getWorldHeight();
        float sprSize = sh * 0.10f;

        batch.begin();
        fonts.title.setColor(Color.CYAN);
        layout.setText(fonts.title, "ORDER THE PLANETS", Color.CYAN, sw, Align.center, false);
        fonts.title.draw(batch, layout, 0, sh - 20f);
        fonts.title.setColor(Color.WHITE);
        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.3f, 0.5f, 0.8f, 0.4f);
        shapeRenderer.line(sw * 0.05f, sh * 0.55f, sw * 0.95f, sh * 0.55f);
        shapeRenderer.end();

        for (int i = 0; i < PLANET_COUNT; i++) {
            float r = DISPLAY_RADII[i];
            if (gameState == GameState.CONFIRMING) {
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(slotCorrect[i]
                    ? new Color(0f, 0.5f, 0.1f, 0.5f)
                    : new Color(0.5f, 0f, 0.05f, 0.5f));
                shapeRenderer.circle(slotCX[i], slotCY[i], r + 6f);
                shapeRenderer.end();
            }
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(playerSlots[i] != null
                ? Color.WHITE : new Color(1f, 1f, 1f, 0.5f));
            shapeRenderer.circle(slotCX[i], slotCY[i], r);
            shapeRenderer.end();
        }

        batch.begin();
        for (int i = 0; i < PLANET_COUNT; i++) {
            fonts.body.setColor(new Color(0.5f, 0.7f, 1f, 0.6f));
            layout.setText(fonts.body, String.valueOf(i + 1));
            fonts.body.draw(batch, layout,
                slotCX[i] - layout.width / 2f,
                slotCY[i] - DISPLAY_RADII[i] - 6f);
        }
        fonts.body.setColor(Color.WHITE);
        batch.end();

        batch.begin();
        fonts.stat.setColor(Color.WHITE);
        layout.setText(fonts.stat, "SMALLEST");
        fonts.stat.draw(batch, layout, slotCX[0] - layout.width / 2f, sh * 0.14f);
        layout.setText(fonts.stat, "LARGEST");
        fonts.stat.draw(batch, layout, slotCX[PLANET_COUNT - 1] - layout.width / 2f, sh * 0.14f);
        batch.end();

        if (selectedCard != null) {
            int selIdx = shuffledPlanets.indexOf(selectedCard);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.YELLOW);
            shapeRenderer.rect(
                cards.get(selIdx).currentX - 4,
                cards.get(selIdx).currentY - 4,
                sprSize + 8, sprSize + 8);
            shapeRenderer.end();
        }

        batch.begin();
        entityManager.renderAll(batch);
        batch.end();

        batch.begin();
        for (int i = 0; i < PLANET_COUNT; i++) {
            String  name   = shuffledPlanets.get(i);
            boolean placed = false;
            for (String p : playerSlots) if (name.equals(p)) { placed = true; break; }
            fonts.body.setColor(placed
                ? new Color(0.3f, 0.3f, 0.5f, 0.4f)
                : new Color(0.8f, 0.9f, 1f, 0.9f));
            layout.setText(fonts.body, name);
            fonts.body.draw(batch, layout,
                cards.get(i).homeX + sprSize / 2f - layout.width / 2f,
                cards.get(i).homeY - 8f);
        }
        fonts.body.setColor(Color.WHITE);
        batch.end();

        if (selectedCard != null) {
            batch.begin();
            fonts.stat.setColor(Color.YELLOW);
            layout.setText(fonts.stat, "Selected:  " + selectedCard + "   — click a slot");
            fonts.stat.draw(batch, layout, (sw - layout.width) / 2f, sh * 0.50f);
            fonts.stat.setColor(Color.WHITE);
            batch.end();
        }

        if (gameState == GameState.CONFIRMING) {
            batch.begin();
            String msg = correctCountForResult + "  out of  " + PLANET_COUNT + "  correct!";
            Color  c   = correctCountForResult == PLANET_COUNT ? Color.GREEN : Color.YELLOW;
            fonts.header.setColor(c);
            layout.setText(fonts.header, msg, c, sw, Align.center, false);
            fonts.header.draw(batch, layout, 0, sh * 0.52f);
            fonts.header.setColor(Color.WHITE);
            batch.end();
        }

        if (confirmButton != null && confirmButton.isVisible()) {
            float bw = sw * 0.18f;
            float bx = (sw - bw) / 2f;
            float by = sh * 0.06f;
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.1f, 0.4f, 0.15f, 1f);
            shapeRenderer.rect(bx, by, bw, 52f);
            shapeRenderer.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(0.3f, 1f, 0.4f, 1f);
            shapeRenderer.rect(bx, by, bw, 52f);
            shapeRenderer.end();
        }

        batch.begin();
        layout.setText(fonts.body, "ESC  —  Return to Solar System",
            new Color(0.7f, 0.7f, 0.7f, 1f), sw, Align.center, false);
        fonts.body.setColor(new Color(0.7f, 0.7f, 0.7f, 1f));
        fonts.body.draw(batch, layout, 0, 30f);
        fonts.body.setColor(Color.WHITE);
        batch.end();
    }
}
