package io.github.some_example_name.SolarSystemSimulation.PlanetData;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Map;

/*
    PlanetFactsPanel

    Draws the UI panel that displays educational information
    about the currently selected planet.
*/

public class PlanetFactsPanel {

    // Rendering utilities provided externally
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final Viewport viewport;

    // Fonts are injected instead of created here
    private final BitmapFont titleFont;
    private final BitmapFont headerFont;
    private final BitmapFont bodyFont;
    private final BitmapFont statFont;

    // Used to measure and wrap long text
    private final GlyphLayout layout = new GlyphLayout();

    // Padding inside the information panel
    private static final float PADDING = 40f;

    // Constructor now receives fonts instead of generating them
    public PlanetFactsPanel(SpriteBatch batch,
                            ShapeRenderer shapeRenderer,
                            Viewport viewport,
                            BitmapFont titleFont,
                            BitmapFont headerFont,
                            BitmapFont bodyFont,
                            BitmapFont statFont) {

        // Store renderer references
        this.batch = batch;
        this.shapeRenderer = shapeRenderer;
        this.viewport = viewport;

        // Store fonts provided by the caller
        this.titleFont = titleFont;
        this.headerFont = headerFont;
        this.bodyFont = bodyFont;
        this.statFont = statFont;
    }

    // Draws the entire planet information panel
    public void render(String planetName, PlanetDataComponent data) {

        // Do nothing if no planet data is provided
        if (data == null) return;

        float screenWidth = viewport.getWorldWidth();
        float screenHeight = viewport.getWorldHeight();

        // Panel width takes up roughly 40% of screen
        float panelWidth = screenWidth * 0.42f;

        // Panel positioned on the right side
        float panelX = screenWidth - panelWidth - PADDING;

        float panelHeight = screenHeight - PADDING * 2;
        float panelY = screenHeight - PADDING;

        // Draw panel background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0.15f, 0.95f);
        shapeRenderer.rect(panelX, panelY - panelHeight, panelWidth, panelHeight);
        shapeRenderer.end();

        // Draw panel border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.5f, 0.7f, 1f, 1f);
        shapeRenderer.rect(panelX, panelY - panelHeight, panelWidth, panelHeight);
        shapeRenderer.end();

        batch.begin();

        float textX = panelX + PADDING;
        float textY = panelY - PADDING;

        // Draw planet name
        titleFont.draw(batch, planetName, textX, textY);

        textY -= 60f;

        // Render description section
        textY = renderAbout(data, panelWidth, textX, textY);

        textY -= 30f;

        // Render statistics section
        textY = renderStats(data, textX, textY);

        textY -= 40f;

        // Render fun facts section
        renderFacts(data, panelWidth, panelX, textX, textY);

        // Draw keyboard control instructions at the bottom of the panel
        renderControls(panelX, panelWidth, panelY - panelHeight);

        batch.end();
    }

    // Draws the planet description paragraph
    private float renderAbout(PlanetDataComponent data,
                              float panelWidth,
                              float textX,
                              float textY) {

        // Draw section header
        headerFont.draw(batch, "About This Planet", textX, textY);

        textY -= 45f;

        // Prepare wrapped paragraph text
        layout.setText(
            bodyFont,
            data.getDescription(),
            Color.WHITE,
            panelWidth - PADDING * 2,
            Align.left,
            true
        );

        // Draw description text
        bodyFont.draw(batch, layout, textX, textY);

        textY -= layout.height + 30f;

        return textY;
    }

    // Draws the list of planet statistics
    private float renderStats(PlanetDataComponent data,
                              float textX,
                              float textY) {

        // Draw section header
        headerFont.draw(batch, "Key Stats", textX, textY);

        textY -= 45f;

        Map<String, String> stats = data.getStats();

        if (stats != null) {

            for (Map.Entry<String, String> stat : stats.entrySet()) {

                // Draw stat label
                String label = stat.getKey() + ":";

                bodyFont.draw(batch, label, textX, textY);

                // Measure label width so value can be aligned next to it
                layout.setText(bodyFont, label);

                // Draw stat value
                statFont.draw(
                    batch,
                    stat.getValue(),
                    textX + layout.width + 10f,
                    textY
                );

                textY -= 40f;
            }
        }

        return textY;
    }

    // Draws fact cards containing interesting information
    private void renderFacts(PlanetDataComponent data,
                             float panelWidth,
                             float panelX,
                             float textX,
                             float textY) {

        // Draw section header
        headerFont.draw(batch, "Fun Facts", textX, textY);

        textY -= 50f;

        for (PlanetData.Fact fact : data.getFacts()) {

            // Measure wrapped fact paragraph
            layout.setText(
                bodyFont,
                fact.getText(),
                Color.WHITE,
                panelWidth - PADDING * 2,
                Align.left,
                true
            );

            float cardHeight = layout.height + 60f;

            batch.end();

            // Draw card background
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.08f, 0.08f, 0.18f, 1f);

            shapeRenderer.rect(
                panelX + PADDING - 10,
                textY - cardHeight + 10,
                panelWidth - PADDING * 2 + 20,
                cardHeight
            );

            shapeRenderer.end();

            batch.begin();

            // Draw fact title
            headerFont.setColor(Color.YELLOW);
            headerFont.draw(batch, fact.getTitle(), textX, textY);
            headerFont.setColor(Color.CYAN);

            textY -= 35f;

            // Draw fact text
            bodyFont.draw(batch, layout, textX, textY);

            textY -= layout.height + 50f;
        }
    }

    // Draw keyboard instructions at the bottom of the panel
    private void renderControls(float panelX, float panelWidth, float panelBottomY) {

        float textX = panelX + PADDING;
        float textY = panelBottomY + 150f;

        headerFont.draw(batch, "Controls", textX, textY);

        textY -= 40f;

        bodyFont.draw(batch, "A : Scroll Left Planet", textX, textY);
        textY -= 30f;

        bodyFont.draw(batch, "D : Scroll Right Planet", textX, textY);
        textY -= 30f;

        bodyFont.draw(batch, "ESC : Return to Solar System", textX, textY);
    }
}
