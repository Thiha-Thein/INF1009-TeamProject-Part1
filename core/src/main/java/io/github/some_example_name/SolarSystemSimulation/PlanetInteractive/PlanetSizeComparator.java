package io.github.some_example_name.SolarSystemSimulation.PlanetInteractive;

import io.github.some_example_name.SolarSystemSimulation.PlanetObj;

public class PlanetSizeComparator {

    // real relative diameters (Earth = 1.0)
    private static final String[] NAMES = {
        "Sun", "Mercury", "Venus", "Earth", "Mars",
        "Jupiter", "Saturn", "Uranus", "Neptune"
    };

    private static final float[] TRUE_DIAMETERS = {
        109.0f,  // Sun
        0.38f, // Mercury
        0.95f, // Venus
        1.00f, // Earth
        0.53f, // Mars
        11.21f, // Jupiter
        9.45f, // Saturn
        4.01f, // Uranus
        3.88f, // Neptune
    };

    // larger planet fills this fraction of screen height
    private static final float MAX_DISPLAY_FRACTION = 0.30f;

    // smaller planet never goes below this so it stays visible
    private static final float MIN_DISPLAY_FRACTION = 0.06f;

    // sun sprite is textured smaller than planets so we compensate visually
    private static final float SUN_SPRITE_COMPENSATION = 1.6f;

    // log base used when sun is involved — softens the 109x gap so planets stay readable
    private static final float LOG_BASE = 4.0f;


    // looks up true diameter by planet name, falls back to 1.0 if unknown
    public static float getTrueDiameter(String name) {
        for (int i = 0; i < NAMES.length; i++) {
            if (NAMES[i].equalsIgnoreCase(name))
                return TRUE_DIAMETERS[i];
        }
        return 1.0f;
    }

    // applies log compression when the size gap is extreme (i.e. sun is involved)
    private float compressDiameter(float diameter, boolean sunInvolved) {
        if (!sunInvolved)
            return diameter;
        // log compression keeps relative differences visible without destroying small planets
        return (float)(Math.log(diameter + 1) / Math.log(LOG_BASE));
    }

    // returns { selectedH, compareH } — larger anchors to MAX, smaller scales down from there
    // returns { selectedH, compareH } — larger anchors to MAX, smaller scales down from there
    public float[] getDisplayHeights(PlanetObj selected, PlanetObj compare, float screenHeight) {

        if (selected == null || compare == null)
            return new float[]{ screenHeight * MAX_DISPLAY_FRACTION,
                screenHeight * MAX_DISPLAY_FRACTION };

        float selDiam = getTrueDiameter(selected.getPlanetName());
        float cmpDiam = getTrueDiameter(compare.getPlanetName());

        // check if sun is either party — triggers log compression
        boolean sunInvolved = "Sun".equalsIgnoreCase(selected.getPlanetName())
            || "Sun".equalsIgnoreCase(compare.getPlanetName());

        float selCompressed = compressDiameter(selDiam, sunInvolved);
        float cmpCompressed = compressDiameter(cmpDiam, sunInvolved);

        float maxCompressed = Math.max(selCompressed, cmpCompressed);
        float minCompressed = Math.min(selCompressed, cmpCompressed);

        float maxDisplay = screenHeight * MAX_DISPLAY_FRACTION;
        float minDisplay = screenHeight * MIN_DISPLAY_FRACTION;

        // scale so the bigger compressed value fills maxDisplay
        float scale = maxDisplay / maxCompressed;

        float largerH  = maxDisplay;
        float smallerH = Math.max(minCompressed * scale, minDisplay);

        // assign back to selected vs compare
        float selectedH = (selCompressed >= cmpCompressed) ? largerH : smallerH;
        float compareH  = (cmpCompressed >= selCompressed) ? largerH : smallerH;

        // when sun is selected, shrink the compare planet a bit more so the size gap reads clearly
        if ("Sun".equalsIgnoreCase(selected.getPlanetName()))
            compareH *= 0.55f;

        // compensate for the sun sprite being textured smaller than other planet sprites
        if ("Sun".equalsIgnoreCase(selected.getPlanetName()))
            selectedH *= SUN_SPRITE_COMPENSATION;

        if ("Sun".equalsIgnoreCase(compare.getPlanetName()))
            compareH *= SUN_SPRITE_COMPENSATION;

        return new float[]{ selectedH, compareH };
    }


    // legacy single ratio, kept for compatibility
    public float getScale(PlanetObj selected, PlanetObj compare) {

        if (selected == null || compare == null)
            return 1f;

        float selDiam = getTrueDiameter(selected.getPlanetName());
        float cmpDiam = getTrueDiameter(compare.getPlanetName());

        if (selDiam == 0)
            return 1f;

        float ratio = cmpDiam / selDiam;
        return Math.max(0.06f, Math.min(ratio, 109f / 0.38f));
    }
}
