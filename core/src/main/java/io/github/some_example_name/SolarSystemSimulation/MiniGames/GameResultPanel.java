package io.github.some_example_name.SolarSystemSimulation.MiniGames;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.some_example_name.AbstractEngine.UIManagement.UIElement;

// Draws the result screen shown at the end of every minigame.
//
// Now extends UIElement so it is managed by UIManager → UILayer and rendered
// through the standard UI pipeline. Callers should:
//   1. Add this panel to a UILayer once (e.g. in initBase / initialize()).
//   2. Call setResults(correct, total) when the round ends.
//   3. Call setVisible(true) to show the panel; setVisible(false) to hide it.
//
// The batch passed to render() is already open. This class closes it only around
// ShapeRenderer calls and re-opens it immediately after, exactly as before.
public class GameResultPanel extends UIElement {

    // ShapeRenderer is needed for background/divider shapes — injected via constructor
    // because UIElement.render() only receives SpriteBatch.
    private final ShapeRenderer shapeRenderer;
    private final Viewport      viewport;

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

    // Result state set by the minigame when the round ends
    private int correct;
    private int total;

    public GameResultPanel(ShapeRenderer shapeRenderer,
                           Viewport viewport,
                           BitmapFont titleFont,
                           BitmapFont headerFont,
                           BitmapFont bodyFont) {
        this.shapeRenderer = shapeRenderer;
        this.viewport      = viewport;
        this.titleFont     = titleFont;
        this.headerFont    = headerFont;
        this.bodyFont      = bodyFont;
    }

    // Call this when the round ends to supply the score before making the panel visible.
    public void setResults(int correct, int total) {
        this.correct = correct;
        this.total   = total;
    }

    // Called by UILayer — batch is already open.
    // ShapeRenderer sections temporarily close and re-open the batch as needed.
    @Override
    public void render(SpriteBatch batch) {
        if (!visible) return;

        float sw = viewport.getWorldWidth();
        float sh = viewport.getWorldHeight();

        float panelW = sw * PANEL_W_FRAC;
        float panelH = sh * PANEL_H_FRAC;

        float panelX = (sw - panelW) / 2f;
        float panelY = (sh - panelH) / 2f;

        // draw the dark blue filled background rectangle
        batch.end();
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

        float textX = panelX + PADDING;
        float textY = panelY + panelH - PADDING;
        float textW = panelW - PADDING * 2f;

        // draw the "RESULTS" title in cyan at the top of the panel
        titleFont.setColor(Color.CYAN);
        layout.setText(titleFont, "RESULTS", Color.CYAN, textW, Align.center, false);
        titleFont.draw(batch, layout, textX, textY);
        titleFont.setColor(Color.WHITE);

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
        headerFont.setColor(Color.WHITE);

        textY -= layout.height + 50f;

        // draw a horizontal divider between score and instructions
        batch.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.3f, 0.4f, 0.6f, 1f);
        shapeRenderer.line(panelX + PADDING, textY, panelX + panelW - PADDING, textY);
        shapeRenderer.end();
        batch.begin();

        textY -= 35f;

        // draw the instruction telling the player to press ESC to go back
        layout.setText(bodyFont, "Press  ESC  to return to the Solar System",
            Color.LIGHT_GRAY, textW, Align.center, false);
        bodyFont.draw(batch, layout, textX, textY);
        // batch is left open — UILayer will close it after all elements are drawn
    }

    // returns an encouraging message that matches how well the player scored
    private String buildMessage(int correct, int total) {

        float ratio = (float) correct / total;

        if (ratio >= 1f)    return "Perfect score! You are a true space explorer!";
        if (ratio >= 0.8f)  return "Excellent work! The solar system holds no secrets from you.";
        if (ratio >= 0.6f)  return "Great effort! Keep exploring to learn more.";
        if (ratio >= 0.4f)  return "Good try! Head back and read the planet facts again.";
        return "Keep going! Every explorer starts somewhere.";
    }
}
