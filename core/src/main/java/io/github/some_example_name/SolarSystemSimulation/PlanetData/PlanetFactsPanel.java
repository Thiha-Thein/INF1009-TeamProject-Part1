package io.github.some_example_name.SolarSystemSimulation.PlanetData;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.HashMap;
import java.util.Map;

import io.github.some_example_name.SolarSystemSimulation.ScaleUtil;

/*
    PlanetFactsPanel

    All spacing is multiplied by ScaleUtil.get() so the panel looks correct
    at any resolution. Fun Facts rendering stops before reaching the Controls
    zone so content never overlaps the keyboard hints.
*/
public class PlanetFactsPanel {

    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final Viewport viewport;

    private final BitmapFont titleFont;
    private final BitmapFont headerFont;
    private final BitmapFont bodyFont;
    private final BitmapFont statFont;

    private final GlyphLayout layout = new GlyphLayout();

    private Map<String, Runnable> gameCallbacks = new HashMap<>();

    public PlanetFactsPanel(SpriteBatch batch,
                            ShapeRenderer shapeRenderer,
                            Viewport viewport,
                            BitmapFont titleFont,
                            BitmapFont headerFont,
                            BitmapFont bodyFont,
                            BitmapFont statFont) {
        this.batch = batch;
        this.shapeRenderer = shapeRenderer;
        this.viewport = viewport;
        this.titleFont = titleFont;
        this.headerFont = headerFont;
        this.bodyFont = bodyFont;
        this.statFont = statFont;
    }

    public void setGameCallbacks(Map<String, Runnable> callbacks) {
        this.gameCallbacks = callbacks;
    }

    public void render(String planetName, PlanetDataComponent data) {

        if (data == null) return;

        float s = ScaleUtil.get();

        float screenWidth  = viewport.getWorldWidth();
        float screenHeight = viewport.getWorldHeight();

        float padding      = 40f * s;
        float panelWidth   = screenWidth * 0.42f;
        float panelX       = screenWidth - panelWidth - padding;
        float panelH       = screenHeight - padding * 2f;
        float panelY       = screenHeight - padding;
        float panelBottomY = panelY - panelH;

        // Height of the fixed controls block at the bottom
        float controlsBlockH = 32f * s + 30f * s * 3f + 10f * s + padding;
        // Height of the optional PLAY GAME button above controls
        float buttonBlockH = gameCallbacks.containsKey(planetName)
            ? 55f * s + 20f * s
            : 0f;
        // Scrolling content must not go below this Y
        float contentFloor = panelBottomY + controlsBlockH + buttonBlockH + padding;

        // Panel background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0.15f, 0.95f);
        shapeRenderer.rect(panelX, panelBottomY, panelWidth, panelH);
        shapeRenderer.end();

        // Panel border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.5f, 0.7f, 1f, 1f);
        shapeRenderer.rect(panelX, panelBottomY, panelWidth, panelH);
        shapeRenderer.end();

        batch.begin();

        float textX = panelX + padding;
        float textY = panelY - padding;

        titleFont.draw(batch, planetName, textX, textY);
        textY -= 60f * s;

        textY = renderAbout(data, panelWidth, padding, textX, textY, s);
        textY -= 30f * s;

        textY = renderStats(data, textX, textY, s);
        textY -= 40f * s;

        renderFacts(data, panelWidth, padding, panelX, textX, textY, contentFloor, s);

        renderControls(panelX, panelBottomY, padding, s);
        renderPlayGameButton(planetName, panelX, panelWidth, panelBottomY, padding, s);

        batch.end();
    }

    private float renderAbout(PlanetDataComponent data,
                              float panelWidth, float padding,
                              float textX, float textY, float s) {

        headerFont.draw(batch, "About This Planet", textX, textY);
        textY -= 45f * s;

        layout.setText(bodyFont, data.getDescription(), Color.WHITE,
            panelWidth - padding * 2f, Align.left, true);
        bodyFont.draw(batch, layout, textX, textY);

        return textY - layout.height - 30f * s;
    }

    private float renderStats(PlanetDataComponent data,
                              float textX, float textY, float s) {

        headerFont.draw(batch, "Key Stats", textX, textY);
        textY -= 45f * s;

        Map<String, String> stats = data.getStats();
        if (stats != null) {
            for (Map.Entry<String, String> stat : stats.entrySet()) {
                String label = stat.getKey() + ":";
                bodyFont.draw(batch, label, textX, textY);
                layout.setText(bodyFont, label);
                statFont.draw(batch, stat.getValue(), textX + layout.width + 10f * s, textY);
                textY -= 40f * s;
            }
        }

        return textY;
    }

