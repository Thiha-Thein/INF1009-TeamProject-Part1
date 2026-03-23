package io.github.some_example_name.SolarSystemSimulation;

import com.badlogic.gdx.Gdx;

public class ScaleUtil {
    private static final float REFERENCE_WIDTH = 2560f;
    private static final float MIN_SCALE       = 0.40f;

    //Raw scale factor for the current screen width.
    public static float get() {
        return Math.max(Gdx.graphics.getWidth() / REFERENCE_WIDTH, MIN_SCALE);
    }

    //Scale a font size (designed at the reference resolution). Always returns at least 8 so text stays legible.
    public static int fontSize(int referenceSize) {
        return Math.max(Math.round(referenceSize * get()), 8);
    }

    //Scale a pixel measurement (padding, button height, spacing, etc.) designed at the reference resolution.
    public static float px(float referencePixels) {
        return referencePixels * get();
    }
}
