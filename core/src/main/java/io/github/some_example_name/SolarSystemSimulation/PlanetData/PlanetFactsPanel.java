package io.github.some_example_name.SolarSystemSimulation.PlanetData;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Map;

// Draws the right-side information panel shown when a planet is selected
// Renders three sections: About (description paragraph), Key Stats (key-value table), and Fun Facts (card list)
// Fonts are injected by SolarSystemMap rather than created here to keep font lifecycle centralised
public class PlanetFactsPanel {

    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final Viewport viewport;

    private final BitmapFont titleFont;
    private final BitmapFont headerFont;
    private final BitmapFont bodyFont;
    private final BitmapFont statFont; // slightly larger than bodyFont to make stat values stand out

    // Reusable layout object for measuring and wrapping text — avoids allocating a new one per frame
    private final GlyphLayout layout = new GlyphLayout();

    // Space between the panel edge and its text content
    private static final float PADDING = 40f;

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

    // Renders the full panel — silently skips if no data is provided (e.g. planet has no JSON entry)
    public void render(String planetName, PlanetDataComponent data) {

        if (data == null) return;

        float screenWidth = viewport.getWorldWidth();
        float screenHeight = viewport.getWorldHeight();

        // Panel occupies the right ~42% of screen width
        float panelWidth = screenWidth * 0.42f;
        float panelX = screenWidth - panelWidth - PADDING;
        float panelHeight = screenHeight - PADDING * 2;
        float panelY = screenHeight - PADDING; // LibGDX Y is bottom-up so this is the top of the panel

        // Draw dark blue panel background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0.15f, 0.95f);
        shapeRenderer.rect(panelX, panelY - panelHeight, panelWidth, panelHeight);
        shapeRenderer.end();

        // Draw a light blue border around the panel
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.5f, 0.7f, 1f, 1f);
        shapeRenderer.rect(panelX, panelY - panelHeight, panelWidth, panelHeight);
        shapeRenderer.end();

        batch.begin();

        float textX = panelX + PADDING;
        float textY = panelY - PADDING;

        titleFont.draw(batch, planetName, textX, textY);

        textY -= 60f;

        textY = renderAbout(data, panelWidth, textX, textY);

        textY -= 30f;

        textY = renderStats(data, textX, textY);

        textY -= 40f;

        renderFacts(data, panelWidth, panelX, textX, textY);

        // Keyboard hint at the bottom of the panel so users know how to navigate comparison mode
        renderControls(panelX, panelWidth, panelY - panelHeight);

        batch.end();
    }

    // Draws the "About This Planet" section — description is word-wrapped to fit the panel width
    private float renderAbout(PlanetDataComponent data,
                              float panelWidth,
                              float textX,
                              float textY) {

        headerFont.draw(batch, "About This Planet", textX, textY);

        textY -= 45f;

        // Wrap the description to the available text area width
        layout.setText(
            bodyFont,
            data.getDescription(),
            Color.WHITE,
            panelWidth - PADDING * 2,
            Align.left,
            true
        );

        bodyFont.draw(batch, layout, textX, textY);

        textY -= layout.height + 30f;

        return textY;
    }

    // Draws the "Key Stats" section — stat label on the left, value next to it measured to avoid overlap
    private float renderStats(PlanetDataComponent data,
                              float textX,
                              float textY) {

        headerFont.draw(batch, "Key Stats", textX, textY);

        textY -= 45f;

        Map<String, String> stats = data.getStats();

        if (stats != null) {

            for (Map.Entry<String, String> stat : stats.entrySet()) {

                String label = stat.getKey() + ":";

                bodyFont.draw(batch, label, textX, textY);

                // Measure the label so the value starts immediately to the right of the colon
                layout.setText(bodyFont, label);

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

    // Draws the "Fun Facts" section — each fact rendered inside a subtle card background
    private void renderFacts(PlanetDataComponent data,
                             float panelWidth,
                             float panelX,
                             float textX,
                             float textY) {

        headerFont.draw(batch, "Fun Facts", textX, textY);

        textY -= 50f;

        for (PlanetData.Fact fact : data.getFacts()) {

            // Measure wrapped fact text before drawing the card background so the card height fits the content
            layout.setText(
                bodyFont,
                fact.getText(),
                Color.WHITE,
                panelWidth - PADDING * 2,
                Align.left,
                true
            );

            float cardHeight = layout.height + 60f;

            batch.end(); // end batch to switch to ShapeRenderer for the card background

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.08f, 0.08f, 0.18f, 1f);

            shapeRenderer.rect(
                panelX + PADDING - 10,
                textY - cardHeight + 10,
                panelWidth - PADDING * 2 + 20,
                cardHeight
            );

            shapeRenderer.end();

            batch.begin(); // resume batch to draw text on top of the card

            // Fact title in yellow to distinguish it from the body text
            headerFont.setColor(Color.YELLOW);
            headerFont.draw(batch, fact.getTitle(), textX, textY);
            headerFont.setColor(Color.CYAN); // reset to cyan for subsequent headers

            textY -= 35f;

            bodyFont.draw(batch, layout, textX, textY);

            textY -= layout.height + 50f;
        }
    }

    // Draws keyboard shortcut hints at the bottom of the panel to guide users through comparison mode
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
