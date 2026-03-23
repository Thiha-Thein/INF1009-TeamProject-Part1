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
import io.github.some_example_name.SolarSystemSimulation.MiniGames.MinigameData.FactOrFictionQuestion;

// Fact or Fiction minigame — assigned to Earth in the solar system
// A statement about the solar system is shown and the player picks TRUE or FALSE
// TRUE statements are lifted directly from planets.json facts
// FALSE statements are hand-written plausible inversions of those same facts
// so the game directly reinforces what the player read in the facts panel
// Pressing ESC at any point returns the player to the solar system map
public class FactOrFictionMap implements ISimulation {

    // the four states the game can be in at any time
    private enum GameState { INSTRUCTIONS, PLAYING, FEEDBACK, RESULT }

    private final SpriteBatch batch;
    private final IOManager ioManager;
    private final SoundManager soundManager;
    private final EntityManager entityManager;
    private final AudioSystem audioSystem;
    // called when the player presses ESC to go back to the solar system
    private final Runnable onReturn;

    private Texture background;
    private OrthographicCamera camera;
    private ScreenViewport viewport;
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
    // the two answer buttons shown during each question
    private UIButton trueButton;
    private UIButton falseButton;

    // a faint spinning earth sprite shown in the background behind the question panel
    private AbstractEntity backgroundPlanet;

    // all questions loaded from minigames.json
    private FactOrFictionQuestion[] questions;

    // how many questions are shown per round
    private static final int QUESTIONS_PER_ROUND = 5;
    // how long the correct/wrong feedback is shown before moving to the next question
    private static final float FEEDBACK_DURATION = 1.8f;

    private GameState gameState;
    // shuffled list of question indices so the round order varies each play
    private List<Integer> questionOrder;
    // which question in the round we are currently on
    private int currentIndex;
    // running total of correct answers this round
    private int correctCount;
    // whether the most recent answer was correct — used to color the feedback text
    private boolean lastAnswerCorrect;
    // counts down from FEEDBACK_DURATION before advancing to the next question
    private float feedbackTimer;

    // shared result screen drawn at the end of every minigame
    private GameResultPanel resultPanel;


