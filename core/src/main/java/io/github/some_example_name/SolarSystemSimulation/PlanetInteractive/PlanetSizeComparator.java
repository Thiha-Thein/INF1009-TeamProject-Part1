package io.github.some_example_name.SolarSystemSimulation.PlanetInteractive;

import io.github.some_example_name.SolarSystemSimulation.PlanetObj;

// Calculates display heights for two planets in size comparison mode
// Uses real relative diameters (Earth = 1.0) with log compression when the Sun is involved
// to keep comparisons visually readable despite the 109x size gap between the Sun and Earth
public class PlanetSizeComparator {

    // Real relative diameters with Earth = 1.0 as the baseline
    private static final String[] NAMES = {
        "Sun", "Mercury", "Venus", "Earth", "Mars",
        "Jupiter", "Saturn", "Uranus", "Neptune"
    };

    private static final float[] TRUE_DIAMETERS = {
        109.0f,  // Sun
        0.38f,   // Mercury
        0.95f,   // Venus
        1.00f,   // Earth
        0.53f,   // Mars
        11.21f,  // Jupiter
        9.45f,   // Saturn
        4.01f,   // Uranus
        3.88f,   // Neptune
    };

    // The larger planet fills this fraction of screen height in comparison mode
    private static final float MAX_DISPLAY_FRACTION = 0.30f;

    // The smaller planet never drops below this fraction so it always remains visible
    private static final float MIN_DISPLAY_FRACTION = 0.06f;

    // The Sun's sprite is textured to appear smaller than its actual planet data size — this multiplier compensates
    private static final float SUN_SPRITE_COMPENSATION = 1.6f;

    // Log base used when the Sun is in a comparison — softens the extreme 109x ratio so smaller planets are still readable
    private static final float LOG_BASE = 4.0f;


    // Returns the true diameter for a planet by name — falls back to 1.0 (Earth-sized) for unknown names
    public static float getTrueDiameter(String name) {
        for (int i = 0; i < NAMES.length; i++) {
            if (NAMES[i].equalsIgnoreCase(name))
                return TRUE_DIAMETERS[i];
        }
        return 1.0f;
    }

    // Applies logarithmic compression to the diameter when the Sun is one of the compared planets
    // Without this, Earth-sized planets would render below the minimum visible size
    private float compressDiameter(float diameter, boolean sunInvolved) {
        if (!sunInvolved)
            return diameter;
        return (float)(Math.log(diameter + 1) / Math.log(LOG_BASE));
    }

    // Returns { selectedDisplayHeight, compareDisplayHeight } in screen pixels
    // The larger body anchors to MAX_DISPLAY_FRACTION of screen height; the smaller scales proportionally from there
    public float[] getDisplayHeights(PlanetObj selected, PlanetObj compare, float screenHeight) {

        if (selected == null || compare == null)
            return new float[]{ screenHeight * MAX_DISPLAY_FRACTION,
                screenHeight * MAX_DISPLAY_FRACTION };

        float selDiam = getTrueDiameter(selected.getPlanetName());
        float cmpDiam = getTrueDiameter(compare.getPlanetName());

        // Sun-involved comparisons use log compression to prevent extreme size ratios
        boolean sunInvolved = "Sun".equalsIgnoreCase(selected.getPlanetName())
            || "Sun".equalsIgnoreCase(compare.getPlanetName());

        float selCompressed = compressDiameter(selDiam, sunInvolved);
        float cmpCompressed = compressDiameter(cmpDiam, sunInvolved);

        float maxCompressed = Math.max(selCompressed, cmpCompressed);
        float minCompressed = Math.min(selCompressed, cmpCompressed);

        float maxDisplay = screenHeight * MAX_DISPLAY_FRACTION;
        float minDisplay = screenHeight * MIN_DISPLAY_FRACTION;

        // Scale factor maps the largest compressed value to maxDisplay
        float scale = maxDisplay / maxCompressed;

        float largerH  = maxDisplay;
        float smallerH = Math.max(minCompressed * scale, minDisplay); // floor at minimum so it stays visible

        // Assign heights back to the correct planet
        float selectedH = (selCompressed >= cmpCompressed) ? largerH : smallerH;
        float compareH  = (cmpCompressed >= selCompressed) ? largerH : smallerH;

        // When the Sun is selected, shrink the compare planet further to make the scale difference more apparent
        if ("Sun".equalsIgnoreCase(selected.getPlanetName()))
            compareH *= 0.55f;

        // Compensate for the Sun sprite being textured smaller than its true proportional size
        if ("Sun".equalsIgnoreCase(selected.getPlanetName()))
            selectedH *= SUN_SPRITE_COMPENSATION;

        if ("Sun".equalsIgnoreCase(compare.getPlanetName()))
            compareH *= SUN_SPRITE_COMPENSATION;

        return new float[]{ selectedH, compareH };
    }


    // Legacy single-ratio method — kept for any callers that request a scale factor instead of display heights
    public float getScale(PlanetObj selected, PlanetObj compare) {

        if (selected == null || compare == null)
            return 1f;

        float selDiam = getTrueDiameter(selected.getPlanetName());
        float cmpDiam = getTrueDiameter(compare.getPlanetName());

        if (selDiam == 0)
            return 1f;

        float ratio = cmpDiam / selDiam;

        // Clamp to the actual Sun-to-Mercury range so the scale never exceeds what the screen can show
        return Math.max(0.06f, Math.min(ratio, 109f / 0.38f));
    }
}
