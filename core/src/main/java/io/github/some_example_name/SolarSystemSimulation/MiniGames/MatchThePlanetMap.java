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
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.some_example_name.AbstractEngine.AudioManagement.AudioSystem;
import io.github.some_example_name.AbstractEngine.AudioManagement.SoundEventComponent;
import io.github.some_example_name.AbstractEngine.AudioManagement.SoundManager;
import io.github.some_example_name.AbstractEngine.CollisionManagement.Collider;
import io.github.some_example_name.AbstractEngine.CollisionManagement.CollisionManager;
import io.github.some_example_name.AbstractEngine.CollisionManagement.ICollision;
import io.github.some_example_name.AbstractEngine.EntityManagement.AbstractEntity;
import io.github.some_example_name.AbstractEngine.EntityManagement.AnimationRenderer;
import io.github.some_example_name.AbstractEngine.EntityManagement.EntityManager;
import io.github.some_example_name.AbstractEngine.EntityManagement.Transform;
import io.github.some_example_name.AbstractEngine.IOManagement.IOManager;
import io.github.some_example_name.AbstractEngine.ScreenManagement.ISimulation;
import io.github.some_example_name.AbstractEngine.UIManagement.*;
import io.github.some_example_name.SolarSystemSimulation.MainObject;
import io.github.some_example_name.SolarSystemSimulation.MiniGames.MinigameData.MatchQuestion;
import io.github.some_example_name.SolarSystemSimulation.ScaleUtil;

// Match the Planet minigame — assigned to Saturn in the solar system
// A fun fact clue is shown and the player picks which planet it belongs to
// from four answer choices displayed as animated planet sprites
// Planet sprites are rendered through EntityManager exactly as in SolarSystemMap
// Pressing ESC at any point returns to the solar system map
public class MatchThePlanetMap implements ISimulation {

    // the four states the game can be in
    private enum GameState { INSTRUCTIONS, PLAYING, FEEDBACK, RESULT }

    private final SpriteBatch batch;
    private final IOManager ioManager;
    private final SoundManager soundManager;
    private final EntityManager entityManager;
    private final AudioSystem audioSystem;
    private final CollisionManager collisionManager;
    // called when the player presses ESC to return to the solar system
    private final Runnable onReturn;

    private Texture background;
    // shared viewport injected by SimulationScreen — avoids creating a second camera
    private Viewport viewport;
    private ShapeRenderer shapeRenderer;

    // fonts match PlanetFactsPanel sizes so the UI looks consistent
    private BitmapFont titleFont;   // size 46
    private BitmapFont headerFont;  // size 32
    private BitmapFont bodyFont;    // size 26
    private BitmapFont statFont;    // size 28

    // reused each frame to measure text without allocating new objects
    private final GlyphLayout layout = new GlyphLayout();

    private final UIManager uiManager = new UIManager();
    private final UILayer uiLayer = new UILayer();
    private UIInputSystem uiInputSystem;

    // one entity per answer choice — rebuilt for each question
    // each entity implements ICollision so CollisionManager detects mouse hover and click
    private final List<AbstractEntity> choiceEntities = new ArrayList<>();

    // minimal entity that only carries SoundEventComponent — consumed by AudioSystem each frame
    private AbstractEntity soundEntity;

    // how many questions are shown each round
    private static final int QUESTIONS_PER_ROUND = 5;
    // how long the correct/wrong feedback is shown before moving to the next question
    private static final float FEEDBACK_DURATION = 1.8f;

    // all possible planet names used to generate distractor choices
    private static final String[] ALL_PLANETS = {
        "Sun", "Mercury", "Venus", "Earth", "Mars",
        "Jupiter", "Saturn", "Uranus", "Neptune"
    };

    // sprite sheet paths keyed by planet name — same paths used in SolarSystemMap
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

    private GameState gameState;
    // the subset of questions chosen for this round
    private List<MatchQuestion> roundQuestions;
    // which question in the round we are currently on
    private int currentIndex;
    // running total of correct answers this round
    private int correctCount;
    // whether the last answer was correct — used to color the feedback border
    private boolean lastAnswerCorrect;
    // counts down before advancing to the next question
    private float feedbackTimer;
    // the planet name the player chose — stored so we can highlight the wrong choice
    private String playerChoice;
    // the four answer choices for the current question in display order
    private List<String> currentChoices;