    // constructor — stores all engine references needed to run the minigame
    public FactOrFictionMap(SpriteBatch batch,
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


    // sets up the camera, fonts, UI, background planet, loads questions and shows the instruction screen
    @Override
    public void initialize() {

        // set up camera and match it to the current window size
        camera = new OrthographicCamera();
        viewport = new ScreenViewport(camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        shapeRenderer = new ShapeRenderer();
        background = new Texture("planets/spaceBackground.png");

        generateFonts();
        setupUI();
        setupBackgroundPlanet();

        // load question data from minigames.json
        questions = MinigameDataLoader.getFactOrFiction();

        resultPanel = new GameResultPanel(
            batch, shapeRenderer, viewport,
            titleFont, headerFont, bodyFont
        );

        // start on the instruction screen so the player knows the rules
        gameState = GameState.INSTRUCTIONS;
    }

    // generates four font sizes from the rajdhani font file matching the facts panel style
    private void generateFonts() {

        FreeTypeFontGenerator generator =
            new FreeTypeFontGenerator(Gdx.files.internal("fonts/rajdhani.regular.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter param =
            new FreeTypeFontGenerator.FreeTypeFontParameter();

        // generate each size in order then dispose the generator to free memory
        param.size = 46; titleFont = generator.generateFont(param);
        param.size = 32; headerFont = generator.generateFont(param);
        param.size = 26; bodyFont = generator.generateFont(param);
        param.size = 28; statFont = generator.generateFont(param);

        generator.dispose();
    }

    // registers the UI layer and creates the input system for detecting button clicks
    private void setupUI() {

        uiManager.addLayer(uiLayer);
        uiInputSystem = new UIInputSystem(ioManager, uiManager);
    }

    // creates a faint spinning Earth entity drawn behind the question panel
    // it also carries a SoundEventComponent so clicks can trigger sounds through the engine
    private void setupBackgroundPlanet() {

        entityManager.clear();

        backgroundPlanet = new AbstractEntity() {

            @Override
            public void start() {

                float sw = Gdx.graphics.getWidth();
                float sh = Gdx.graphics.getHeight();

                // size the earth at 55% of the screen height and center it
                float size = sh * 0.55f;
                transform = new Transform((sw - size) / 2f, (sh - size) / 2f, size, size);

                AnimationRenderer ar = new AnimationRenderer();
                ar.addAnimation("spin", "planets/earth.png", 30, 8, 0.08f, true);
                // very low alpha so it does not distract from the question text
                ar.setAlpha(0.12f);
                setAnimationRenderer(ar);

                // SoundEventComponent lets us request sounds through the AudioSystem
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

        // build a shuffled index list so questions appear in a different order each game
        questionOrder = new ArrayList<>();
        for (int i = 0; i < questions.length; i++) questionOrder.add(i);
        Collections.shuffle(questionOrder);

        loadQuestion(0);
        gameState = GameState.PLAYING;
    }

    // removes the old buttons and adds fresh TRUE/FALSE buttons for the question at pos
    private void loadQuestion(int pos) {

        // remove previous buttons so they do not stack up in the layer
        if (trueButton != null) uiLayer.remove(trueButton);
        if (falseButton != null) uiLayer.remove(falseButton);

        float sw = viewport.getWorldWidth();
        float sh = viewport.getWorldHeight();
        float btnW = sw * 0.20f;
        float btnH = 60f;
        float gap = sw * 0.06f;
        float startX = (sw - (btnW * 2 + gap)) / 2f;
        float btnY = sh * 0.25f;

        // TRUE button — clicking it calls handleAnswer(true)
        trueButton = new UIButton("TRUE", headerFont);
        trueButton.setSize(btnW, btnH);
        trueButton.setPosition(startX, btnY);
        trueButton.setOnClick(() -> handleAnswer(true));

        // FALSE button — clicking it calls handleAnswer(false)
        falseButton = new UIButton("FALSE", headerFont);
        falseButton.setSize(btnW, btnH);
        falseButton.setPosition(startX + btnW + gap, btnY);
        falseButton.setOnClick(() -> handleAnswer(false));

        uiLayer.add(trueButton);
        uiLayer.add(falseButton);
    }

    // checks whether the player's answer matches the correct one, plays a sound,
    // then hides the buttons and starts the feedback timer
    private void handleAnswer(boolean playerSaidTrue) {

        int qIdx = questionOrder.get(currentIndex);
        boolean isTrue = questions[qIdx].getAnswer().equals("true");

        lastAnswerCorrect = (playerSaidTrue == isTrue);
        if (lastAnswerCorrect) correctCount++;

        // request a click sound through the component system — AudioSystem picks it up next frame
        SoundEventComponent sfx = backgroundPlanet.getComponent(SoundEventComponent.class);
        if (sfx != null) sfx.request("ui_click");

        // hide buttons so the player cannot click again during the feedback display
        trueButton.setVisible(false);
        falseButton.setVisible(false);

        feedbackTimer = FEEDBACK_DURATION;
        gameState = GameState.FEEDBACK;
    }

    // moves to the next question, or switches to the result screen if the round is done
    private void advanceQuestion() {

        currentIndex++;

        if (currentIndex >= QUESTIONS_PER_ROUND) {
            // round is finished — clean up buttons and show the result screen
            if (trueButton != null) uiLayer.remove(trueButton);
            if (falseButton != null) uiLayer.remove(falseButton);
            gameState = GameState.RESULT;
        } else {
            // load the next question and resume playing
            loadQuestion(currentIndex);
            gameState = GameState.PLAYING;
        }
    }


    // called every frame — handles ESC, updates the entity lifecycle and drives the state machine
    @Override
    public void update(float deltaTime) {

        // ESC always exits the minigame regardless of which state we are in
        if (ioManager.wasPressed("escape")) { onReturn.run(); return; }

        // advance animation frames and process any pending sound events
        entityManager.updateAll(deltaTime);
        audioSystem.update(entityManager.getEntities());

        switch (gameState) {

            case INSTRUCTIONS:
                // SPACE or a click advances past the instruction screen and starts the round
                if (ioManager.wasPressed("space") || ioManager.wasPressed("leftClick"))
                    startRound();
                break;

            case PLAYING:
                // convert raw mouse pixel position to world coordinates for button hit testing
                Vector2 mouse = viewport.unproject(new Vector2(ioManager.getMouseX(), ioManager.getMouseY()));
                uiInputSystem.update(mouse.x, mouse.y);
                uiManager.update(deltaTime);
                break;

            case FEEDBACK:
                // count down the feedback timer then move on
                feedbackTimer -= deltaTime;
                if (feedbackTimer <= 0f) advanceQuestion();
                break;

            case RESULT:
                // nothing to update on the result screen
                break;
        }
    }


    // called every frame — applies the viewport and delegates drawing to the correct state renderer
    @Override
    public void render(SpriteBatch batch) {

        viewport.apply();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        // draw the faint background planet sprite through the entity manager
        batch.begin();
        entityManager.renderAll(batch);
        batch.end();

        switch (gameState) {
            case INSTRUCTIONS: renderInstructions(); break;
            case PLAYING: case FEEDBACK: renderGame(); break;
            case RESULT: resultPanel.render(correctCount, QUESTIONS_PER_ROUND); break;
        }
    }

    // draws the how-to-play panel shown before the game starts
    private void renderInstructions() {

        float sw = viewport.getWorldWidth();
        float sh = viewport.getWorldHeight();
        float pw = sw * 0.60f, ph = sh * 0.65f;
        float px = (sw - pw) / 2f, py = (sh - ph) / 2f;

        // dark filled background panel
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0.15f, 0.97f);
        shapeRenderer.rect(px, py, pw, ph);
        shapeRenderer.end();

        // blue-white border around the panel
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.5f, 0.7f, 1f, 1f);
        shapeRenderer.rect(px, py, pw, ph);
        shapeRenderer.end();

        batch.begin();

        float tx = px + 40f, tw = pw - 80f;
        float ty = py + ph - 40f;

        // game title at the top of the panel in cyan
        titleFont.setColor(Color.CYAN);
        layout.setText(titleFont, "FACT OR FICTION", Color.CYAN, tw, Align.center, false);
        titleFont.draw(batch, layout, tx, ty);
        titleFont.setColor(Color.WHITE);
        ty -= layout.height + 20f;

        // subtitle line describing the game
        layout.setText(headerFont, "How well do you know our Solar System?",
            Color.WHITE, tw, Align.center, false);
        headerFont.draw(batch, layout, tx, ty);
        ty -= layout.height + 35f;

        // instruction lines — empty strings add a small gap between sections
        String[] lines = {
            "A statement about the Solar System will appear.",
            "Click  TRUE  if you believe it is correct.",
            "Click  FALSE  if you think it is wrong.",
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

    // draws the question panel, the TRUE/FALSE buttons, and the feedback overlay
    private void renderGame() {

        float sw = viewport.getWorldWidth();
        float sh = viewport.getWorldHeight();

        // question panel dimensions and position
        float pw = sw * 0.65f, ph = sh * 0.35f;
        float px = (sw - pw) / 2f, py = sh * 0.52f;

        // filled panel background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0.15f, 0.97f);
        shapeRenderer.rect(px, py, pw, ph);
        shapeRenderer.end();

        // panel border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.5f, 0.7f, 1f, 1f);
        shapeRenderer.rect(px, py, pw, ph);
        shapeRenderer.end();

        batch.begin();

        float tx = px + 30f, tw = pw - 60f;

        // question counter shown in the top right of the panel
        String counter = "Question  " + (currentIndex + 1) + "  of  " + QUESTIONS_PER_ROUND;
        statFont.setColor(Color.LIGHT_GRAY);
        layout.setText(statFont, counter, Color.LIGHT_GRAY, tw, Align.right, false);
        statFont.draw(batch, layout, tx, py + ph - 15f);

        // score shown in the top left of the panel
        statFont.setColor(Color.YELLOW);
        layout.setText(statFont, "Score:  " + correctCount);
        statFont.draw(batch, layout, tx, py + ph - 15f);
        statFont.setColor(Color.WHITE);

        // the statement the player has to judge — word-wrapped inside the panel
        int qIdx = questionOrder.get(currentIndex);
        String statement = questions[qIdx].getStatement();

        layout.setText(bodyFont, statement, Color.WHITE, tw, Align.center, true);
        bodyFont.draw(batch, layout,
            tx, py + ph / 2f + layout.height / 2f + 10f);

        batch.end();

        // button background positions
        float btnW = sw * 0.20f, btnH = 60f;
        float gap = sw * 0.06f;
        float startX = (sw - (btnW * 2 + gap)) / 2f;
        float btnY = sh * 0.25f;

        // during FEEDBACK, color buttons green or red to show which answer was correct
        if (gameState == GameState.FEEDBACK) {

            boolean correctIsTrue = questions[questionOrder.get(currentIndex)].getAnswer().equals("true");

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            // TRUE button is green if the correct answer is true, red otherwise
            shapeRenderer.setColor(correctIsTrue ? 0.1f : 0.5f, correctIsTrue ? 0.5f : 0.1f, 0.1f, 1f);
            shapeRenderer.rect(startX, btnY, btnW, btnH);
            // FALSE button is the opposite color
            shapeRenderer.setColor(!correctIsTrue ? 0.1f : 0.5f,
                                   !correctIsTrue ? 0.5f : 0.1f, 0.1f, 1f);
            shapeRenderer.rect(startX + btnW + gap, btnY, btnW, btnH);
            shapeRenderer.end();

        } else {

            // standard dark button background during normal play
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.05f, 0.1f, 0.3f, 1f);
            shapeRenderer.rect(startX, btnY, btnW, btnH);
            shapeRenderer.rect(startX + btnW + gap, btnY, btnW, btnH);
            shapeRenderer.end();
        }

        // button borders
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.5f, 0.7f, 1f, 1f);
        shapeRenderer.rect(startX, btnY, btnW, btnH);
        shapeRenderer.rect(startX + btnW + gap, btnY, btnW, btnH);
        shapeRenderer.end();

        // UIManager draws the TRUE/FALSE text labels on top of the shape renderer backgrounds
        batch.begin();
        uiManager.render(batch);

        // draw the verdict and explanation during the feedback phase
        if (gameState == GameState.FEEDBACK) {

            int qIdxF = questionOrder.get(currentIndex);
            String explanation = questions[qIdxF].getExplanation();
            Color feedColor = lastAnswerCorrect ? Color.GREEN : Color.RED;
            String verdict = lastAnswerCorrect ? "Correct!" : "Wrong!";

            // verdict text — green for correct, red for wrong
            layout.setText(headerFont, verdict, feedColor, sw * 0.6f, Align.center, false);
            headerFont.setColor(feedColor);
            headerFont.draw(batch, layout, (sw - sw * 0.6f) / 2f, sh * 0.18f);
            headerFont.setColor(Color.WHITE);

            // explanation shown below the verdict
            layout.setText(bodyFont, explanation, Color.LIGHT_GRAY,
                sw * 0.6f, Align.center, true);
            bodyFont.draw(batch, layout, (sw - sw * 0.6f) / 2f, sh * 0.13f);
        }

        // ESC hint shown at the very bottom of the screen
        layout.setText(bodyFont, "ESC  —  Return to Solar System",
            Color.DARK_GRAY, sw, Align.center, false);
        bodyFont.setColor(Color.DARK_GRAY);
        bodyFont.draw(batch, layout, 0, 30f);
        bodyFont.setColor(Color.WHITE);

        batch.end();
    }

    // called when the window is resized — updates the viewport to match the new size
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

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
