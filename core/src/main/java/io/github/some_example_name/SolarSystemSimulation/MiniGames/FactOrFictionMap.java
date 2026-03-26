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
import io.github.some_example_name.AbstractEngine.EntityManagement.AbstractEntity;
import io.github.some_example_name.AbstractEngine.EntityManagement.AnimationRenderer;
import io.github.some_example_name.AbstractEngine.EntityManagement.EntityManager;
import io.github.some_example_name.AbstractEngine.EntityManagement.Transform;
import io.github.some_example_name.AbstractEngine.IOManagement.IOManager;
import io.github.some_example_name.AbstractEngine.UIManagement.UIButton;
import io.github.some_example_name.AbstractEngine.UIManagement.UIInputSystem;
import io.github.some_example_name.AbstractEngine.UIManagement.UILayer;
import io.github.some_example_name.AbstractEngine.UIManagement.UIManager;
import io.github.some_example_name.SolarSystemSimulation.MiniGames.MinigameData.FactOrFictionQuestion;

// Fact or Fiction minigame — assigned to Earth in the solar system.
// A statement about the solar system is shown and the player picks TRUE or FALSE.
//
// Responsibilities (SRP — game logic only):
//   - question loading, shuffling and round progression
//   - answer handling and score tracking
//   - TRUE/FALSE button lifecycle and feedback state
//   - rendering the question panel and feedback overlay
//
// Delegated to AbstractMinigame:
//   - viewport injection/fallback, ShapeRenderer, SimulationFonts, Texture lifecycle
//   - GameResultPanel construction, resize(), getBackground(), dispose()
//   - ESC check and audioSystem tick in update()
//
// Delegated to MinigameInstructionPanel:
//   - drawing the how-to-play panel in the INSTRUCTIONS state
public class FactOrFictionMap extends AbstractMinigame {

    private enum GameState { INSTRUCTIONS, PLAYING, FEEDBACK, RESULT }

    private final UIManager     uiManager = new UIManager();
    private final UILayer       uiLayer   = new UILayer();
    private       UIInputSystem uiInputSystem;
    private       UIButton      trueButton;
    private       UIButton      falseButton;

    // faint spinning earth drawn behind the question panel
    private AbstractEntity backgroundPlanet;

    private FactOrFictionQuestion[] questions;
    private static final int   QUESTIONS_PER_ROUND = 5;
    private static final float FEEDBACK_DURATION   = 1.8f;

    private GameState     gameState;
    // shuffled index list so the round order varies each play
    private List<Integer> questionOrder;
    private int           currentIndex;
    private int           correctCount;
    private boolean       lastAnswerCorrect;
    private float         feedbackTimer;

    private MinigameInstructionPanel instructionPanel;
    private final GlyphLayout layout = new GlyphLayout();


    public FactOrFictionMap(SpriteBatch   batch,
                             IOManager     ioManager,
                             SoundManager  soundManager,
                             EntityManager entityManager,
                             AudioSystem   audioSystem,
                             Runnable      onReturn) {
        super(batch, ioManager, soundManager, entityManager, audioSystem, onReturn);
    }


    @Override
    public void initialize() {

        // sets up viewport, shapeRenderer, background, fonts, resultPanel
        initBase("planets/spaceBackground.png");

        uiManager.addLayer(uiLayer);
        uiInputSystem = new UIInputSystem(ioManager, uiManager);

        setupBackgroundPlanet();

        questions = MinigameDataLoader.getFactOrFiction();

        instructionPanel = new MinigameInstructionPanel(
            batch, shapeRenderer, viewport,
            fonts.title, fonts.header, fonts.body,
            "FACT OR FICTION",
            "How well do you know our Solar System?",
            new String[] {
                "A statement about the Solar System will appear.",
                "Click  TRUE  if you believe it is correct.",
                "Click  FALSE  if you think it is wrong.",
                "",
                QUESTIONS_PER_ROUND + "  questions per round.",
                "",
                "Press  SPACE  or  CLICK  to begin.",
                "Press  ESC  at any time to return."
            }
        );

        gameState = GameState.INSTRUCTIONS;
    }

