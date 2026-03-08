package io.github.some_example_name.SolarSystemSimulation.MiniGames;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

// draws the result screen shown at the end of every minigame
// takes a score and total, then draws a centered panel with a message and an ESC instruction
public class GameResultPanel {

    // rendering tools passed in from the parent map, not created here
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final ScreenViewport viewport;

    // three font sizes used for different lines of text on the panel
    private final BitmapFont titleFont;   // size 46 — used for the "RESULTS" heading
    private final BitmapFont headerFont;  // size 32 — used for the score and message
    private final BitmapFont bodyFont;    // size 26 — used for the ESC instruction

    // reused each frame to measure text width and height without creating new objects
    private final GlyphLayout layout = new GlyphLayout();

    // spacing inside the panel edges
    private static final float PADDING = 40f;
    // panel takes up 55% of the screen width
    private static final float PANEL_W_FRAC = 0.55f;
    // panel takes up 55% of the screen height
    private static final float PANEL_H_FRAC = 0.55f;

    // constructor — stores all the rendering tools and fonts passed in from the minigame
    public GameResultPanel(SpriteBatch batch,
                           ShapeRenderer shapeRenderer,
                           ScreenViewport viewport,
                           BitmapFont titleFont,
                           BitmapFont headerFont,
                           BitmapFont bodyFont) {

        this.batch = batch;
        this.shapeRenderer = shapeRenderer;
        this.viewport = viewport;
        this.titleFont = titleFont;
        this.headerFont = headerFont;
        this.bodyFont = bodyFont;
    }

    // draws the full result panel every frame
    // correct — how many questions the player got right
    // total   — how many questions were in the round
    public void render(int correct, int total) {

        // get the current screen size from the viewport
        float sw = viewport.getWorldWidth();
        float sh = viewport.getWorldHeight();

        // calculate the panel size as a fraction of the screen
        float panelW = sw * PANEL_W_FRAC;
        float panelH = sh * PANEL_H_FRAC;

        // position the panel so it sits in the center of the screen
        float panelX = (sw - panelW) / 2f;
        float panelY = (sh - panelH) / 2f;

        // draw the dark blue filled background rectangle
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0.15f, 0.97f);
        shapeRenderer.rect(panelX, panelY, panelW, panelH);
        shapeRenderer.end();

        // draw the blue-white border around the panel
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.5f, 0.7f, 1f, 1f);
        shapeRenderer.rect(panelX, panelY, panelW, panelH);
        shapeRenderer.end();

        batch.begin();

        // starting x and y positions for text, inset by padding
        float textX = panelX + PADDING;
        float textY = panelY + panelH - PADDING;
        float textW = panelW - PADDING * 2f;

        // draw the "RESULTS" title in cyan at the top of the panel
        titleFont.setColor(Color.CYAN);
        layout.setText(titleFont, "RESULTS", Color.CYAN, textW, Align.center, false);
        titleFont.draw(batch, layout, textX, textY);
        // reset font color back to white after drawing
        titleFont.setColor(Color.WHITE);

        // move y down by the height of the title plus a gap
        textY -= layout.height + 30f;

        // draw the score line showing how many questions were correct
        String scoreLine = "You scored  " + correct + "  out of  " + total;
        layout.setText(headerFont, scoreLine, Color.WHITE, textW, Align.center, false);
        headerFont.draw(batch, layout, textX, textY);

        textY -= layout.height + 25f;

        // pick and draw a message based on how well the player did
        String message = buildMessage(correct, total);
        layout.setText(headerFont, message, Color.YELLOW, textW, Align.center, true);
        headerFont.setColor(Color.YELLOW);
        headerFont.draw(batch, layout, textX, textY);
        // reset font color back to white after drawing
        headerFont.setColor(Color.WHITE);

        textY -= layout.height + 50f;

        // end the batch so ShapeRenderer can draw the divider line
        batch.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.3f, 0.4f, 0.6f, 1f);
        // draw a horizontal line across the panel to separate score from instructions
        shapeRenderer.line(panelX + PADDING, textY, panelX + panelW - PADDING, textY);
        shapeRenderer.end();
        batch.begin();

        textY -= 35f;

        // draw the instruction telling the player to press ESC to go back
        layout.setText(bodyFont, "Press  ESC  to return to the Solar System",
            Color.LIGHT_GRAY, textW, Align.center, false);
        bodyFont.draw(batch, layout, textX, textY);

        batch.end();
    }

    // returns an encouraging message that matches how well the player scored
    private String buildMessage(int correct, int total) {

        // calculate the score as a value between 0.0 and 1.0
        float ratio = (float) correct / total;

        if (ratio >= 1f) return "Perfect score! You are a true space explorer!";
        if (ratio >= 0.8f) return "Excellent work! The solar system holds no secrets from you.";
        if (ratio >= 0.6f) return "Great effort! Keep exploring to learn more.";
        if (ratio >= 0.4f) return "Good try! Head back and read the planet facts again.";
        return "Keep going! Every explorer starts somewhere.";
    }
}
