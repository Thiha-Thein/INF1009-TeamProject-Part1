package io.github.some_example_name.SolarSystemSimulation.MiniGames;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;

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
import io.github.some_example_name.AbstractEngine.UIManagement.UIInputSystem;
import io.github.some_example_name.AbstractEngine.UIManagement.UILayer;
import io.github.some_example_name.AbstractEngine.UIManagement.UIManager;
import io.github.some_example_name.SolarSystemSimulation.MainObject;
import io.github.some_example_name.SolarSystemSimulation.MiniGames.MinigameData.MatchQuestion;
import io.github.some_example_name.SolarSystemSimulation.Shared.PlanetAssets;

// Match the Planet minigame — assigned to Saturn.
// A fun fact clue is shown and the player clicks which of four planet sprites it belongs to.
//
// Responsibilities (SRP — game logic only):
//   - question loading, distractor generation and round progression
//   - answer handling and score tracking
//   - ChoiceEntity lifecycle and feedback state
//   - rendering the clue panel and feedback overlay
//
// Delegated to AbstractMinigame:
//   - viewport injection/fallback, ShapeRenderer, SimulationFonts, Texture lifecycle
//   - GameResultPanel construction, resize(), getBackground(), dispose()
//   - ESC check and audioSystem tick in update()
//
// Delegated to MinigameInstructionPanel:
//   - drawing the how-to-play panel in the INSTRUCTIONS state
public class MatchThePlanetMap extends AbstractMinigame {

    private enum GameState { INSTRUCTIONS, PLAYING, FEEDBACK, RESULT }

    // extra engine dependency unique to this minigame
    private final CollisionManager collisionManager;

    // ── UI ───────────────────────────────────────────────────────────────────
    private final UILayer       uiLayer   = new UILayer();
    private       UIInputSystem uiInputSystem;

    // ── game entities ────────────────────────────────────────────────────────
    private final List<AbstractEntity> choiceEntities = new ArrayList<>();
    private AbstractEntity             soundEntity;

    // ── question data ────────────────────────────────────────────────────────
    private static final int   QUESTIONS_PER_ROUND = 5;
    private static final float FEEDBACK_DURATION   = 1.8f;
    private static final String[] ALL_PLANETS = {
        "Sun", "Mercury", "Venus", "Earth", "Mars",
        "Jupiter", "Saturn", "Uranus", "Neptune"
    };

    // ── round state ──────────────────────────────────────────────────────────
    private GameState         gameState;
    private List<MatchQuestion> roundQuestions;
    private int               currentIndex;
    private int               correctCount;
    private boolean           lastAnswerCorrect;
    private float             feedbackTimer;
    private String            playerChoice;
    private List<String>      currentChoices;

    // ── extracted renderers ──────────────────────────────────────────────────
    private MinigameInstructionPanel instructionPanel;
    private final GlyphLayout layout = new GlyphLayout();


    // ── constructor ──────────────────────────────────────────────────────────

    public MatchThePlanetMap(SpriteBatch      batch,
                              IOManager        ioManager,
                              SoundManager     soundManager,
                              EntityManager    entityManager,
                              AudioSystem      audioSystem,
                              CollisionManager collisionManager,
                              Runnable         onReturn) {
        super(batch, ioManager, soundManager, entityManager, audioSystem, onReturn);
        this.collisionManager = collisionManager;
    }


    // ── ISimulation: initialize ──────────────────────────────────────────────

    @Override
    public void initialize() {

        // add game button layer first so resultLayer (added by initBase) renders on top
        uiManager.addLayer(uiLayer);

        // sets up viewport, shapeRenderer, background, fonts, resultPanel + resultLayer
        initBase("planets/spaceBackground.png");

        uiInputSystem = new UIInputSystem(ioManager, uiManager);

        setupSoundEntity();

        instructionPanel = new MinigameInstructionPanel(
            batch, shapeRenderer, viewport,
            fonts.title, fonts.header, fonts.body,
            "MATCH THE PLANET",
            "Which planet does this fact belong to?",
            new String[] {
                "Read the clue shown on screen.",
                "Click the planet sprite you think matches the clue.",
                "",
                QUESTIONS_PER_ROUND + "  questions per round.",
                "",
                "Press  SPACE  or  CLICK  to begin.",
                "Press  ESC  at any time to return."
            }
        );

        gameState = GameState.INSTRUCTIONS;
    }

    private void setupSoundEntity() {
        entityManager.clear();

        soundEntity = new AbstractEntity() {
            @Override public void start() {
                transform = new Transform(0, 0, 1, 1);
                addComponent(SoundEventComponent.class, new SoundEventComponent());
                setTag("match_sound");
            }
            @Override public void update(float deltaTime) {}
            @Override public void resize(int w, int h) {}
        };
        entityManager.addEntity(soundEntity);

        // mouse cursor entity — gives the cursor a Collider so CollisionManager
        // can fire events on ChoiceEntity when the player hovers/clicks
        entityManager.addEntity(new MainObject(ioManager, viewport));
        entityManager.start();
    }


