package io.github.some_example_name.SolarSystemSimulation.PlanetData;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.some_example_name.SolarSystemSimulation.*;

import java.util.ArrayList;
import java.util.List;

// Renders a row of planet names across the top of the screen and detects clicks on them
// Clicking a name triggers presentation mode the same way clicking the planet sprite does
// This class intentionally does not use the generic UIManager — it manages its own layout to stay
// tightly coupled with the planet list ordering
public class PlanetNameBar {

    private final SpriteBatch batch;
    private final Viewport viewport;

    private BitmapFont nameFont;

    // Parallel lists — each index corresponds to one planet in orderedPlanets
    private final List<GlyphLayout> nameLayouts = new ArrayList<>();   // pre-measured text for efficient rendering
    private final List<Float> namePositionsX = new ArrayList<>();      // centered X position within each slot
    private final List<PlanetObj> orderedPlanets = new ArrayList<>();  // the planet list this bar represents

    private float nameBarY; // Y position of the name bar — recalculated on resize

    public PlanetNameBar(SpriteBatch batch, Viewport viewport) {
        this.batch = batch;
        this.viewport = viewport;
    }

    // Initializes the bar with the planet list and generates the font — must be called after the GL context exists
    public void initialize(List<PlanetObj> planets) {

        orderedPlanets.clear();
        nameLayouts.clear();
        namePositionsX.clear();

        orderedPlanets.addAll(planets);

        FreeTypeFontGenerator generator =
            new FreeTypeFontGenerator(Gdx.files.internal("fonts/star_crush.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter param =
            new FreeTypeFontGenerator.FreeTypeFontParameter();

        param.size = 50;
        param.color = Color.WHITE;

        nameFont = generator.generateFont(param);
        generator.dispose();

        rebuildLayout();
    }

    // Recalculates text positions to fit the current viewport width — called on init and on every resize
    public void rebuildLayout() {

        nameLayouts.clear();
        namePositionsX.clear();

        float worldWidth = viewport.getWorldWidth();

        // Divide screen into equal horizontal slots, one per planet
        float slotWidth = worldWidth / orderedPlanets.size();

        nameBarY = viewport.getWorldHeight() - 50f; // a small margin from the top edge

        for (int i = 0; i < orderedPlanets.size(); i++) {

            PlanetObj planet = orderedPlanets.get(i);

            GlyphLayout layout = new GlyphLayout(nameFont, planet.getPlanetName());
            nameLayouts.add(layout);

            // Center the name within its allocated slot
            float slotCenterX = slotWidth * i + slotWidth / 2f;
            namePositionsX.add(slotCenterX - layout.width / 2f);
        }
    }

    // Called by SolarSystemMap on resize — recalculates positions for the new viewport dimensions
    public void resize() {
        rebuildLayout();
    }

    // Returns the planet whose name was clicked, or null if the click missed all names
    public PlanetObj getClickedPlanet(Vector2 mouse) {

        for (int i = 0; i < orderedPlanets.size(); i++) {

            if (isInsideName(mouse, namePositionsX.get(i), nameBarY, nameLayouts.get(i))) {
                return orderedPlanets.get(i);
            }
        }

        return null;
    }

    // Renders all planet names at their pre-calculated positions
    public void render() {
        batch.begin();
        for (int i = 0; i < orderedPlanets.size(); i++) {
            nameFont.draw(batch, nameLayouts.get(i), namePositionsX.get(i), nameBarY);
        }
        batch.end();
    }

    // AABB hit test on the text bounding box — Y range accounts for the font's descender below the baseline
    private boolean isInsideName(Vector2 mouse, float x, float y, GlyphLayout layout) {

        return mouse.x >= x && mouse.x <= x + layout.width &&
            mouse.y >= y - layout.height && mouse.y <= y;
    }

    public void dispose() {

        if (nameFont != null)
            nameFont.dispose();
    }
}
