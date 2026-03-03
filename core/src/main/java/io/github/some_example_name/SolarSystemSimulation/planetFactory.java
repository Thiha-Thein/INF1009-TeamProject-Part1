package io.github.some_example_name.SolarSystemSimulation;

import com.badlogic.gdx.Gdx;
import io.github.some_example_name.AbstractEngine.EntityManagement.AbstractEntity;

public class planetFactory {

    // visual tilt settings
    private static final float TILT_BASE = -20f;
    private static final float TILT_STEP = 2.5f;

    // how oval the orbit looks
    private static final float ELLIPSE_RATIO = 0.4f;

    // number of planets in system
    private static final int TOTAL_PLANETS = 8;

    public static PlanetObj create(String name,
                                   float mass,
                                   float size,
                                   String spritePath,
                                   AbstractEntity parent,
                                   int orbitIndex,
                                   float startAngle) {

        PlanetObj planet = new PlanetObj(name, mass, size, spritePath, parent);

        // Sun has no orbit
        if (parent == null)
            return planet;

        //orbit math

        // Sun size
        float sunRadius = parent.getTransform().getWidth() / 2f;

        // Screen usable radius
        float screenWidth = Gdx.graphics.getWidth();

        // keep planets inside screen edges
        float maxOrbitRadius = (screenWidth / 2f) - 80f;

        // extra breathing room near sun
        float innerGap = 100f;   // ← adjust this value

        float availableSpace = maxOrbitRadius - (sunRadius + innerGap);

        float spacing = availableSpace / TOTAL_PLANETS;

        // push all orbits outward by innerGap
        float radiusX = sunRadius + innerGap + spacing * (orbitIndex + 0.5f);

        // vertical squash (ellipse look)
        float radiusY = radiusX * ELLIPSE_RATIO;

        // Approximates Kepler's Third Law: T² ∝ a³
        // where T = orbital period, a = semi-major axis
        //Outer planets orbit slower — speed decreases as orbitIndex increases
        float speed = 1.2f / (orbitIndex + 1);

        // small tilt difference
        float tilt = TILT_BASE + orbitIndex * TILT_STEP;

        planet.setOrbit(radiusX, radiusY, speed, tilt, startAngle);

        return planet;
    }
}