    // shared result screen used by all minigames
    private GameResultPanel resultPanel;


    // constructor — stores all engine references needed to run the minigame
    public MatchThePlanetMap(SpriteBatch batch,
                             IOManager ioManager,
                             SoundManager soundManager,
                             EntityManager entityManager,
                             AudioSystem audioSystem,
                             CollisionManager collisionManager,
                             Runnable onReturn) {

        this.batch = batch;
        this.ioManager = ioManager;
        this.soundManager = soundManager;
        this.entityManager = entityManager;
        this.audioSystem = audioSystem;
        this.collisionManager = collisionManager;
        this.onReturn = onReturn;
    }

    // receives the shared FitViewport from SimulationScreen before initialize() is called
    @Override
    public void setViewport(Viewport viewport) {
        this.viewport = viewport;
    }

    // sets up fonts, UI, sound entity and shows the instruction screen
    @Override
    public void initialize() {

        // if SimulationScreen did not inject a viewport, fall back to a local ScreenViewport
        if (viewport == null) {
            OrthographicCamera camera = new OrthographicCamera();
            viewport = new ScreenViewport(camera);
            viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        }

        shapeRenderer = new ShapeRenderer();
        background = new Texture("planets/spaceBackground.png");

        generateFonts();
        setupUI();
        setupSoundEntity();

        resultPanel = new GameResultPanel(
            batch, shapeRenderer, viewport,
            titleFont, headerFont, bodyFont
        );

        gameState = GameState.INSTRUCTIONS;
    }

    // generates four font sizes from the rajdhani font file matching the facts panel style
    private void generateFonts() {

        FreeTypeFontGenerator generator =
            new FreeTypeFontGenerator(Gdx.files.internal("fonts/rajdhani.regular.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter param =
            new FreeTypeFontGenerator.FreeTypeFontParameter();

        param.size = ScaleUtil.fontSize(46); titleFont = generator.generateFont(param);
        param.size = ScaleUtil.fontSize(32); headerFont = generator.generateFont(param);
        param.size = ScaleUtil.fontSize(26); bodyFont = generator.generateFont(param);
        param.size = ScaleUtil.fontSize(28); statFont = generator.generateFont(param);

        // done generating — free the font file from memory
        generator.dispose();
    }

    // registers the UI layer and creates the input system for button click detection
    private void setupUI() {

        uiManager.addLayer(uiLayer);
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
                setTag("match_sound");
            }

            @Override public void update(float deltaTime) {}
            @Override public void resize(int w, int h) {}
        };

        entityManager.addEntity(soundEntity);

        // mouse cursor entity — same pattern as SolarSystemMap's MainObject
        // gives the cursor a Transform+Collider so CollisionManager can fire events on choice entities
        MainObject mouseCursor = new MainObject(ioManager, viewport);
        entityManager.addEntity(mouseCursor);

        entityManager.start();
    }

    // resets score, builds the question pool and loads the first question
    private void startRound() {

        correctCount = 0;
        currentIndex = 0;
        roundQuestions = buildQuestions();

        loadQuestion(0);
        gameState = GameState.PLAYING;
    }

    // loads questions from minigames.json, shuffles them and picks QUESTIONS_PER_ROUND to use
    private List<MatchQuestion> buildQuestions() {

        MatchQuestion[] all = MinigameDataLoader.getMatchThePlanet();

        List<MatchQuestion> pool = new ArrayList<>();
        for (MatchQuestion q : all) pool.add(q);

        Collections.shuffle(pool);
        // guard against having fewer questions in the file than the round requires
        return pool.subList(0, Math.min(QUESTIONS_PER_ROUND, pool.size()));
    }

