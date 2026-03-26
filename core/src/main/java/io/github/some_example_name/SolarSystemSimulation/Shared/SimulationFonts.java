package io.github.some_example_name.SolarSystemSimulation.Shared;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

import io.github.some_example_name.SolarSystemSimulation.ScaleUtil;

// loads and owns the four font sizes used across every simulation world
// call dispose() when the world is done to free the font textures from memory
public class SimulationFonts {

    // sized to match the facts panel style used everywhere in the simulation
    public final BitmapFont title;   // size 46 — panel headings
    public final BitmapFont header;  // size 32 — section labels
    public final BitmapFont body;    // size 26 — regular text
    public final BitmapFont stat;    // size 28 — numbers and smaller labels

    // generates all four fonts from the rajdhani typeface in one pass then disposes the generator
    public SimulationFonts() {

        FreeTypeFontGenerator generator =
            new FreeTypeFontGenerator(Gdx.files.internal("fonts/rajdhani.regular.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter param =
            new FreeTypeFontGenerator.FreeTypeFontParameter();

        param.size = ScaleUtil.fontSize(46); title  = generator.generateFont(param);
        param.size = ScaleUtil.fontSize(32); header = generator.generateFont(param);
        param.size = ScaleUtil.fontSize(26); body   = generator.generateFont(param);
        param.size = ScaleUtil.fontSize(28); stat   = generator.generateFont(param);

        // done generating — free the font file from memory
        generator.dispose();
    }

    // releases all four font textures — call this inside the world's dispose()
    public void dispose() {
        title.dispose();
        header.dispose();
        body.dispose();
        stat.dispose();
    }
}