    // ── round logic ──────────────────────────────────────────────────────────

    private void startRound() {
        correctCount   = 0;
        currentIndex   = 0;
        roundQuestions = buildQuestions();
        loadQuestion(0);
        gameState = GameState.PLAYING;
    }

    private List<MatchQuestion> buildQuestions() {
        MatchQuestion[] all  = MinigameDataLoader.getMatchThePlanet();
        List<MatchQuestion> pool = new ArrayList<>();
        for (MatchQuestion q : all) pool.add(q);
        Collections.shuffle(pool);
        return pool.subList(0, Math.min(QUESTIONS_PER_ROUND, pool.size()));
    }

    private void loadQuestion(int pos) {
        for (AbstractEntity e : choiceEntities) entityManager.removeEntity(e);
        choiceEntities.clear();

        MatchQuestion q  = roundQuestions.get(pos);
        float sw         = viewport.getWorldWidth();
        float sh         = viewport.getWorldHeight();

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
        float gapX       = sw * 0.06f;
        float gapY       = sh * 0.04f;
        float startX     = (sw - (spriteSize * 2 + gapX)) / 2f;
        float row1Y      = sh * 0.32f;
        float row0Y      = row1Y - spriteSize - gapY;

        float[] xs = { startX, startX + spriteSize + gapX, startX, startX + spriteSize + gapX };
        float[] ys = { row1Y,  row1Y,  row0Y,  row0Y };

        for (int i = 0; i < 4; i++) {
            ChoiceEntity ce = new ChoiceEntity(choices.get(i), xs[i], ys[i], spriteSize);
            entityManager.addEntity(ce);
            ce.start();
            choiceEntities.add(ce);
        }
    }

    // Named inner class — anonymous classes cannot both extend AbstractEntity AND
    // implement ICollision, so a named inner class is required here.
    // CollisionManager fires onCollisionUpdate when the MainObject cursor overlaps this entity.
    private class ChoiceEntity extends AbstractEntity implements ICollision {
        private final String choice;
        private final float  cx, cy, size;
        private Collider collider;

        ChoiceEntity(String choice, float x, float y, float size) {
            this.choice = choice; this.cx = x; this.cy = y; this.size = size;
        }

        @Override public void start() {
            transform = new Transform(cx, cy, size, size);
            AnimationRenderer ar = new AnimationRenderer();
            ar.addAnimation("spin",
                PlanetAssets.SPRITE_PATHS.getOrDefault(choice, "planets/earth.png"),
                30, 8, 0.08f, true);
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
        @Override public void onCollisionExit(AbstractEntity other) {}

        @Override
        public void onCollisionUpdate(AbstractEntity other) {
            if ("mouse".equals(other.getTag())
                    && gameState == GameState.PLAYING
                    && ioManager.wasPressed("leftClick")) {
                handleAnswer(choice);
            }
        }
    }

    private void handleAnswer(String choiceName) {
        MatchQuestion q = roundQuestions.get(currentIndex);
        lastAnswerCorrect = choiceName.equals(q.getPlanet());
        playerChoice      = choiceName;
        if (lastAnswerCorrect) correctCount++;

        SoundEventComponent sfx = soundEntity.getComponent(SoundEventComponent.class);
        if (sfx != null) sfx.request("ui_click");

        feedbackTimer = FEEDBACK_DURATION;
        gameState     = GameState.FEEDBACK;
    }

    private void advanceQuestion() {
        currentIndex++;
        if (currentIndex >= roundQuestions.size()) {
            for (AbstractEntity e : choiceEntities) entityManager.removeEntity(e);
            choiceEntities.clear();
            showResult(correctCount, roundQuestions.size());
            gameState = GameState.RESULT;
        } else {
            loadQuestion(currentIndex);
            gameState = GameState.PLAYING;
        }
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
            case FEEDBACK:
                feedbackTimer -= deltaTime;
                if (feedbackTimer <= 0f) advanceQuestion();
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
            case INSTRUCTIONS: instructionPanel.render(); break;
            case PLAYING:      renderGame(); break;
            case FEEDBACK:     renderGame(); renderFeedbackOverlay(); break;
            case RESULT:       break;
        }

        // uiManager renders buttons (uiLayer) AND the result panel (resultLayer) every frame.
        // resultPanel is hidden until showResult() makes it visible, so this is always safe.
        batch.begin();
        uiManager.render(batch);
        batch.end();
    }