    // removes previous choice entities and spawns fresh ChoiceEntity instances for the question at pos
    private void loadQuestion(int pos) {

        for (AbstractEntity e : choiceEntities) entityManager.removeEntity(e);
        choiceEntities.clear();

        MatchQuestion q = roundQuestions.get(pos);
        float sw = viewport.getWorldWidth();
        float sh = viewport.getWorldHeight();

        // build 3 random distractors
        List<String> distractors = new ArrayList<>();
        for (String other : ALL_PLANETS)
            if (!other.equals(q.getPlanet())) distractors.add(other);
        Collections.shuffle(distractors);

        List<String> choices = new ArrayList<>();
        choices.add(q.getPlanet());
        choices.add(distractors.get(0));
        choices.add(distractors.get(1));
        choices.add(distractors.get(2));
        Collections.shuffle(choices);
        currentChoices = choices;

        float spriteSize = sh * 0.18f;
        float gapX      = sw * 0.06f;
        float gapY      = sh * 0.04f;
        float startX    = (sw - (spriteSize * 2 + gapX)) / 2f;
        float row1Y     = sh * 0.32f;
        float row0Y     = row1Y - spriteSize - gapY;

        float[] xs = { startX, startX + spriteSize + gapX, startX, startX + spriteSize + gapX };
        float[] ys = { row1Y, row1Y, row0Y, row0Y };

        for (int i = 0; i < 4; i++) {
            ChoiceEntity ce = new ChoiceEntity(choices.get(i), xs[i], ys[i], spriteSize);
            entityManager.addEntity(ce);
            ce.start();
            choiceEntities.add(ce);
        }
    }

    // Named inner class — Java anonymous classes cannot both extend AbstractEntity AND implement
    // ICollision, so a named inner class is required. CollisionManager fires collision events on
    // this entity when the MainObject mouse cursor overlaps its Collider.
    private class ChoiceEntity extends AbstractEntity implements ICollision {

        private final String choice;
        private final float cx, cy, size;
        private Collider collider;

        ChoiceEntity(String choice, float x, float y, float size) {
            this.choice = choice;
            this.cx = x;
            this.cy = y;
            this.size = size;
        }

        @Override
        public void start() {
            transform = new Transform(cx, cy, size, size);

            AnimationRenderer ar = new AnimationRenderer();
            ar.addAnimation("spin", SPRITE_PATHS.getOrDefault(choice, "planets/earth.png"), 30, 8, 0.08f, true);
            if (choice.equals("Sun"))    ar.setScale(1.6f);
            if (choice.equals("Saturn")) ar.setScale(2.7f);
            setAnimationRenderer(ar);
            setTag("choice_" + choice);

            collider = new Collider(transform);
            addComponent(Collider.class, collider);
        }

        @Override public void update(float deltaTime) {}
        @Override public void resize(int w, int h) {}
        @Override public Collider getCollider() { return collider; }

        @Override public void onCollisionStart(AbstractEntity other) {}

        // click is registered here — fired every frame the cursor overlaps this entity
        @Override
        public void onCollisionUpdate(AbstractEntity other) {
            if ("mouse".equals(other.getTag())
                    && gameState == GameState.PLAYING
                    && ioManager.wasPressed("leftClick")) {
                handleAnswer(choice);
            }
        }

        @Override public void onCollisionExit(AbstractEntity other) {}
    }

    // checks whether the chosen planet is correct, plays a sound and starts the feedback timer
    private void handleAnswer(String choiceName) {

        MatchQuestion q = roundQuestions.get(currentIndex);
        lastAnswerCorrect = choiceName.equals(q.getPlanet());
        playerChoice = choiceName;

        if (lastAnswerCorrect) correctCount++;

        SoundEventComponent sfx = soundEntity.getComponent(SoundEventComponent.class);
        if (sfx != null) sfx.request("ui_click");

        feedbackTimer = FEEDBACK_DURATION;
        gameState = GameState.FEEDBACK;
    }

    // moves to the next question, or switches to the result screen when the round is done
    private void advanceQuestion() {

        currentIndex++;

        if (currentIndex >= roundQuestions.size()) {
            for (AbstractEntity e : choiceEntities) entityManager.removeEntity(e);
            choiceEntities.clear();
            gameState = GameState.RESULT;
        } else {
            loadQuestion(currentIndex);
            gameState = GameState.PLAYING;
        }
    }