    // creates a faint spinning Earth entity drawn behind the question panel
    // also carries a SoundEventComponent so clicks can trigger sounds through the engine
    private void setupBackgroundPlanet() {

        entityManager.clear();

        backgroundPlanet = new AbstractEntity() {

            @Override
            public void start() {

                float sw = viewport.getWorldWidth();
                float sh = viewport.getWorldHeight();

                // size the earth at 55% of the screen height and center it
                float size = sh * 0.55f;
                transform = new Transform((sw - size) / 2f, (sh - size) / 2f, size, size);

                AnimationRenderer ar = new AnimationRenderer();
                ar.addAnimation("spin", "planets/earth.png", 30, 8, 0.08f, true);
                // very low alpha so it does not distract from the question text
                ar.setAlpha(0.12f);
                setAnimationRenderer(ar);

                addComponent(SoundEventComponent.class, new SoundEventComponent());
                setTag("factorfiction_bg");
            }

            @Override public void update(float deltaTime) {}
            @Override public void resize(int w, int h) {}
        };

        entityManager.addEntity(backgroundPlanet);
        entityManager.start();
    }

    // resets the score, shuffles question order and loads the first question
    private void startRound() {

        correctCount = 0;
        currentIndex = 0;

        questionOrder = new ArrayList<>();
        for (int i = 0; i < questions.length; i++) questionOrder.add(i);
        Collections.shuffle(questionOrder);

        loadQuestion(0);
        gameState = GameState.PLAYING;
    }

    // removes old buttons and creates fresh TRUE/FALSE buttons for the question at pos
    private void loadQuestion(int pos) {

        if (trueButton  != null) uiLayer.remove(trueButton);
        if (falseButton != null) uiLayer.remove(falseButton);

        float sw   = viewport.getWorldWidth();
        float sh   = viewport.getWorldHeight();
        float btnW = sw * 0.20f;
        float btnH = 60f;
        float gap  = sw * 0.06f;
        float startX = (sw - (btnW * 2 + gap)) / 2f;
        float btnY   = sh * 0.25f;

        trueButton = new UIButton("TRUE", fonts.header);
        trueButton.setSize(btnW, btnH);
        trueButton.setPosition(startX, btnY);
        trueButton.setOnClick(() -> handleAnswer(true));

        falseButton = new UIButton("FALSE", fonts.header);
        falseButton.setSize(btnW, btnH);
        falseButton.setPosition(startX + btnW + gap, btnY);
        falseButton.setOnClick(() -> handleAnswer(false));

        uiLayer.add(trueButton);
        uiLayer.add(falseButton);
    }

    // checks the player's answer, plays a sound, hides buttons and starts the feedback timer
    private void handleAnswer(boolean playerSaidTrue) {

        int     qIdx    = questionOrder.get(currentIndex);
        boolean isTrue  = questions[qIdx].getAnswer().equals("true");

        lastAnswerCorrect = (playerSaidTrue == isTrue);
        if (lastAnswerCorrect) correctCount++;

        SoundEventComponent sfx = backgroundPlanet.getComponent(SoundEventComponent.class);
        if (sfx != null) sfx.request("ui_click");

        // hide buttons so the player cannot click again during feedback
        trueButton.setVisible(false);
        falseButton.setVisible(false);

        feedbackTimer = FEEDBACK_DURATION;
        gameState     = GameState.FEEDBACK;
    }

    // moves to the next question, or switches to the result screen if the round is done
    private void advanceQuestion() {

        currentIndex++;

        if (currentIndex >= QUESTIONS_PER_ROUND) {
            if (trueButton  != null) uiLayer.remove(trueButton);
            if (falseButton != null) uiLayer.remove(falseButton);
            gameState = GameState.RESULT;
        } else {
            loadQuestion(currentIndex);
            gameState = GameState.PLAYING;
        }
    }


    @Override
    public void update(float deltaTime) {

        if (handleCommonUpdate(deltaTime)) return;

        switch (gameState) {

            case INSTRUCTIONS:
                if (ioManager.wasPressed("space") || ioManager.wasPressed("leftClick"))
                    startRound();
                break;

            case PLAYING:
                Vector2 mouse = viewport.unproject(new Vector2(ioManager.getMouseX(), ioManager.getMouseY()));
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


    @Override
    public void render(SpriteBatch batch) {

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);

        switch (gameState) {
            case INSTRUCTIONS:            instructionPanel.render(); break;
            case PLAYING: case FEEDBACK:  renderGame(); break;
            case RESULT:                  resultPanel.render(correctCount, QUESTIONS_PER_ROUND); break;
        }
    }

    // draws the question panel, TRUE/FALSE buttons and the feedback overlay
    private void renderGame() {

        float sw = viewport.getWorldWidth();
        float sh = viewport.getWorldHeight();

        // draw the faint background planet before the panel so it sits underneath
        batch.begin();
        entityManager.renderAll(batch);
        batch.end();

        float pw = sw * 0.65f, ph = sh * 0.35f;
        float px = (sw - pw) / 2f,  py = sh * 0.52f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0.15f, 0.97f);
        shapeRenderer.rect(px, py, pw, ph);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.5f, 0.7f, 1f, 1f);
        shapeRenderer.rect(px, py, pw, ph);
        shapeRenderer.end();

