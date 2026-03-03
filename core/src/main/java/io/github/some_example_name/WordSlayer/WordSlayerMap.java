package io.github.some_example_name.WordSlayer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

import java.util.Arrays;
import java.util.List;

import io.github.some_example_name.AbstractEngine.AIManagement.*;
import io.github.some_example_name.AbstractEngine.AudioManagement.*;
import io.github.some_example_name.AbstractEngine.CollisionManagement.*;
import io.github.some_example_name.AbstractEngine.EntityManagement.*;
import io.github.some_example_name.AbstractEngine.IOManagement.*;
import io.github.some_example_name.AbstractEngine.MovementManagement.*;
import io.github.some_example_name.AbstractEngine.ScreenManagement.*;
import io.github.some_example_name.WordSlayer.characterObjs.*;
import io.github.some_example_name.WordSlayer.spawnMechanics.*;
import io.github.some_example_name.WordSlayer.wordMechanics.*;

public class WordSlayerMap implements ISimulation {

    private final EntityManager entityManager;
    private final MovementManager movementManager;
    private final CollisionManager collisionManager;
    private final SoundManager soundManager;
    private final IOManager ioManager;
    private final AIManager aiManager;
    private WaveManager waveManager;
    private SentenceManager sentenceManager;

    private Texture background;
    private Viewport viewport;

    private PlayerObj player;


    private boolean levelComplete = false;
    private BitmapFont sentenceFont, enemyWordFont;
    private GlyphLayout layout;

    private final List<String> wrongWords =
        Arrays.asList("dog", "run", "tree", "blue", "jump", "fast");

    public WordSlayerMap(EntityManager entityManager,
                         MovementManager movementManager,
                         CollisionManager collisionManager,
                         SoundManager soundManager,
                         IOManager ioManager,
                         AIManager aiManager) {

        this.entityManager = entityManager;
        this.movementManager = movementManager;
        this.collisionManager = collisionManager;
        this.soundManager = soundManager;
        this.ioManager = ioManager;
        this.aiManager = aiManager;
    }

    @Override
    public void initialize() {

        entityManager.clear();
        viewport = new ScreenViewport();

        background = new Texture("planets/spaceBackground.png");

        player = new PlayerObj(viewport);
        entityManager.addEntity(player);

        // create sentence manager FIRST
        sentenceManager = new SentenceManager();
        // now load sentence
        sentenceManager.loadSentence("The cat ___ on the ___.", Arrays.asList("sat", "mat"));

        waveManager = new WaveManager();
        waveManager.setMapBounds(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // font setup
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/star_crush.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();

        // big font for sentence
        param.size = 100;
        sentenceFont = generator.generateFont(param);

        // smaller font for enemies
        param.size = 40;
        enemyWordFont = generator.generateFont(param);

        generator.dispose();

        layout = new GlyphLayout();

        entityManager.start();
    }

    @Override
    public void update(float deltaTime) {

        if (levelComplete) return;

        viewport.apply();

        handleWaveFlow();
    }

    private void handleWaveFlow() {

        int wave = waveManager.getCurrentWave();
        
        // only start wave if none active
        if (!waveManager.isWaveActive() && wave < 4) {
            List<SpawnData> spawns = waveManager.startWave();
            wave = waveManager.getCurrentWave();
            List<String> correctWords = wave <= 3 ? sentenceManager.getMissingWords() : null;
            int correctIndex = 0;
            for (SpawnData data : spawns) {
                AbstractEnemy enemy = createEnemy(data.getEnemyType());
                enemy.setInitialPosition(data.getPosition().x, data.getPosition().y);
                enemy.setTarget(player);
                enemy.setSentenceManager(sentenceManager);
                if (wave <= 3) {
                    String word;
                    if (correctIndex < correctWords.size()) {
                        word = correctWords.get(correctIndex);
                        correctIndex++;
                    } else {
                        word = wrongWords.get((int)(Math.random() * wrongWords.size()));
                    }
                    enemy.addComponent(WordComponent.class, new WordComponent(word));
                }
                entityManager.addEntity(enemy);
            }
            entityManager.start();
            return;
        }
        // sentence waves
        if (wave <= 3 && waveManager.isWaveActive()) {
            if (sentenceManager.isSentenceComplete()) {
                waveManager.endWave();
            }
        }
        // boss wave
        if (wave == 4 && waveManager.isWaveActive()) {
            if (!areEnemiesAlive()) {
                levelComplete = true;
                waveManager.endWave();
            }
        }
    }

    private AbstractEnemy createEnemy(EnemyType type) {
        switch (type) {
            case BAT:
                return new BatEnemy();
            case GOBLIN:
                return new GoblinEnemy();
            case MUSHROOM:
                return new MushroomEnemy();
            case SKELETON:
                return new SkeletonEnemy();
            case BOSS:
                return new EvilWizardBoss();
            default:
                return new BatEnemy();
        }
    }

    private boolean areEnemiesAlive() {

        for (AbstractEntity entity : entityManager.getEntities()) {
            if (!entity.isActive()) continue;
            if ("enemy".equals(entity.getTag())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.begin();
        entityManager.renderAll(batch);

        // sentence at top
        String sentence = sentenceManager.getDisplaySentence();
        layout.setText(sentenceFont, sentence);
        float sx = (viewport.getWorldWidth() - layout.width) / 2f;
        float sy = viewport.getWorldHeight() - 40f;
        sentenceFont.draw(batch, layout, sx, sy);
        // words above enemies
        for (AbstractEntity entity : entityManager.getEntities()) {
            if (!"enemy".equals(entity.getTag())) continue;
            WordComponent wc = entity.getComponent(WordComponent.class);
            if (wc == null) continue;
            String word = wc.getWord();
            layout.setText(enemyWordFont, word);
            float x = entity.getTransform().getX() + entity.getTransform().getWidth() / 2f - layout.width / 2f;
            float y = entity.getTransform().getY() + entity.getTransform().getHeight() + 70f;
            enemyWordFont.draw(batch, layout, x, y);
        }
        batch.end();
    }

    @Override
    public void resize(int width, int height) {

        if (viewport != null)
            viewport.update(width, height, true);

        collisionManager.setWorldBounds(width, height);

        if (waveManager != null)
            waveManager.setMapBounds(width, height);
    }

    @Override
    public Texture getBackground() {
        return background;
    }

    @Override
    public void dispose() {

        entityManager.clear();
        if (enemyWordFont != null) enemyWordFont.dispose();
        if (sentenceFont != null) sentenceFont.dispose();
        if (background != null) {
            background.dispose();
            background = null;
        }
    }
}