    // called every frame — handles ESC, updates entity lifecycle and drives the state machine
    @Override
    public void update(float deltaTime) {

        // ESC always exits the minigame regardless of which state we are in
        if (ioManager.wasPressed("escape")) { onReturn.run(); return; }

        // audioSystem processes sound events queued by entities this frame
        // entityManager.updateAll() is already called by GameMaster's update loop — do not call it again here
        audioSystem.update(entityManager.getEntities());

        switch (gameState) {

            case INSTRUCTIONS:
                // SPACE or a click starts the round
                if (ioManager.wasPressed("space") || ioManager.wasPressed("leftClick"))
                    startRound();
                break;

            case PLAYING:
                // convert raw mouse pixel position to world coordinates for button hit testing
                Vector2 mouse = viewport.unproject(
                    new Vector2(ioManager.getMouseX(), ioManager.getMouseY())
                );
                uiInputSystem.update(mouse.x, mouse.y);
                uiManager.update(deltaTime);
                break;

            case FEEDBACK:
                // count down the feedback timer then move to the next question
                feedbackTimer -= deltaTime;
                if (feedbackTimer <= 0f) advanceQuestion();
                break;

            case RESULT:
                break;
        }
    }


    // called every frame — applies the viewport and delegates drawing to the current state
    @Override
    public void render(SpriteBatch batch) {

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);