        batch.begin();

        float tx = px + 30f, tw = pw - 60f;

        // question counter in the top-right of the panel
        fonts.stat.setColor(Color.LIGHT_GRAY);
        layout.setText(fonts.stat, "Question  " + (currentIndex + 1) + "  of  " + QUESTIONS_PER_ROUND,
            Color.LIGHT_GRAY, tw, Align.right, false);
        fonts.stat.draw(batch, layout, tx, py + ph - 15f);

        // score in the top-left of the panel
        fonts.stat.setColor(Color.YELLOW);
        layout.setText(fonts.stat, "Score:  " + correctCount);
        fonts.stat.draw(batch, layout, tx, py + ph - 15f);
        fonts.stat.setColor(Color.WHITE);

        // the statement the player has to judge, word-wrapped inside the panel
        int    qIdx      = questionOrder.get(currentIndex);
        String statement = questions[qIdx].getStatement();
        layout.setText(fonts.body, statement, Color.WHITE, tw, Align.center, true);
        fonts.body.draw(batch, layout, tx, py + ph / 2f + layout.height / 2f + 10f);

        batch.end();

        // button area positions — shared between the shape renderer backgrounds and UIManager labels
        float btnW   = sw * 0.20f, btnH = 60f;
        float gap    = sw * 0.06f;
        float startX = (sw - (btnW * 2 + gap)) / 2f;
        float btnY   = sh * 0.25f;

        // during feedback, color buttons green or red to show which answer was correct
        if (gameState == GameState.FEEDBACK) {

            boolean correctIsTrue = questions[questionOrder.get(currentIndex)].getAnswer().equals("true");

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(correctIsTrue ? 0.1f : 0.5f, correctIsTrue ? 0.5f : 0.1f, 0.1f, 1f);
            shapeRenderer.rect(startX, btnY, btnW, btnH);
            shapeRenderer.setColor(!correctIsTrue ? 0.1f : 0.5f, !correctIsTrue ? 0.5f : 0.1f, 0.1f, 1f);
            shapeRenderer.rect(startX + btnW + gap, btnY, btnW, btnH);
            shapeRenderer.end();

        } else {

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.05f, 0.1f, 0.3f, 1f);
            shapeRenderer.rect(startX, btnY, btnW, btnH);
            shapeRenderer.rect(startX + btnW + gap, btnY, btnW, btnH);
            shapeRenderer.end();
        }

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.5f, 0.7f, 1f, 1f);
        shapeRenderer.rect(startX, btnY, btnW, btnH);
        shapeRenderer.rect(startX + btnW + gap, btnY, btnW, btnH);
        shapeRenderer.end();

        batch.begin();
        uiManager.render(batch);

        // verdict and explanation shown during the feedback phase
        if (gameState == GameState.FEEDBACK) {

            int    qIdxF       = questionOrder.get(currentIndex);
            String explanation = questions[qIdxF].getExplanation();
            Color  feedColor   = lastAnswerCorrect ? Color.GREEN : Color.RED;
            String verdict     = lastAnswerCorrect ? "Correct!" : "Wrong!";

            layout.setText(fonts.header, verdict, feedColor, sw * 0.6f, Align.center, false);
            fonts.header.setColor(feedColor);
            fonts.header.draw(batch, layout, (sw - sw * 0.6f) / 2f, sh * 0.18f);
            fonts.header.setColor(Color.WHITE);

            layout.setText(fonts.body, explanation, Color.LIGHT_GRAY, sw * 0.6f, Align.center, true);
            fonts.body.draw(batch, layout, (sw - sw * 0.6f) / 2f, sh * 0.13f);
        }

        // ESC hint at the bottom of the screen
        layout.setText(fonts.body, "ESC  —  Return to Solar System",
            Color.DARK_GRAY, sw, Align.center, false);
        fonts.body.setColor(Color.DARK_GRAY);
        fonts.body.draw(batch, layout, 0, 30f);
        fonts.body.setColor(Color.WHITE);

        batch.end();
    }
}