    private void renderFacts(PlanetDataComponent data,
                             float panelWidth, float padding,
                             float panelX, float textX,
                             float textY, float contentFloor, float s) {

        headerFont.draw(batch, "Fun Facts", textX, textY);
        textY -= 50f * s;

        for (PlanetData.Fact fact : data.getFacts()) {

            layout.setText(bodyFont, fact.getText(), Color.WHITE,
                panelWidth - padding * 2f, Align.left, true);

            float cardHeight = layout.height + 60f * s;

            // Stop before overlapping the controls
            if (textY - cardHeight < contentFloor) break;

            batch.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.08f, 0.08f, 0.18f, 1f);
            shapeRenderer.rect(
                panelX + padding - 10f * s,
                textY - cardHeight + 10f * s,
                panelWidth - padding * 2f + 20f * s,
                cardHeight
            );
            shapeRenderer.end();

            batch.begin();

            headerFont.setColor(Color.YELLOW);
            headerFont.draw(batch, fact.getTitle(), textX, textY);
            headerFont.setColor(Color.CYAN);
            textY -= 35f * s;

            bodyFont.draw(batch, layout, textX, textY);
            textY -= layout.height + 50f * s;
        }
    }

    // Controls anchored to the panel bottom — never overlaps content
    private void renderControls(float panelX, float panelBottomY, float padding, float s) {

        float textX = panelX + padding;
        float textY = panelBottomY + padding + 30f * s * 3f + 10f * s;

        headerFont.draw(batch, "Controls", textX, textY);
        textY -= 32f * s;

        bodyFont.draw(batch, "A : Scroll Left Planet",       textX, textY); textY -= 30f * s;
        bodyFont.draw(batch, "D : Scroll Right Planet",      textX, textY); textY -= 30f * s;
        bodyFont.draw(batch, "ESC : Return to Solar System", textX, textY);
    }

    // PLAY GAME button anchored just above the controls block
    private void renderPlayGameButton(String planetName,
                                      float panelX, float panelWidth,
                                      float panelBottomY, float padding, float s) {

        if (!gameCallbacks.containsKey(planetName)) return;

        float controlsBlockH = 32f * s + 30f * s * 3f + 10f * s + padding;
        float btnHeight = 55f * s;
        float btnWidth  = panelWidth - padding * 2f;
        float btnX      = panelX + padding;
        float btnY      = panelBottomY + controlsBlockH + 10f * s;

        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.5f, 0.2f, 1f);
        shapeRenderer.rect(btnX, btnY, btnWidth, btnHeight);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.3f, 1f, 0.4f, 1f);
        shapeRenderer.rect(btnX, btnY, btnWidth, btnHeight);
        shapeRenderer.end();

        batch.begin();

        GlyphLayout btnLayout = new GlyphLayout(headerFont, "PLAY GAME");
        headerFont.setColor(Color.WHITE);
        headerFont.draw(batch, btnLayout,
            btnX + (btnWidth  - btnLayout.width)  / 2f,
            btnY + (btnHeight + btnLayout.height) / 2f);
        headerFont.setColor(Color.CYAN);
    }

    // Click detection must mirror renderPlayGameButton geometry exactly
    public boolean checkPlayGameClick(String planetName, float mouseX, float mouseY, boolean wasClicked) {

        if (!gameCallbacks.containsKey(planetName)) return false;
        if (!wasClicked) return false;

        float s           = ScaleUtil.get();
        float screenWidth = viewport.getWorldWidth();
        float padding     = 40f * s;

        float panelWidth   = screenWidth * 0.42f;
        float panelX       = screenWidth - panelWidth - padding;
        float panelBottomY = padding;

        float controlsBlockH = 32f * s + 30f * s * 3f + 10f * s + padding;
        float btnHeight = 55f * s;
        float btnWidth  = panelWidth - padding * 2f;
        float btnX      = panelX + padding;
        float btnY      = panelBottomY + controlsBlockH + 10f * s;

        if (mouseX >= btnX && mouseX <= btnX + btnWidth &&
            mouseY >= btnY && mouseY <= btnY + btnHeight) {
            gameCallbacks.get(planetName).run();
            return true;
        }

        return false;
    }
}
