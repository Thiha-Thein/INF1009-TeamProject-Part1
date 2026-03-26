package io.github.some_example_name.SolarSystemSimulation.Shared;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// single source of truth for planet sprite sheet paths
// previously duplicated inside MatchThePlanetMap and OrderThePlanetsMap
// adding or renaming a planet asset only requires changing this one file
public final class PlanetAssets {

    // maps every planet name (including the Sun) to its sprite sheet path
    public static final Map<String, String> SPRITE_PATHS;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("Sun",     "planets/sun.png");
        map.put("Mercury", "planets/mercury.png");
        map.put("Venus",   "planets/venus.png");
        map.put("Earth",   "planets/earth.png");
        map.put("Mars",    "planets/mars.png");
        map.put("Jupiter", "planets/jupiter.png");
        map.put("Saturn",  "planets/saturn.png");
        map.put("Uranus",  "planets/uranus.png");
        map.put("Neptune", "planets/neptune.png");

        // wrap in unmodifiable so no class can accidentally mutate the map at runtime
        SPRITE_PATHS = Collections.unmodifiableMap(map);
    }

    // private constructor — this class is a constants holder, never instantiated
    private PlanetAssets() {}
}
