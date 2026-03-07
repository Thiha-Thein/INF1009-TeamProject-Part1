package io.github.some_example_name.SolarSystemSimulation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.google.gson.Gson;

import io.github.some_example_name.AbstractEngine.EntityManagement.AbstractEntity;
import io.github.some_example_name.SolarSystemSimulation.PlanetData.PlanetData;
import io.github.some_example_name.SolarSystemSimulation.PlanetData.PlanetDataList;

import java.util.HashMap;
import java.util.Map;

public class PlanetFactory {

    // Visual tilt settings
    private static final float TILT_BASE  = -20f;
    private static final float TILT_STEP  = 2.5f;

    // How oval the orbit looks
    private static final float ELLIPSE_RATIO = 0.4f;

    // Number of planets in system
    private static final int TOTAL_PLANETS = 8;

    // Cached planet facts loaded from JSON
    private static Map<String, PlanetData> planetDataMap;

    // Loads planet data if it hasn't already been loaded
    private static void ensureDataLoaded() {

        if (planetDataMap != null) return;

        String json = Gdx.files.internal("planets.json").readString();

        PlanetDataList dataList =
            new Gson().fromJson(json, PlanetDataList.class);

        planetDataMap = new HashMap<>();

        for (PlanetData data : dataList.getPlanets()) {

            planetDataMap.put(data.getName(), data);
        }
    }

    public static PlanetObj create(String name,
                                   float mass,
                                   float size,
                                   String spritePath,
                                   AbstractEntity parent,
                                   int orbitIndex,
                                   float startAngle) {

        // Ensure JSON data is loaded before using it
        ensureDataLoaded();

        PlanetData data = planetDataMap.get(name);

        PlanetObj planet =
            new PlanetObj(name, mass, size, spritePath, parent, data);

        if (parent == null) return planet;

        float sunRadius = parent.getTransform().getWidth() / 2f;

        float screenWidth = Gdx.graphics.getWidth();

        float maxOrbitRadius = (screenWidth / 2f) - 80f;

        float innerGap = 100f;

        float availableSpace = maxOrbitRadius - (sunRadius + innerGap);

        float spacing = availableSpace / TOTAL_PLANETS;

        float radiusX = sunRadius + innerGap + spacing * (orbitIndex + 0.5f);

        float radiusY = radiusX * ELLIPSE_RATIO;

        float speed = 0.1f / (orbitIndex + 1);

        float tilt = TILT_BASE + orbitIndex * TILT_STEP;

        planet.setOrbit(radiusX, radiusY, speed, tilt, startAngle);

        return planet;
    }
}