    private void renderGame() {
        float sw = viewport.getWorldWidth();
        float sh = viewport.getWorldHeight();
        float pw = sw * 0.65f, ph = sh * 0.30f;
        float px = (sw - pw) / 2f, py = sh * 0.60f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0.15f, 0.97f);
        shapeRenderer.rect(px, py, pw, ph);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.5f, 0.7f, 1f, 1f);
        shapeRenderer.rect(px, py, pw, ph);
        shapeRenderer.end();

        batch.begin();
        entityManager.renderAll(batch);
        batch.end();

        batch.begin();
        float tx = px + 30f, tw = pw - 60f;

        fonts.stat.setColor(Color.LIGHT_GRAY);
        layout.setText(fonts.stat,
            "Question  " + (currentIndex + 1) + "  of  " + roundQuestions.size(),
            Color.LIGHT_GRAY, tw, Align.right, false);
        fonts.stat.draw(batch, layout, tx, py + ph - 15f);

        fonts.stat.setColor(Color.YELLOW);
        layout.setText(fonts.stat, "Score:  " + correctCount);
        fonts.stat.draw(batch, layout, tx, py + ph - 15f);
        fonts.stat.setColor(Color.WHITE);

        fonts.header.setColor(Color.CYAN);
        layout.setText(fonts.header, "Which planet does this fact belong to?",
            Color.CYAN, tw, Align.center, false);
        fonts.header.draw(batch, layout, tx, py + ph - 40f);
        fonts.header.setColor(Color.WHITE);

        MatchQuestion q = roundQuestions.get(currentIndex);
        layout.setText(fonts.body, q.getClue(), Color.WHITE, tw, Align.center, true);
        fonts.body.draw(batch, layout, tx, py + ph / 2f + layout.height / 2f);

        float spriteSize = sh * 0.18f;
        float gapX  = sw * 0.06f;
        float gapY  = sh * 0.04f;
        float startX = (sw - (spriteSize * 2 + gapX)) / 2f;
        float row1Y  = sh * 0.32f;
        float row0Y  = row1Y - spriteSize - gapY;
        float[] xs = { startX, startX + spriteSize + gapX, startX, startX + spriteSize + gapX };
        float[] ys = { row1Y, row1Y, row0Y, row0Y };

        for (int i = 0; i < 4 && i < currentChoices.size(); i++) {
            layout.setText(fonts.body, currentChoices.get(i), Color.WHITE, spriteSize, Align.center, false);
            fonts.body.draw(batch, layout, xs[i], ys[i] - 8f);
        }

        layout.setText(fonts.body, "ESC  —  Return to Solar System",
            Color.DARK_GRAY, sw, Align.center, false);
        fonts.body.setColor(Color.DARK_GRAY);
        fonts.body.draw(batch, layout, 0, 30f);
        fonts.body.setColor(Color.WHITE);
        batch.end();
    }

    private void renderFeedbackOverlay() {
        float sw = viewport.getWorldWidth();
        float sh = viewport.getWorldHeight();

        MatchQuestion q      = roundQuestions.get(currentIndex);
        float spriteSize     = sh * 0.18f;
        float gapX           = sw * 0.06f;
        float gapY           = sh * 0.04f;
        float startX         = (sw - (spriteSize * 2 + gapX)) / 2f;
        float row1Y          = sh * 0.32f;
        float row0Y          = row1Y - spriteSize - gapY;
        float[] xs = { startX, startX + spriteSize + gapX, startX, startX + spriteSize + gapX };
        float[] ys = { row1Y, row1Y, row0Y, row0Y };

        for (int i = 0; i < 4 && i < currentChoices.size(); i++) {
            String  choice     = currentChoices.get(i);
            boolean isCorrect  = choice.equals(q.getPlanet());
            boolean isSelected = choice.equals(playerChoice);
            if (isCorrect || (isSelected && !lastAnswerCorrect)) {
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                shapeRenderer.setColor(isCorrect ? Color.GREEN : Color.RED);
                shapeRenderer.rect(xs[i] - 4, ys[i] - 4, spriteSize + 8, spriteSize + 8);
                shapeRenderer.end();
            }
        }

        Color  feedColor = lastAnswerCorrect ? Color.GREEN : Color.RED;
        String verdict   = lastAnswerCorrect ? "Correct!" : "Wrong!";

        batch.begin();
        layout.setText(fonts.header, verdict, feedColor, sw * 0.6f, Align.center, false);
        fonts.header.setColor(feedColor);
        fonts.header.draw(batch, layout, (sw - sw * 0.6f) / 2f, sh * 0.10f);
        fonts.header.setColor(Color.WHITE);
        if (!lastAnswerCorrect) {
            layout.setText(fonts.body, "It was  " + q.getPlanet(),
                Color.LIGHT_GRAY, sw * 0.6f, Align.center, false);
            fonts.body.draw(batch, layout, (sw - sw * 0.6f) / 2f, sh * 0.06f);
        }
        batch.end();
    }
}
