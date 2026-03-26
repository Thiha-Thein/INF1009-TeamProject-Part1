package io.github.some_example_name.SolarSystemSimulation.MiniGames;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;

// Draws the how-to-play panel shown before a minigame round starts.
//
// This panel was copy-pasted — pixel-for-pixel — into renderInstructions() in
// all three minigame classes. The only things that differed between them were:
//   - the title string  (e.g. "FACT OR FICTION" vs "ORDER THE PLANETS")
//   - the subtitle string
//   - the array of instruction lines
//   - the panel height fraction (0.65 vs 0.70)
//
// Extracting this class removes ~55 lines of duplicated rendering code from
// each map and satisfies SRP: the minigame class owns game logic, this class
// owns instruction-screen rendering.
//
// Usage:
//   private MinigameInstructionPanel instructionPanel;
//
//   // in initialize(), after initBase():
//   instructionPanel = new MinigameInstructionPanel(
//       batch, shapeRenderer, viewport,
//       fonts.title, fonts.header, fonts.body,
//       "FACT OR FICTION",
//       "How well do you know our Solar System?",
//       new String[] { "line 1", "line 2", "", "line 3" }
//   );
//
//   // in render(), INSTRUCTIONS case:
//   instructionPanel.render();
public class MinigameInstructionPanel {

    // rendering tools passed in from the parent map — not created here
    private final SpriteBatch    batch;
    private final ShapeRenderer  shapeRenderer;
    private final Viewport       viewport;

    private final BitmapFont titleFont;
    private final BitmapFont headerFont;
    private final BitmapFont bodyFont;

    // content that varies per minigame
    private final String   title;
    private final String   subtitle;
    private final String[] lines;

    // panel height as a fraction of the screen — OrderThePlanets needs 0.70
    // because it has more instructions; the other two use 0.65
    private final float panelHeightFraction;

    // reused each frame to avoid allocations
    private final GlyphLayout layout = new GlyphLayout();

    // standard constructor — uses 0.65 panel height fraction (suitable for
    // FactOrFiction and MatchThePlanet)
    public MinigameInstructionPanel(SpriteBatch   batch,
                                     ShapeRenderer shapeRenderer,
                                     Viewport      viewport,
                                     BitmapFont    titleFont,
                                     BitmapFont    headerFont,
                                     BitmapFont    bodyFont,
                                     String        title,
                                     String        subtitle,
                                     String[]      lines) {
        this(batch, shapeRenderer, viewport,
             titleFont, headerFont, bodyFont,
             title, subtitle, lines,
             0.65f);
    }

    // full constructor — pass a custom panelHeightFraction when the default
    // 0.65 does not give enough vertical space for the instruction lines
    public MinigameInstructionPanel(SpriteBatch   batch,
                                     ShapeRenderer shapeRenderer,
                                     Viewport      viewport,
                                     BitmapFont    titleFont,
                                     BitmapFont    headerFont,
                                     BitmapFont    bodyFont,
                                     String        title,
                                     String        subtitle,
                                     String[]      lines,
                                     float         panelHeightFraction) {
        this.batch               = batch;
        this.shapeRenderer       = shapeRenderer;
        this.viewport            = viewport;
        this.titleFont           = titleFont;
        this.headerFont          = headerFont;
        this.bodyFont            = bodyFont;
        this.title               = title;
        this.subtitle            = subtitle;
        this.lines               = lines;
        this.panelHeightFraction = panelHeightFraction;
    }

    // draws the full instruction panel — call this every frame while in the INSTRUCTIONS state
    public void render() {

        float sw = viewport.getWorldWidth();
        float sh = viewport.getWorldHeight();
        float pw = sw * 0.60f;
        float ph = sh * panelHeightFraction;
        float px = (sw - pw) / 2f;
        float py = (sh - ph) / 2f;

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

        float tx = px + 40f;
        float tw = pw - 80f;
        float ty = py + ph - 40f;

        // game title in cyan
        titleFont.setColor(Color.CYAN);
        layout.setText(titleFont, title, Color.CYAN, tw, Align.center, false);
        titleFont.draw(batch, layout, tx, ty);
        titleFont.setColor(Color.WHITE);
        ty -= layout.height + 20f;

        // subtitle
        layout.setText(headerFont, subtitle, Color.WHITE, tw, Align.center, false);
        headerFont.draw(batch, layout, tx, ty);
        ty -= layout.height + 35f;

        // instruction lines — an empty string inserts a small vertical gap
        for (String line : lines) {
            if (line.isEmpty()) { ty -= 14f; continue; }
            layout.setText(bodyFont, line, Color.LIGHT_GRAY, tw, Align.center, false);
            bodyFont.draw(batch, layout, tx, ty);
            ty -= layout.height + 11f;
        }

        batch.end();
    }
}
