package io.github.some_example_name.SolarSystemSimulation.PlanetData;

import java.util.List;

// Top-level wrapper that mirrors the root JSON object in planets.json
// Gson maps the "planets" array directly into this list — field name must match the JSON key exactly
public class PlanetDataList {
    private List<PlanetData> planets;
    public List<PlanetData> getPlanets() { return planets; }
}