        switch (gameState) {
            case INSTRUCTIONS: renderInstructions(); break;
            case PLAYING: case FEEDBACK: renderGame(); break;
            case RESULT:
                resultPanel.render(correctCount, QUESTIONS_PER_ROUND);
                break;
        }
    }

    // draws the how-to-play panel shown before the round starts
    private void renderInstructions() {

        float sw = viewport.getWorldWidth();
        float sh = viewport.getWorldHeight();
        float pw = sw * 0.60f, ph = sh * 0.65f;
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

        // game title in cyan
        titleFont.setColor(Color.CYAN);
        layout.setText(titleFont, "MATCH THE PLANET", Color.CYAN, tw, Align.center, false);
        titleFont.draw(batch, layout, tx, ty);
        titleFont.setColor(Color.WHITE);
        ty -= layout.height + 20f;

        // subtitle describing what the player needs to do
        layout.setText(headerFont, "Which planet does this fact belong to?",
            Color.WHITE, tw, Align.center, false);
        headerFont.draw(batch, layout, tx, ty);
        ty -= layout.height + 35f;

        // instruction lines — empty strings add a gap between sections
        String[] lines = {
            "Read the clue shown on screen.",
            "Click the planet sprite you think matches the clue.",
            "",
            QUESTIONS_PER_ROUND + "  questions per round.",
            "",
            "Press  SPACE  or  CLICK  to begin.",
            "Press  ESC  at any time to return."
        };

        for (String line : lines) {
            if (line.isEmpty()) { ty -= 15f; continue; }
            layout.setText(bodyFont, line, Color.LIGHT_GRAY, tw, Align.center, false);
            bodyFont.draw(batch, layout, tx, ty);
            ty -= layout.height + 12f;
        }

        batch.end();
    }

    // draws the clue panel, the spinning planet sprites and any feedback overlay
    private void renderGame() {

        float sw = viewport.getWorldWidth();
        float sh = viewport.getWorldHeight();

        // clue panel dimensions and position
        float pw = sw * 0.65f, ph = sh * 0.30f;
        float px = (sw - pw) / 2f, py = sh * 0.60f;

        // dark filled panel background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0.15f, 0.97f);
        shapeRenderer.rect(px, py, pw, ph);
        shapeRenderer.end();

        // blue-white panel border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.5f, 0.7f, 1f, 1f);
        shapeRenderer.rect(px, py, pw, ph);
        shapeRenderer.end();

        // during feedback, draw colored borders around the correct and selected sprites
        if (gameState == GameState.FEEDBACK && roundQuestions != null) {

            MatchQuestion q = roundQuestions.get(currentIndex);
            float spriteSize = sh * 0.18f;
            float gapX = sw * 0.06f;
            float gapY = sh * 0.04f;
            float startX = (sw - (spriteSize * 2 + gapX)) / 2f;
            float row1Y = sh * 0.32f;
            float row0Y = row1Y - spriteSize - gapY;

            float[] xs = { startX, startX + spriteSize + gapX, startX, startX + spriteSize + gapX };
            float[] ys = { row1Y, row1Y, row0Y, row0Y };

            for (int i = 0; i < 4 && i < currentChoices.size(); i++) {

                String choice = currentChoices.get(i);
                boolean isCorrect = choice.equals(q.getPlanet());
                boolean isSelected = choice.equals(playerChoice);

                // green border on the correct answer, red border on the wrong selection
                if (isCorrect || (isSelected && !lastAnswerCorrect)) {

                    shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                    shapeRenderer.setColor(isCorrect ? Color.GREEN : Color.RED);
                    shapeRenderer.rect(xs[i] - 4, ys[i] - 4, spriteSize + 8, spriteSize + 8);
                    shapeRenderer.end();
                }
            }
        }

        // draw all planet choice sprites through the entity manager
        batch.begin();
        entityManager.renderAll(batch);
        batch.end();

        batch.begin();

        float tx = px + 30f, tw = pw - 60f;

        // question counter in the top right of the clue panel
        String counter = "Question  " + (currentIndex + 1) + "  of  " + roundQuestions.size();
        statFont.setColor(Color.LIGHT_GRAY);
        layout.setText(statFont, counter, Color.LIGHT_GRAY, tw, Align.right, false);
        statFont.draw(batch, layout, tx, py + ph - 15f);

        // score in the top left of the clue panel
        statFont.setColor(Color.YELLOW);
        layout.setText(statFont, "Score:  " + correctCount);
        statFont.draw(batch, layout, tx, py + ph - 15f);
        statFont.setColor(Color.WHITE);

        // clue prompt header inside the panel
        headerFont.setColor(Color.CYAN);
        layout.setText(headerFont, "Which planet does this fact belong to?",
            Color.CYAN, tw, Align.center, false);
        headerFont.draw(batch, layout, tx, py + ph - 40f);
        headerFont.setColor(Color.WHITE);

        // the clue text — word-wrapped inside the panel
        MatchQuestion q = roundQuestions.get(currentIndex);
        layout.setText(bodyFont, q.getClue(), Color.WHITE, tw, Align.center, true);
        bodyFont.draw(batch, layout, tx, py + ph / 2f + layout.height / 2f);

        // planet name labels drawn below each sprite
        float spriteSize = sh * 0.18f;
        float gapX = sw * 0.06f;
        float gapY = sh * 0.04f;
        float startX = (sw - (spriteSize * 2 + gapX)) / 2f;
        float row1Y = sh * 0.32f;
        float row0Y = row1Y - spriteSize - gapY;

        float[] xs = { startX, startX + spriteSize + gapX, startX, startX + spriteSize + gapX };
        float[] ys = { row1Y, row1Y, row0Y, row0Y };

        for (int i = 0; i < 4 && i < currentChoices.size(); i++) {

            String choice = currentChoices.get(i);
            layout.setText(bodyFont, choice, Color.WHITE, spriteSize, Align.center, false);
            bodyFont.draw(batch, layout, xs[i], ys[i] - 8f);
        }

        // feedback overlay — shown after the player answers
        if (gameState == GameState.FEEDBACK) {

            String verdict = lastAnswerCorrect ? "Correct!" : "Wrong!";
            Color feedColor = lastAnswerCorrect ? Color.GREEN : Color.RED;

            // verdict text colored green or red
            layout.setText(headerFont, verdict, feedColor, sw * 0.6f, Align.center, false);
            headerFont.setColor(feedColor);
            headerFont.draw(batch, layout, (sw - sw * 0.6f) / 2f, sh * 0.10f);
            headerFont.setColor(Color.WHITE);

            // if the answer was wrong, show which planet it actually was
            if (!lastAnswerCorrect) {
                layout.setText(bodyFont, "It was  " + q.getPlanet(),
                    Color.LIGHT_GRAY, sw * 0.6f, Align.center, false);
                bodyFont.draw(batch, layout, (sw - sw * 0.6f) / 2f, sh * 0.06f);
            }
        }

        // ESC hint at the very bottom of the screen
        layout.setText(bodyFont, "ESC  —  Return to Solar System",
            Color.DARK_GRAY, sw, Align.center, false);
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
