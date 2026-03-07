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

/*
 * PlanetNameBar
 *
 * Responsible for rendering planet names across the top of the screen
 * and detecting which planet name was clicked.
 *
 * This UI element belongs to the Solar System simulation layer and
 * intentionally does not use the generic UIManager system.
 *
 * Responsibilities:
 *
 * 1. Render planet names evenly across the top of the screen
 * 2. Detect which planet name is clicked
 * 3. Return the selected PlanetObj to SolarSystemMap
 */
public class PlanetNameBar {

    private final SpriteBatch batch;
    private final Viewport viewport;

    private BitmapFont nameFont;

    // Layout data for each planet name
    private final List<GlyphLayout> nameLayouts = new ArrayList<>();
    private final List<Float> namePositionsX = new ArrayList<>();
    private final List<PlanetObj> orderedPlanets = new ArrayList<>();

    // Y position of the name bar
    private float nameBarY;

    public PlanetNameBar(SpriteBatch batch, Viewport viewport) {
        this.batch = batch;
        this.viewport = viewport;
    }

    /*
     * Initializes the name bar.
     * Called once during SolarSystemMap.initialize().
     */
    public void initialize(List<PlanetObj> planets) {

        orderedPlanets.clear();
        nameLayouts.clear();
        namePositionsX.clear();

        orderedPlanets.addAll(planets);

        // Generate font used for planet names
        FreeTypeFontGenerator generator =
            new FreeTypeFontGenerator(Gdx.files.internal("fonts/star_crush.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter param =
            new FreeTypeFontGenerator.FreeTypeFontParameter();

        param.size = 50;
        param.color = Color.WHITE;

        nameFont = generator.generateFont(param);
        generator.dispose();

        // Build initial layout
        rebuildLayout();
    }

    //Rebuilds name layout positions based on viewport size.
    //This ensures correct alignment when the window is resized.
    public void rebuildLayout() {

        nameLayouts.clear();
        namePositionsX.clear();

        float worldWidth = viewport.getWorldWidth();

        // Divide screen into equal slots for each planet
        float slotWidth = worldWidth / orderedPlanets.size();

        nameBarY = viewport.getWorldHeight() - 50f;

        for (int i = 0; i < orderedPlanets.size(); i++) {

            PlanetObj planet = orderedPlanets.get(i);

            GlyphLayout layout = new GlyphLayout(nameFont, planet.getPlanetName());
            nameLayouts.add(layout);

            // Centre each planet name within its slot
            float slotCenterX = slotWidth * i + slotWidth / 2f;
            namePositionsX.add(slotCenterX - layout.width / 2f);
        }
    }

    /*
     * Called by SolarSystemMap when the screen resizes.
     * Recalculates layout positions.
     */
    public void resize() {
        rebuildLayout();
    }

    //Returns the clicked planet or null if no name was clicked.
    public PlanetObj getClickedPlanet(Vector2 mouse) {

        for (int i = 0; i < orderedPlanets.size(); i++) {

            if (isInsideName(mouse, namePositionsX.get(i), nameBarY, nameLayouts.get(i))) {
                return orderedPlanets.get(i);
            }
        }

        return null;
    }

    //Renders all planet names evenly spaced across the top.
    public void render() {
        batch.begin();
        for (int i = 0; i < orderedPlanets.size(); i++) {
            nameFont.draw(batch, nameLayouts.get(i), namePositionsX.get(i), nameBarY);
        }
        batch.end();
    }

    //Hit test for detecting clicks on planet names.
    private boolean isInsideName(Vector2 mouse, float x, float y, GlyphLayout layout) {

        return mouse.x >= x && mouse.x <= x + layout.width &&
            mouse.y >= y - layout.height && mouse.y <= y;
    }

    public void dispose() {

        if (nameFont != null)
            nameFont.dispose();
    }
}
