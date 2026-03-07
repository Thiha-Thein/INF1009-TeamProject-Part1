package io.github.some_example_name.SolarSystemSimulation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.google.gson.Gson;

import io.github.some_example_name.AbstractEngine.EntityManagement.AbstractEntity;
import io.github.some_example_name.SolarSystemSimulation.PlanetData.PlanetData;
import io.github.some_example_name.SolarSystemSimulation.PlanetData.PlanetDataList;

import java.util.HashMap;
import java.util.Map;

// Factory that constructs PlanetObj instances with correct orbital parameters and data
// Centralises orbit spacing math so SolarSystemMap only needs to pass a name and orbit index
public class PlanetFactory {

    // All planets are tilted by the same base angle plus a small per-orbit step
    // to create a layered, staggered look rather than flat coplanar orbits
    private static final float TILT_BASE  = -20f; // degrees
    private static final float TILT_STEP  = 2.5f; // degrees added per orbit index outward

    // Y-radius = X-radius * ELLIPSE_RATIO — lower values make orbits look more overhead/isometric
    private static final float ELLIPSE_RATIO = 0.4f;

    // Used when distributing orbit spacing evenly across the visible screen area
    private static final int TOTAL_PLANETS = 8;

    // Planet JSON data is loaded once and cached — avoids re-reading the file every time create() is called
    private static Map<String, PlanetData> planetDataMap;

    // Lazy-loads planet fact data from planets.json the first time a planet is created
    private static void ensureDataLoaded() {

        if (planetDataMap != null) return; // already loaded — skip

        String json = Gdx.files.internal("planets.json").readString();

        PlanetDataList dataList =
            new Gson().fromJson(json, PlanetDataList.class);

        planetDataMap = new HashMap<>();

        for (PlanetData data : dataList.getPlanets()) {
            planetDataMap.put(data.getName(), data);
        }
    }

    // Creates a PlanetObj with automatically calculated orbit parameters based on orbit index
    // Passing null as parent creates a stationary body (used for the Sun)
    public static PlanetObj create(String name,
                                   float mass,
                                   float size,
                                   String spritePath,
                                   AbstractEntity parent,
                                   int orbitIndex,
                                   float startAngle) {

        ensureDataLoaded();

        PlanetData data = planetDataMap.get(name);

        PlanetObj planet =
            new PlanetObj(name, mass, size, spritePath, parent, data);

        // Stationary body (no parent = no orbit) — return early without setting orbit parameters
        if (parent == null) return planet;

        float sunRadius = parent.getTransform().getWidth() / 2f;

        float screenWidth = Gdx.graphics.getWidth();

        // Leave an 80-unit margin on each side so the outermost planet does not clip the edge
        float maxOrbitRadius = (screenWidth / 2f) - 80f;

        float innerGap = 100f; // minimum gap between the sun surface and the first planet's orbit

        float availableSpace = maxOrbitRadius - (sunRadius + innerGap);

        // Divide the available radial space evenly across all planets
        float spacing = availableSpace / TOTAL_PLANETS;

        // Place each planet at the center of its allocated slot (+ 0.5f) so they are spread evenly
        float radiusX = sunRadius + innerGap + spacing * (orbitIndex + 0.5f);

        float radiusY = radiusX * ELLIPSE_RATIO;

        // Outer planets orbit more slowly — approximates Kepler's third law in a simplified form
        float speed = 0.1f / (orbitIndex + 1);

        float tilt = TILT_BASE + orbitIndex * TILT_STEP;

        planet.setOrbit(radiusX, radiusY, speed, tilt, startAngle);

        return planet;
    }
}
